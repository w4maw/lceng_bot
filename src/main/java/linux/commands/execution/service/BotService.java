package linux.commands.execution.service;

import linux.commands.execution.configuration.TelegramConfig;
import linux.commands.execution.model.TelegramMessage;
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
        var telegramMessage = TelegramMessage.fromMsg(tCommand).orElse(TelegramMessage.NONE);
        return switch (telegramMessage) {
            case START, HELP -> sendMessage(chatId, getHelpMessage());
            case DATE -> shellService.execute("date")
                    .flatMap(resp -> sendMessage(chatId, resp));
            case NMAP, NC -> executeCompound(chatId, text);
            case TEST -> sendMessage(chatId, randomText(300));
            case NONE -> sendMessage(chatId, "Неизвестная комманда.\nСписок доступных комманд:\n" + getHelpMessage());
        };
    }

    private Mono<Void> executeCompound(String chatId, String text) {
        var commandParts = text.trim().replaceAll("/", "").split("\\s+");
        if (commandParts.length == 1)
            sendMessage(chatId, "Неизвестная комманда\nСписок доступных комманд:\n" + getHelpMessage());
        switch (commandParts[0]) {
            case "nmap" -> shellService.execute("nmap %s".formatted(commandParts[1]))
                    .flatMap(resp -> sendMessage(chatId, resp));
            case "nc" -> shellService.execute("nc -zvw10 %s %s".formatted(commandParts[1], commandParts[2]));
        }
        return Mono.empty();
    }

    private Mono<Void> sendMessage(String chatId, String text) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>(2);
        formData.set("chat_id", chatId);
        formData.set("text", text);
        log.info("Отправляю ответ:\n {}", text);
        return telegramWebClient.post().uri("/sendMessage").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .exchange()
                .doOnError(throwable -> log.error("Вернулась ошибка: {}", throwable.getMessage()))
                .then();
    }

    private String getHelpMessage() {
        return """
                /help - текущее сообщение;
                /date - вывести текущую дату; 
                /nmap example.com - сканировать хост example.com;
                /nc example.com 80 - проверить открыт ли порт
                """;
    }

    private String randomText(int num) {
        var random = new SecureRandom();
        return random.ints(num)
                .mapToObj(Integer::toString)
                .collect(Collectors.joining(""));
    }
}
