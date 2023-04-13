package linux.commands.execution.model;

import java.util.Arrays;
import java.util.Optional;

public enum CommandType {
    START("/start", 1),
    HELP("/help", 1),
    DATE("/date", 1),
    SCAN("/scan", 2),
    PORT("/port", 3),
    MAN("/man", 2),
    IP("/ip", 2),
    NONE("", 0);
    private String msg;
    private int length;

    CommandType(String msg, int length) {
        this.msg = msg;
        this.length = length;
    }

    public String getMsg() {
        return msg;
    }

    public int getLength() {
        return length;
    }

    public static Optional<CommandType> fromMsg(String msg) {
        return Arrays.stream(values())
                .filter(m -> m.getMsg().equals(msg))
                .findFirst();
    }
}
