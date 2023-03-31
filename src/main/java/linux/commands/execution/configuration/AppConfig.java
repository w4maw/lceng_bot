package linux.commands.execution.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
}
