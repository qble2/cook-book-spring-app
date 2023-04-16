package qble2.cookbook.role.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;
import org.springframework.validation.annotation.Validated;
import qble2.cookbook.role.model.RoleEnum;
import qble2.cookbook.validation.UniqueRoleName;

@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Relation(collectionRelation = "roles")
@JsonPropertyOrder({"id", "name"})
@Validated
public class RoleDto extends RepresentationModel<RoleDto> {

  public interface OnCreateValidationGroup {

  }

  public interface OnUpdateValidationGroup {

  }

  @Null(groups = OnCreateValidationGroup.class, message = "{role.id.OnCreate.Null.message}")
  @NotNull(groups = OnUpdateValidationGroup.class, message = "{role.id.OnUpdate.NotNull.message}")
  @EqualsAndHashCode.Include
  @JsonProperty("id")
  private UUID id;

  @UniqueRoleName(groups = OnCreateValidationGroup.class)
  @NotNull(message = "{role.name.NotNull.message}")
  @JsonProperty("name")
  private RoleEnum name;

}
