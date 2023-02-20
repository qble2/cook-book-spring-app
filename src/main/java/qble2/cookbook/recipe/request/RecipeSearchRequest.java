package qble2.cookbook.recipe.request;

import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RecipeSearchRequest {

  @JsonProperty("userId")
  private UUID userId;

  @JsonProperty("filters")
  private List<RecipeSearchFilter> filters;

  @JsonProperty("sort")
  private RecipeSearchSort sort;

}
