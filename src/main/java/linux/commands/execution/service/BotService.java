package linux.commands.execution.service;

import linux.commands.execution.model.CommandType;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BotService{
    private WebClient telegramWebClient;
    private ShellService shellService;

    public BotService(
            @Qualifier("telegram") WebClient webclientTelegram,
            ShellService shellService
    ) {
        this.telegramWebClient = webclientTelegram;
        this.shellService = shellService;
    }

    public Mono<Void> onWebhookUpdateReceived(Update update) {
        var message = update.getMessage();
        log.info("Получено сообщение: {} от {}", message.getText(), update.getMessage().getChat().getUserName());
        var chatId = message.getChatId().toString();
        var text = message.getText();
        var tCommand = text.split("\\s+")[0];
        var commandType = CommandType.fromMsg(tCommand).orElse(CommandType.NONE);
        return switch (commandType) {
            case START, HELP -> sendMessage(chatId, getHelpMessage());
            case SCAN, PORT, DATE -> execute(commandType, chatId, text);
            case TEST -> sendMessage(chatId, randomText(300));
            case NONE -> sendMessage(chatId, "Неизвестная комманда %s.\nСписок доступных комманд:\n%s".formatted(tCommand, getHelpMessage()));
        };
    }

    private Mono<Void> execute(CommandType commandType, String chatId, String text) {
        var commandParts = text.trim().replaceAll("/", "").split("\\s+");
        validate(commandType, commandParts).subscribe(
                unused -> shellService.execute(assembleCommand(commandType, commandParts).get())
                        .subscribe(resp -> sendMessage(chatId, resp)),
                throwable -> sendMessage(chatId, "Неправильное количество аргументов.")
        );
        return Mono.empty();
    }

    private Supplier<String> assembleCommand(CommandType commandType, String[] commandParts) {
        return () ->
                switch (commandType) {
                    case DATE -> "date";
                    case SCAN -> "nmap %s".formatted(commandParts[1]);
                    case PORT -> "nmap -p%s %s".formatted(commandParts[2], commandParts[1]);
                    default -> throw new RuntimeException("Команда еще не реализована");
                };
    }

    private Mono<Boolean> validate(CommandType command, String[] commandParts) {
        return commandParts.length == command.getLength() ? Mono.just(true) : Mono.error(IllegalArgumentException::new);
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
                /port example.com 80 - проверить открыт ли порт 80;
                """;
    }

    private String randomText(int num) {
        var random = new SecureRandom();
        return random.ints(num)
                .mapToObj(Integer::toString)
                .collect(Collectors.joining(""));
    }
}
