package qble2.cookbook.exception;

import java.io.Serial;

public class InvalidArgumentException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String MESSAGE = "Invalid argument";

    public InvalidArgumentException() {
        super(getFormattedMessage());
    }

    // also called by tests
    public static String getFormattedMessage() {
        return MESSAGE;
    }

}
