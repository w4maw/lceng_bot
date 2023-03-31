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
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class BotService{
    private TelegramConfig telegramConfig;
    private WebClient telegramWebClient;

    public BotService(@Qualifier("telegram") WebClient webclientTelegram, TelegramConfig telegramConfig) {
        this.telegramConfig = telegramConfig;
        this.telegramWebClient = webclientTelegram;
    }

//    @Override
    public Mono<Void> onWebhookUpdateReceived(Update update) {
        var message = update.getMessage();
        log.info("Получено сообщение {} от {}", message.getText(), update.getMessage().getChat().getUserName());
        var chatId = message.getChatId().toString();
        var text = message.getText();
        var telegramMessage = TelegramMessage.fromMsg(text).orElse(TelegramMessage.NONE);
        return switch (telegramMessage) {
            case START, HELP -> sendMessage(chatId, TelegramMessage.HELP.getMsg());
            case NONE -> sendMessage(chatId, TelegramMessage.HELP.getMsg());
        };
    }

    private Mono<Void> sendMessage(String chatId, String text) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>(2);
        formData.set("chat_id", chatId);
        formData.set("text", text);
        return telegramWebClient.post().uri("/sendMessage").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData)).exchange().then();
    }
}
