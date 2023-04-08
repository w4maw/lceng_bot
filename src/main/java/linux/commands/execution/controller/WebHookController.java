package linux.commands.execution.controller;

import linux.commands.execution.service.BotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;

@RestController
public class WebHookController {

    @Autowired
    BotService botService;

    @PostMapping("/")
    public Mono<Void> onUpdateReceived(@RequestBody Update update) {
        return botService.onWebhookUpdateReceived(update);
    }
}
