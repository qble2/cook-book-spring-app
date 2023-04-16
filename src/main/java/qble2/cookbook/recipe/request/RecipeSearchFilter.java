package qble2.cookbook.recipe.request;

import java.io.Serializable;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import qble2.cookbook.recipe.enums.RecipeSearchFilterKeyEnum;
import qble2.cookbook.recipe.enums.RecipeSearchOperatorEnum;

@Data
public class RecipeSearchFilter implements Serializable {

  private static final long serialVersionUID = 1L;

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
    if (value instanceof Integer valueAs)
      return valueAs;

    if (value instanceof Double valueAs)
      return valueAs;

    return null;
  }

}
