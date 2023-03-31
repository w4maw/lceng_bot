package linux.commands.execution.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class ShellService {
    @Value("${shell.dir}")
    String shellDir;

    public Mono<String> execute(String command) {
        log.info("Выполнение команды {}", command);
        var builder = new ProcessBuilder()
                .directory(new File(shellDir))
                .command("bash", "-c", command);
        try {
            var process = builder.start();
            var byteResult = process.getInputStream().readAllBytes();
            var result = new String(byteResult, StandardCharsets.UTF_8);
            log.info("Результат выполнения команды:\n {}", result);
            return Mono.just(result);
        } catch (IOException e) {
            log.error("При запуске ProcessBuilder возникла ошибка: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
