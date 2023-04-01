package linux.commands.execution.service;

import linux.commands.execution.configuration.TelegramConfig;
import linux.commands.execution.model.Command;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;
import java.security.SecureRandom;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BotService{
    private TelegramConfig telegramConfig;
    private WebClient telegramWebClient;
    private ShellService shellService;

    public BotService(
            @Qualifier("telegram") WebClient webclientTelegram,
            TelegramConfig telegramConfig,
            ShellService shellService
    ) {
        this.telegramConfig = telegramConfig;
        this.telegramWebClient = webclientTelegram;
        this.shellService = shellService;
    }

    public Mono<Void> onWebhookUpdateReceived(Update update) {
        var message = update.getMessage();
        log.info("Получено сообщение: {} от {}", message.getText(), update.getMessage().getChat().getUserName());
        var chatId = message.getChatId().toString();
        var text = message.getText();
        var tCommand = text.split("\\s+")[0];
        var command = Command.fromMsg(tCommand).orElse(Command.NONE);
        return switch (command) {
            case START, HELP -> sendMessage(chatId, getHelpMessage());
            case SCAN, DATE -> execute(command, chatId, text);
            case TEST -> sendMessage(chatId, randomText(300));
            case NONE -> sendMessage(chatId, "Неизвестная комманда %s.\nСписок доступных комманд:\n%s".formatted(tCommand, getHelpMessage()));
        };
    }

    private Mono<Void> execute(Command command, String chatId, String text) {
        var commandParts = text.trim().replaceAll("/", "").split("\\s+");
        switch (command) {
            case DATE -> validate(command, commandParts).subscribe(
                    unused -> shellService.execute("date")
                            .subscribe(resp ->sendMessage(chatId, resp)),
                    throwable -> sendMessage(chatId, "Неправильное количество аргументов.")
            );
            case SCAN -> validate(command, commandParts).subscribe(
                    unused -> shellService.execute("nmap %s".formatted(commandParts[1]))
                            .subscribe(resp ->sendMessage(chatId, resp)),
                    throwable -> sendMessage(chatId, "Неправильное количество аргументов.")
            );
        }
        return Mono.empty();
    }

    private Mono<Boolean> validate(Command command, String[] commandParts) {
        return switch (command) {
            case DATE -> commandParts.length == 1 ? Mono.just(true) : Mono.error(RuntimeException::new);
            case SCAN -> commandParts.length == 2 ? Mono.just(true) : Mono.error(RuntimeException::new);
            default -> Mono.empty();
        };
    }

    private Mono<Void> sendMessage(String chatId, String text) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>(2);
        formData.set("chat_id", chatId);
        formData.set("text", text);
        log.info("Отправляю ответ...");
        telegramWebClient.post()
                .uri("/sendMessage")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(log::debug, throwable -> log.error("Телеграм вернул ошибку: {}", throwable.getMessage()));
        return Mono.empty();
    }

    private String getHelpMessage() {
        return """
                /help - текущее сообщение;
                /date - вывести текущую дату;
                /scan example.com - сканировать хост example.com;
                """;
    }

    private String randomText(int num) {
        var random = new SecureRandom();
        return random.ints(num)
                .mapToObj(Integer::toString)
                .collect(Collectors.joining(""));
    }
}
