package linux.commands.execution.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

@Slf4j
@Service
public class ShellService {
    @Value("${shell.dir}")
    String shellDir;

    public Mono<String> execute(String command) {
        return Mono.create(monoSink -> {
            try {
                log.info("Выполнение команды {}", command);
                var builder = new ProcessBuilder()
                        .directory(new File(shellDir))
                        .command("bash", "-c", command);
                var process = builder.start();
                var result = new StringBuilder();
                var scanner = new Scanner(process.getInputStream());
                while (scanner.hasNext()) {
                    result.append(scanner.nextLine())
                            .append("\n");
                }
                log.info("Результат выполнения команды:\n {}", result);
                monoSink.success(result.toString());
            } catch (IOException e) {
                log.error("При запуске ProcessBuilder возникла ошибка: {}", e.getMessage());
                monoSink.error(e);
            }
        });
    }
}
