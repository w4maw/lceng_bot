package linux.commands.execution.service;

import linux.commands.execution.configuration.TelegramConfig;
import linux.commands.execution.model.TelegramMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.starter.SpringWebhookBot;

@Service
@Slf4j
public class BotService extends SpringWebhookBot {
    private TelegramConfig telegramConfig;

    public BotService(SetWebhook setWebhook, TelegramConfig telegramConfig) {
        super(setWebhook, telegramConfig.getBotToken());
        this.telegramConfig = telegramConfig;
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
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

    @Override
    public String getBotPath() {
        return telegramConfig.getWebhookPath();
    }

    @Override
    public String getBotUsername() {
        return telegramConfig.getBotName();
    }

    private SendMessage sendMessage(String chatId, String text) {
        return new SendMessage(chatId, text);
    }
}
