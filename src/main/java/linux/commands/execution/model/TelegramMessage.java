package linux.commands.execution.model;

import java.util.Arrays;
import java.util.Optional;

public enum TelegramMessage {
    START("/start", ""),
    HELP("Здесь будет подсказка", ""),
    DATE("/date", ""),
    NMAP("/nmap", "nmap"),
    NC("/nc", "nc"),
    TEST("/test", ""),
    NONE("", "");
    private String msg;
    private String shellCommand;

    TelegramMessage(String msg, String shellCommand) {
        this.msg = msg;
        this.shellCommand = shellCommand;
    }

    public String getMsg() {
        return msg;
    }

    public static Optional<TelegramMessage> fromMsg(String msg) {
        return Arrays.stream(values())
                .filter(m -> m.getMsg().equals(msg))
                .findFirst();
    }
}
