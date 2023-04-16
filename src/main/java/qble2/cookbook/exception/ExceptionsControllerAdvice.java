package qble2.cookbook.exception;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

// only works for synchronous exceptions
@ControllerAdvice
@Slf4j
public class ExceptionsControllerAdvice {

  public static final String ACCESS_DENIED_MESSAGE = "Access denied";
  public static final String METHOD_ARGUMENT_TYPE_MISMATCH_MESSAGE =
      "Method Argument Type Mismatch";
  public static final String METHOD_ARGUMENT_NOT_VALID_MESSAGE = "Method Argument Not Valid";
  public static final String CONSTRAINT_VIOLATION_MESSAGE = "Constraint Violation";
  public static final String INTERNAL_SERVER_ERROR_MESSAGE =
      "Internal Server Error (Please contact the administrator)";

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ResponseErrorDto> handleHttpRequestMethodNotSupportedException(
      HttpServletRequest request, HttpRequestMethodNotSupportedException exception) {
    return createErrorResponseEntity(request, HttpStatus.METHOD_NOT_ALLOWED, exception);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ResponseErrorDto> handleAccessDeniedException(HttpServletRequest request,
      AccessDeniedException exception) {
    return createErrorResponseEntity(request.getRequestURI(), HttpStatus.FORBIDDEN,
        ACCESS_DENIED_MESSAGE, List.of(exception.getMessage()));
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ResponseErrorDto> handleMissingParams(HttpServletRequest request,
      Exception exception) {
    return createErrorResponseEntity(request, HttpStatus.BAD_REQUEST, exception);
  }

  // Spring Validation (Controller level)
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ResponseErrorDto> handleMethodArgumentTypeMismatchException(
      HttpServletRequest request, MethodArgumentTypeMismatchException exception) {
    String requiredTypeName =
        exception.getRequiredType() != null ? exception.getRequiredType().getSimpleName()
            : "Unknown"; // because nullable

    String detail = String.format("%s should be of type %s ( received value: %s )",
        exception.getName(), requiredTypeName, exception.getValue());
    return createErrorResponseEntity(request.getRequestURI(), HttpStatus.BAD_REQUEST,
        METHOD_ARGUMENT_TYPE_MISMATCH_MESSAGE, List.of(detail));
  }

  // Spring Validation (Controller level)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ResponseErrorDto> handleMethodArgumentNotValidException(
      HttpServletRequest request, MethodArgumentNotValidException exception) {
    List<String> validationErrors = exception.getBindingResult().getFieldErrors().stream()
        .map(DefaultMessageSourceResolvable::getDefaultMessage) // received value: " +
        // error.getRejectedValue()
        .toList();
    return createErrorResponseEntity(request.getRequestURI(), HttpStatus.BAD_REQUEST,
        METHOD_ARGUMENT_NOT_VALID_MESSAGE, validationErrors);
  }

  // Spring Validation
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ResponseErrorDto> handleConstraintViolationException(
      HttpServletRequest request, ConstraintViolationException exception) {
    List<String> constraintViolations =
        exception.getConstraintViolations().stream().map(ConstraintViolation::getMessage).toList();
    return createErrorResponseEntity(request.getRequestURI(), HttpStatus.BAD_REQUEST,
        CONSTRAINT_VIOLATION_MESSAGE, constraintViolations);
  }

  @ExceptionHandler(InvalidArgumentException.class)
  public ResponseEntity<ResponseErrorDto> handleIllegalArgumentException(HttpServletRequest request,
      Exception exception) {
    return createErrorResponseEntity(request, HttpStatus.BAD_REQUEST, exception);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ResponseErrorDto> handleResourceNotFoundException(
      HttpServletRequest request, Exception exception) {
    return createErrorResponseEntity(request, HttpStatus.NOT_FOUND, exception);
  }

  // at last, as a fail-safe, to catch any unhandled server exception
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ResponseErrorDto> unhandledExceptions(HttpServletRequest request,
      Exception exception) {
    log.error("(Unhandled exception) An internal server error has occurred", exception);
    return createErrorResponseEntity(request.getRequestURI(), HttpStatus.INTERNAL_SERVER_ERROR,
        INTERNAL_SERVER_ERROR_MESSAGE, null);
  }

  /////
  /////
  /////

  private ResponseEntity<ResponseErrorDto> createErrorResponseEntity(HttpServletRequest request,
      HttpStatus httpStatus, Exception exception) {
    return createErrorResponseEntity(request.getRequestURI(), httpStatus, exception.getMessage(),
        null);
  }

  private ResponseEntity<ResponseErrorDto> createErrorResponseEntity(String requestUri,
      HttpStatus httpStatus, String message, List<String> details) {
    ResponseErrorDto responseError =
        ResponseErrorDto.builder().status(httpStatus.value()).error(httpStatus.getReasonPhrase())
            .message(message).details(details).path(requestUri).build();

    return new ResponseEntity<>(responseError, httpStatus);
  }

}
