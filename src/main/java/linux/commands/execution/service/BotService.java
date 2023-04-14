package linux.commands.execution.service;

import linux.commands.execution.model.CommandType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Slf4j
public class BotService{
    private ShellService shellService;
    private NetworkService networkService;

    public BotService(
            ShellService shellService,
            NetworkService networkService
    ) {
        this.shellService = shellService;
        this.networkService = networkService;
    }

    public Mono<Void> execute(CommandType commandType, String chatId, String text) {
        var commandParts = text.trim().replaceAll("/", "").split("\\s+");
        validate(commandType, commandParts).subscribeOn(Schedulers.boundedElastic()).subscribe(
                unused -> assembleCommand(commandType, commandParts)
                        .flatMap(shellService::execute)
                        .subscribe(resp -> networkService.sendMessage(chatId, resp)),
                throwable -> networkService.sendMessage(chatId, "Неправильное количество аргументов.")
        );
        return Mono.empty();
    }

    private Mono<String> assembleCommand(CommandType commandType, String[] commandParts) {
        return Mono.create(monoSink -> {
            switch (commandType) {
                case DATE -> monoSink.success("date");
                case SCAN -> monoSink.success("nmap %s".formatted(commandParts[1]));
                case PORT -> monoSink.success("nmap -p%s %s".formatted(commandParts[2], commandParts[1]));
                case MAN -> monoSink.success("curl -Ls -o /dev/null -w %{url_effective} https://manpages.debian.org/bullseye/".concat(commandParts[1]));
                case IP -> monoSink.success("nslookup %s 1.1.1.1".formatted(commandParts[1]));
                default -> monoSink.error(new RuntimeException("Команда еще не реализована"));
            }
        });
    }

    private Mono<Boolean> validate(CommandType command, String[] commandParts) {
        return commandParts.length == command.getLength() ? Mono.just(true) : Mono.error(IllegalArgumentException::new);
    }

    public String getHelpMessage() {
        return """
                /help - текущее сообщение;
                /date - вывести текущую дату;
                /man command - вернет ссылку на man по команде
                /scan example.com - сканировать хост example.com;
                /port example.com 80 - проверить открыт ли порт 80;
                /ip example.com - узнать ip адрес example.com
                """;
    }
}
