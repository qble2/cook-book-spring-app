package qble2.cookbook.user.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Accessors(chain = true, fluent = false)
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Validated
public class UpdateUserRequest {

  @NotBlank(message = "{user.firstName.NotBlank.message}")
  @JsonProperty("firstName")
  private String firstName;

  @NotBlank(message = "{user.lastName.NotBlank.message}")
  @JsonProperty("lastName")
  private String lastName;

  @NotBlank(message = "{user.email.NotBlank.message}")
  @Email(message = "{user.email.Malformed.message}")
  @JsonProperty("email")
  private String email;

}
