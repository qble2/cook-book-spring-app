package qble2.cookbook.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;
import org.springframework.validation.annotation.Validated;
import qble2.cookbook.recipe.dto.RecipeDto;
import qble2.cookbook.review.dto.ReviewDto;
import qble2.cookbook.role.dto.RoleDto;
import qble2.cookbook.validation.UniqueUserEmail;
import qble2.cookbook.validation.UniqueUserUsername;

@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Getter
@Setter
@Accessors(chain = true, fluent = false)
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Relation(collectionRelation = "users")
@JsonPropertyOrder({"id", "username", "firstName", "lastName", "email", "roles"})
@Validated
public class UserDto extends RepresentationModel<UserDto> {

  public interface OnCreateValidationGroup {

  }

  public interface OnUpdateValidationGroup {

  }

  @Null(groups = OnCreateValidationGroup.class, message = "{user.id.OnCreate.Null.message}")
  @NotNull(groups = OnUpdateValidationGroup.class, message = "{user.id.OnUpdate.NotNull.message}")
  @EqualsAndHashCode.Include
  @JsonProperty("id")
  private UUID id;

  @NotBlank(message = "{user.username.NotBlank.message}")
  @UniqueUserUsername(groups = OnCreateValidationGroup.class)
  @JsonProperty("username")
  private String username;

  @NotBlank(message = "{user.password.NotBlank.message}")
  @JsonProperty("password")
  private String password;

  @NotBlank(message = "{user.firstName.NotBlank.message}")
  @JsonProperty("firstName")
  private String firstName;

  @NotBlank(message = "{user.lastName.NotBlank.message}")
  @JsonProperty("lastName")
  private String lastName;

  @NotBlank(message = "{user.email.NotBlank.message}") // @Email considers null values as valid
  @Email(message = "{user.email.Malformed.message}")
  @UniqueUserEmail(groups = OnCreateValidationGroup.class)
  @JsonProperty("email")
  private String email;

  @JsonProperty("roles")
  @Builder.Default
  private List<RoleDto> roles = new ArrayList<>();

  @JsonIgnore
  // @JsonProperty("recipes")
  @Builder.Default
  private List<RecipeDto> recipes = new ArrayList<>();

  @JsonIgnore
  // @JsonProperty("reviews")
  @Builder.Default
  private List<ReviewDto> reviews = new ArrayList<>();

}
