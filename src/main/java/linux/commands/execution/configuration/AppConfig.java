package linux.commands.execution.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
public class AppConfig {

    private TelegramConfig telegramConfig;

    public AppConfig(TelegramConfig telegramConfig) {
        this.telegramConfig = telegramConfig;
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
                log.debug("Request: Method {} {}", clientRequest.method(), clientRequest.url());
                clientRequest.headers()
                        .forEach((name, values) -> values.forEach(value -> log.debug("{}: {}", name, values)));
                log.debug("Body: \n{}", clientRequest.body());
            }
            return Mono.just(clientRequest);
        });
    }
}
