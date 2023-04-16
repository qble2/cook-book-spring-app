package qble2.cookbook.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import qble2.cookbook.exception.ResponseErrorDto;

@Slf4j
public class AuthenticationExceptionHandler implements AuthenticationEntryPoint {

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException authException) throws IOException {
    log.error("Received unauthenticated call to access resource (path: {})",
        request.getRequestURI());
    ResponseErrorDto responseError = ResponseErrorDto.builder()
        .status(HttpStatus.UNAUTHORIZED.value())
        // authException.getMessage() = "Full authentication is required to access this resource"
        .message(authException.getMessage())
        // .message("You need to be logged-in to access this resource")
        .path(request.getRequestURI()).build();

    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    OutputStream responseStream = response.getOutputStream();
    new ObjectMapper().writeValue(responseStream, responseError);

    responseStream.flush();
  }

}
