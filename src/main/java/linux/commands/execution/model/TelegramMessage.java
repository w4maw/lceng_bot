package linux.commands.execution.model;

import java.util.Arrays;
import java.util.Optional;

public enum TelegramMessage {
    START("/start"),
    HELP("Здесь будет подсказка"),
    NONE("");
    private String msg;

    TelegramMessage(String msg) {
        this.msg = msg;
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
