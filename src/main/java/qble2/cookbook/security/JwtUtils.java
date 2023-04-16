package qble2.cookbook.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:jwt.properties")
public class JwtUtils {

  @Value("${jwt.secret}")
  private String authorizationSecret;

  @Value("${jwt.access.token.name}")
  private String accessTokenName;

  @Value("${jwt.refresh.token.name}")
  private String refreshTokenName;

  @Value("${jwt.token.brearer.prefix}")
  public String authorizationHeaderPrefix;

  @Value("${jwt.access.token.expîration.in.hours}")
  private int accessTokenExpirationInHours;

  @Value("${jwt.refresh.token.expîration.in.hours}")
  private int refreshTokenExpirationInHours;

  private Algorithm algorithm;

  public Map<String, String> createAndFormatTokens(String username, List<String> roles,
      String issuer) {
    return formatTokens(createAccessToken(username, roles, issuer),
        createRefreshToken(username, issuer));
  }

  public String createAccessToken(String username, List<String> roles, String issuer) {
    return JWT.create().withSubject(username)
        .withExpiresAt(new Date(
            System.currentTimeMillis() + this.accessTokenExpirationInHours * 60 * 60 * 1000))
        .withIssuer(issuer).withClaim("roles", roles).sign(getAlgorithm());
  }

  public String createRefreshToken(String username, String issuer) {
    return JWT.create().withSubject(username)
        .withExpiresAt(new Date(
            System.currentTimeMillis() + this.refreshTokenExpirationInHours * 60 * 60 * 1000))
        .withIssuer(issuer).sign(getAlgorithm());
  }

  public DecodedJWT getDecodedJwt(String token) {
    JWTVerifier jwtVerifier = JWT.require(getAlgorithm()).build();
    return jwtVerifier.verify(token);
  }

  public Map<String, String> formatTokens(String accessToken, String refreshToken) {
    return Map.of(this.accessTokenName, accessToken, this.refreshTokenName, refreshToken);
  }

  public boolean existsToken(HttpServletRequest request) {
    String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    return authorizationHeader != null
        && authorizationHeader.startsWith(this.authorizationHeaderPrefix);
  }

  public String getToken(HttpServletRequest request) {
    String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    return authorizationHeader.substring(this.authorizationHeaderPrefix.length());
  }

  public Algorithm getAlgorithm() {
    if (algorithm == null) {
      algorithm = Algorithm.HMAC256(this.authorizationSecret.getBytes());
    }
    return algorithm;
  }

}
