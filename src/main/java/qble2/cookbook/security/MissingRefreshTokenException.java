package qble2.cookbook.security;

import java.io.Serial;

public class MissingRefreshTokenException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String MESSAGE_FORMAT = "Refresh Token is missing";

    public MissingRefreshTokenException() {
        super(getFormattedMessage());
    }

    // also called by tests
    public static String getFormattedMessage() {
        return String.format(MESSAGE_FORMAT);
    }

}
