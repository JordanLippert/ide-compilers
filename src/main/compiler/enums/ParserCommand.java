package compiler.enums;

public enum ParserCommand {
    SHIFT(0),
    REDUCE(1),
    ACTION(2),
    ACCEPT(3),
    GO_TO (4),
    ERROR (5);

    private final int value;

    ParserCommand(int value) {
        this.value = value;
    }

    public static ParserCommand fromValue(int value) {
        for (ParserCommand c : values()) {
            if (c.value == value) {
                return c;
            }
        }
        return ERROR; // or throw exception if you prefer
    }

    public int getValue() {
        return value;
    }
}
