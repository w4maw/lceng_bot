package linux.commands.execution.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "telegram")
@Component
public class TelegramConfig{
    String apiUrl;
    private String webhookPath;
    private String botName;
    private String botToken;
}
