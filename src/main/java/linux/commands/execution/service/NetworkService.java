package linux.commands.execution.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import java.util.HashMap;

@Slf4j
@Service
public class NetworkService {
    @Autowired
    @Qualifier("telegram")
    private WebClient telegramWebClient;

    public Mono<Void> sendMessage(String chatId, String text) {
        var response = new HashMap<String, String>();
        response.put("chat_id", chatId);
        response.put("text", text);
        log.info("Отправляю ответ...");
        telegramWebClient.post()
                .uri("/sendMessage")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(response)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(log::debug, throwable -> {
                    log.error("Телеграм вернул ошибку: {}", ((WebClientResponseException.BadRequest) throwable).getResponseBodyAsString());
                });
        return Mono.empty();
    }
}
