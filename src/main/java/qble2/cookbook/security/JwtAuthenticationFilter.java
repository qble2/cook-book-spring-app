package qble2.cookbook.security;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import qble2.cookbook.exception.ResponseErrorDto;
import qble2.cookbook.user.UserService;

@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

  private UserService userService;
  private JwtUtils jwtUtils;
  private AuthenticationManager authenticationManager;

  public JwtAuthenticationFilter(UserService userService, JwtUtils jwtUtils,
      AuthenticationManager authenticationManager) {
    this.userService = userService;
    this.jwtUtils = jwtUtils;
    this.authenticationManager = authenticationManager;
  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request,
      HttpServletResponse response) throws AuthenticationException {
    String username = request.getParameter("username");
    String password = request.getParameter("password");
    log.info("Authentication attempt from user: {}", username);

    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(username, password);
    return this.authenticationManager.authenticate(authenticationToken);
  }

  @Override
  protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain, Authentication authentication) throws IOException, ServletException {
    org.springframework.security.core.userdetails.User user =
        (org.springframework.security.core.userdetails.User) authentication.getPrincipal();

    List<String> roles =
        user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
    String accessToken = this.jwtUtils.createAccessToken(user.getUsername(), roles,
        request.getRequestURL().toString());
    String refreshToken =
        this.jwtUtils.createRefreshToken(user.getUsername(), request.getRequestURL().toString());
    Map<String, String> tokens = this.jwtUtils.formatTokens(accessToken, refreshToken);
    Map<String, Object> loggedInUserWithTokens =
        Map.of("tokens", tokens, "user", this.userService.getUserByUsername(user.getUsername()));

    log.info("User {} has been successfully authenticated", user.getUsername());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    new ObjectMapper().writeValue(response.getOutputStream(), loggedInUserWithTokens);
  }

  @Override
  protected void unsuccessfulAuthentication(HttpServletRequest request,
      HttpServletResponse response, AuthenticationException exception)
      throws IOException, ServletException {
    // XXX BKE add logic here to stop brute force attacks, exceeded max attempts for a period of
    // time, ...
    log.error("Authentication failed for user: {} (path: {})", request.getParameter("username"),
        request.getRequestURI());
    /**
     * XXX BKE delegating all exceptions handling to {@link AuthenticationExceptionHandler} makes it
     * harder to differenciate failed logins from unauthenticated calls to the other endpoints
     */
    // super.unsuccessfulAuthentication(request, response, exception);

    ResponseErrorDto responseError = ResponseErrorDto.builder().status(HttpStatus.UNAUTHORIZED.value())
        .message("Authentication failed") // exception.getMessage()) holds internal cause (ex:
                                          // Resource not found)
        .path(request.getRequestURI()).build();

    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    OutputStream responseStream = response.getOutputStream();
    new ObjectMapper().writeValue(responseStream, responseError);

  }

}
