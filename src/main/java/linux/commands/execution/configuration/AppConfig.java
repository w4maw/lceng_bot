package linux.commands.execution.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;

@Configuration
public class AppConfig {

    private TelegramConfig telegramConfig;

    public AppConfig(TelegramConfig telegramConfig) {
        this.telegramConfig = telegramConfig;
    }

    @Bean
    public SetWebhook setWebhook() {
        return SetWebhook.builder().url(telegramConfig.getWebhookPath()).build();
    }

    @Bean
    @Qualifier("telegram")
    public WebClient webclientTelegram() {
        return WebClient.create(telegramConfig.getApiUrl() + telegramConfig.getBotToken());
    }
}
