package qble2.cookbook.security;

import java.io.Serial;
import java.util.Collection;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.security.core.GrantedAuthority;

@Getter
@Setter
@Accessors(chain = true, fluent = false)
public class CurrentUserDetails extends org.springframework.security.core.userdetails.User {

  @Serial
  private static final long serialVersionUID = 1L;

  private UUID id;

  public CurrentUserDetails(UUID id, String username, String password,
      Collection<? extends GrantedAuthority> authorities) {
    super(username, password, authorities);
    this.id = id;
  }

  public CurrentUserDetails(UUID id, String username, String password, boolean enabled,
      boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked,
      Collection<? extends GrantedAuthority> authorities) {
    super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked,
        authorities);
    this.id = id;
  }

}
