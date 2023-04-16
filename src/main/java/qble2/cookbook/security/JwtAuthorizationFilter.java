package qble2.cookbook.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;
import qble2.cookbook.exception.ResponseErrorDto;
import qble2.cookbook.user.UserService;

@Slf4j
public class JwtAuthorizationFilter extends OncePerRequestFilter {

  private JwtUtils jwtUtils;
  private UserService userService;

  public JwtAuthorizationFilter(JwtUtils jwtUtils, UserService userService) {
    this.jwtUtils = jwtUtils;
    this.userService = userService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    // ignore these requests
    if (request.getServletPath().equals("api/auth/signup")
        || request.getServletPath().equals("/api/auth/login")
        || request.getServletPath().equals("/api/auth/refresh-token")
        || !this.jwtUtils.existsToken(request)) {
      filterChain.doFilter(request, response);

      return;
    }

    try {
      String accessToken = this.jwtUtils.getToken(request);
      DecodedJWT decodedJWT = this.jwtUtils.getDecodedJwt(accessToken);

      String username = decodedJWT.getSubject();
      UserDetails userDetails = this.userService.loadUserByUsername(username);
      String[] roles = decodedJWT.getClaim("roles").asArray(String.class);

      Collection<SimpleGrantedAuthority> authorities =
          Arrays.stream(roles).map(SimpleGrantedAuthority::new).toList();
      // UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new
      // UsernamePasswordAuthenticationToken(
      // username, null, authorities);
      UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
          new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
      SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

      filterChain.doFilter(request, response);
    } catch (Exception e) {
      log.error("JWT Authorization failed: {}", e.getMessage());

      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      // response.setStatus(HttpStatus.FORBIDDEN.value());
      response.setStatus(HttpStatus.UNAUTHORIZED.value());

      ResponseErrorDto reponseError = ResponseErrorDto.builder().message(e.getMessage())
          // .status(HttpStatus.FORBIDDEN.value())
          .status(HttpStatus.UNAUTHORIZED.value()).path(request.getServletPath()).build();
      new ObjectMapper().writeValue(response.getOutputStream(), reponseError);
    }
  }

}
