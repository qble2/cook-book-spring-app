package qble2.cookbook.recipe.request;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import qble2.cookbook.recipe.enums.RecipeSearchFilterKeyEnum;
import qble2.cookbook.recipe.enums.RecipeSearchOperatorEnum;

@Data
public class RecipeSearchFilter {

  @JsonProperty("key")
  private RecipeSearchFilterKeyEnum key;

  @JsonProperty("operator")
  private RecipeSearchOperatorEnum operator;

  @JsonProperty("value")
  private Object value;

  @JsonProperty("values")
  private List<Object> values;

  // allow to safely receive int or double values
  // entity field type will be the one used in the generated sql
  public Number getValueAsNumber() {
    if (value instanceof Integer)
      return (Integer) value;

    if (value instanceof Double)
      return (Double) value;

    return null;
  }

}
