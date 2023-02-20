package qble2.cookbook.security;

public class MissingRefreshTokenException extends RuntimeException {

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
