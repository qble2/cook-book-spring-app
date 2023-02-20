package qble2.cookbook.recipe.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import qble2.cookbook.recipe.enums.RecipeSearchSortDirectionEnum;
import qble2.cookbook.recipe.enums.RecipeSearchSortKeyEnum;

@Data
@AllArgsConstructor
public class RecipeSearchSort {

  @JsonProperty("key")
  private RecipeSearchSortKeyEnum key;

  @JsonProperty("direction")
  private RecipeSearchSortDirectionEnum direction;

}
