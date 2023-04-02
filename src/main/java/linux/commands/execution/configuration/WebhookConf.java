package linux.commands.execution.configuration;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

@Profile("prod")
@Slf4j
public class WebhookConf {
    @Autowired
    private TelegramConfig telegramConfig;

    @PostConstruct
    void init() {
        WebClient.create(telegramConfig.getApiUrl() + telegramConfig.getBotToken())
                .get()
                .uri("/setWebhook?url={url}", telegramConfig.getWebhookPath())
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(
                        resp -> log.debug("Webhook успешно установлен на {}. Вернулся ответ: {}", telegramConfig.getWebhookPath(), resp),
                        throwable -> {
                            log.error("При установке Webhook произошла ошибка: {}", throwable.getMessage());
                            throw new RuntimeException(throwable);
                        }
                );
    }
}
