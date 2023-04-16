package qble2.cookbook.recipe.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashSet;
import java.util.Set;
import javax.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.validation.annotation.Validated;
import qble2.cookbook.recipe.enums.RecipeTagEnum;

@Getter
@Setter
@Accessors(chain = true, fluent = false)
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Validated
public class UpdateRecipeRequest {

  @JsonProperty("name")
  private String name;

  @JsonProperty("description")
  private String description;

  @Min(value = 1, message = "{recipe.servings.Min.message} {value}")
  @JsonProperty("servings")
  private Integer servings;

  @JsonProperty("preparationTime")
  private Long preparationTime;

  @JsonProperty("cookingTime")
  private Long cookingTime;

  @JsonProperty("tags")
  @Builder.Default
  private Set<RecipeTagEnum> tags = new HashSet<>();

}
