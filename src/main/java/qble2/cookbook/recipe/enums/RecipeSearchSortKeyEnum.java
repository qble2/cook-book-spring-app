package qble2.cookbook.recipe.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public enum RecipeSearchSortKeyEnum {

  @JsonProperty("author")
  RECIPE_AUTHOR("author"),

  @JsonProperty("name")
  RECIPE_NAME("name"),

  @JsonProperty("preparationTime")
  RECIPE_PREPARATION_TIME("preparationTime"),

  @JsonProperty("cookingTime")
  RECIPE_COOKING_TIME("cookingTime"),

  @JsonProperty("averageRating")
  RECIPE_AVERAGE_RATING("averageRating"),

  @JsonProperty("createdAt")
  RECIPE_CREATED_AT("createdAt");

  private String value;

  private RecipeSearchSortKeyEnum(String value) {
    this.value = value;
  }

}
