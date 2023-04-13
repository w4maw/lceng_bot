package linux.commands.execution.controller;

import linux.commands.execution.model.CommandType;
import linux.commands.execution.service.BotService;
import linux.commands.execution.service.NetworkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;
import java.util.Objects;

@RestController
@Slf4j
public class WebHookController {

    @Autowired
    BotService botService;

    @Autowired
    NetworkService networkService;

    @PostMapping("/")
    public Mono<Void> onUpdateReceived(@RequestBody Update update) {
        var message = update.getMessage();
        if (Objects.isNull(message)) {
            log.debug("Message is null. EditedMessage is {}", update.getEditedMessage());
            return Mono.empty();
        }
        log.info("Получено сообщение: {} от {}", message.getText(), update.getMessage().getChat().getUserName());
        var chatId = message.getChatId().toString();
        var text = message.getText();
        var tCommand = text.split("\\s+")[0];
        var commandType = CommandType.fromMsg(tCommand).orElse(CommandType.NONE);
        return switch (commandType) {
            case START, HELP -> networkService.sendMessage(chatId, botService.getHelpMessage());
            case SCAN, PORT, DATE, MAN, IP -> botService.execute(commandType, chatId, text);
            case NONE -> networkService.sendMessage(chatId, "Неизвестная комманда %s.\nСписок доступных комманд:\n%s".formatted(tCommand, botService.getHelpMessage()));
        };
    }
}
