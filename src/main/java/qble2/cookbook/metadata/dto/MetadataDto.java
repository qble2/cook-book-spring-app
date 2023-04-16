package qble2.cookbook.metadata.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;
import qble2.cookbook.ingredient.dto.IngredientDto;
import qble2.cookbook.ingredient.model.UnitOfMeasureEnum;
import qble2.cookbook.recipe.enums.RecipeTagEnum;

@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Relation(collectionRelation = "ingredients")
@JsonPropertyOrder({"availableTags", "availableUnitOfMeasures", "availableIngredients"})
public class MetadataDto extends RepresentationModel<MetadataDto> {

  @JsonProperty("availableTags")
  private List<RecipeTagEnum> availableTags;

  @JsonProperty("availableUnitOfMeasures")
  private List<UnitOfMeasureEnum> availableUnitOfMeasures;

  @JsonProperty("availableIngredients")
  private List<IngredientDto> availableIngredients;

}
