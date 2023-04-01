package linux.commands.execution.model;

import java.util.Arrays;
import java.util.Optional;

public enum Command {
    START("/start"),
    HELP("/help"),
    DATE("/date"),
    SCAN("/scan"),
    TEST("/test"),
    NONE("");
    private String msg;

    Command(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public static Optional<Command> fromMsg(String msg) {
        return Arrays.stream(values())
                .filter(m -> m.getMsg().equals(msg))
                .findFirst();
    }
}
