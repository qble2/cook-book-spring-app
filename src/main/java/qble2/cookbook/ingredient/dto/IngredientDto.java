package qble2.cookbook.ingredient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import java.util.UUID;
import javax.validation.constraints.Min;
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
import qble2.cookbook.ingredient.model.UnitOfMeasureEnum;
import qble2.cookbook.recipe.dto.RecipeDto;
import qble2.cookbook.validation.UniqueIngredientName;

@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Getter
@Setter
@Accessors(chain = true, fluent = false)
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Relation(collectionRelation = "ingredients")
@JsonPropertyOrder({"id", "name", "quantity", "unitOfMeasure", "defaultUnitOfMeasure"})
@Validated
public class IngredientDto extends RepresentationModel<IngredientDto> {

  public interface OnCreateValidationGroup {

  }

  public interface OnUpdateValidationGroup {

  }

  @Null(groups = OnCreateValidationGroup.class, message = "{ingredient.id.OnCreate.Null.message}")
  @NotNull(
      groups = {OnUpdateValidationGroup.class, RecipeDto.OnCreateValidationGroup.class,
          RecipeDto.OnUpdateValidationGroup.class},
      message = "{ingredient.id.OnUpdate.NotNull.message}")
  @EqualsAndHashCode.Include
  @JsonProperty("id")
  private UUID id;

  @UniqueIngredientName(groups = OnCreateValidationGroup.class)
  @NotBlank(message = "{ingredient.name.NotBlank.message}")
  @JsonProperty("name")
  private String name;

  @NotNull(message = "{ingredient.quantity.NotNull.message}")
  @Min(value = 1, message = "{ingredient.quantity.Min.message}")
  @JsonProperty("quantity")
  private Integer quantity;

  @NotNull(message = "{ingredient.unitOfMeasure.NotNull.message}")
  @JsonProperty("unitOfMeasure")
  private UnitOfMeasureEnum unitOfMeasure;

  @JsonProperty("defaultUnitOfMeasure")
  @Builder.Default
  private UnitOfMeasureEnum defaultUnitOfMeasure = UnitOfMeasureEnum.GRAM;

  @JsonProperty("alternativeIngredients")
  private List<IngredientDto> alternatives;

}
