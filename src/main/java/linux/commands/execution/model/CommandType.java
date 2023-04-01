package linux.commands.execution.model;

import java.util.Arrays;
import java.util.Optional;

public enum CommandType {
    START("/start"),
    HELP("/help"),
    DATE("/date"),
    SCAN("/scan"),
    PORT("/port"),
    TEST("/test"),
    NONE("");
    private String msg;

    CommandType(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public static Optional<CommandType> fromMsg(String msg) {
        return Arrays.stream(values())
                .filter(m -> m.getMsg().equals(msg))
                .findFirst();
    }
}
