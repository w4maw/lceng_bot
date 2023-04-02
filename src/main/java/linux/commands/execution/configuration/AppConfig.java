package linux.commands.execution.configuration;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
public class AppConfig {

    private TelegramConfig telegramConfig;

    public AppConfig(TelegramConfig telegramConfig) {
        this.telegramConfig = telegramConfig;
    }

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

    @Bean
    public SetWebhook setWebhook() {
        return SetWebhook.builder().url(telegramConfig.getWebhookPath()).build();
    }

    @Bean
    @Qualifier("telegram")
    public WebClient webclientTelegram() {
        return WebClient.builder()
                .filter(this.logRequest())
                .baseUrl(telegramConfig.getApiUrl() + telegramConfig.getBotToken())
                .build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder("Request: \n")
                        .append("Method: %s".formatted(clientRequest.method()))
                        .append(" ").append(clientRequest.url());
                clientRequest.headers()
                        .forEach((name, values) -> values.forEach(value -> sb.append("%s: %s".formatted(name, values))
                                .append("\n")));
                log.debug(sb.toString());
                log.debug(clientRequest.body().toString());
            }
            return Mono.just(clientRequest);
        });
    }
}
