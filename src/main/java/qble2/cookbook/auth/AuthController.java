package qble2.cookbook.auth;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import qble2.cookbook.exception.ResponseErrorDto;
import qble2.cookbook.role.dto.RoleDto;
import qble2.cookbook.security.JwtUtils;
import qble2.cookbook.security.MissingRefreshTokenException;
import qble2.cookbook.user.UserController;
import qble2.cookbook.user.UserService;
import qble2.cookbook.user.dto.UserDto;

@RestController
@RequestMapping(path = AuthController.PATH,
    produces = {MediaTypes.HAL_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
@Validated
// @AllArgsConstructor needed to be able to inject mocked dependencies for unit testing
@AllArgsConstructor
@Slf4j
public class AuthController {

  public static final String PATH = "api/auth";

  @Autowired
  private JwtUtils jwtUtils;

  @Autowired
  private UserService userService;

  @PostMapping(path = "/signup", consumes = MediaType.APPLICATION_JSON_VALUE)
  @Validated(UserDto.OnCreateValidationGroup.class)
  public ResponseEntity<UserDto> registerUser(
      @Valid @RequestBody(required = true) UserDto userDto) {
    UserDto createdUserDto = this.userService.createUser(userDto);
    log.info("User {} has been successfully registered {}", createdUserDto.getUsername());

    final URI uri = MvcUriComponentsBuilder.fromController(UserController.class).path("/{userId}")
        .buildAndExpand(createdUserDto.getId()).toUri();

    return ResponseEntity.created(uri).body(createdUserDto);
  }

  @GetMapping("/refresh-token")
  public void refreshToken(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    if (!this.jwtUtils.existsToken(request)) {
      throw new MissingRefreshTokenException();
    }

    try {
      String refreshToken = this.jwtUtils.getToken(request);
      DecodedJWT decodedJWT = this.jwtUtils.getDecodedJwt(refreshToken);

      String username = decodedJWT.getSubject();
      UserDto userDto = this.userService.getUserByUsername(username);
      List<String> roles = userDto.getRoles().stream().map(RoleDto::toString).toList();

      String accessToken = this.jwtUtils.createAccessToken(userDto.getUsername(), roles,
          request.getRequestURL().toString());
      Map<String, String> tokens = this.jwtUtils.formatTokens(accessToken, refreshToken);

      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      new ObjectMapper().writeValue(response.getOutputStream(), tokens);
    } catch (Exception e) {
      log.error("JWT Refresh Token failed: {}", e.getMessage());

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
