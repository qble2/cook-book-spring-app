package qble2.cookbook.recipe.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public enum RecipeSearchFilterKeyEnum {

  @JsonProperty("userRecipes")
  USER_RECIPES("userRecipes"),

  @JsonProperty("favoriteRecipes")
  FAVORITE_RECIPES("favoriteRecipes"),

  @JsonProperty("author")
  RECIPE_AUTHOR("author"),

  @JsonProperty("name")
  RECIPE_NAME("name"),

  @JsonProperty("description")
  RECIPE_DESCRIPTION("description"),

  @JsonProperty("servings")
  RECIPE_SERVINGS("servings"),

  @JsonProperty("preparationTime")
  RECIPE_PREPARATION_TIME("preparationTime"),

  @JsonProperty("cookingTime")
  RECIPE_COOKING_TIME("cookingTime"),

  @JsonProperty("tags")
  RECIPE_TAGS("tags"),

  @JsonProperty("ingredients")
  RECIPE_INGREDIENTS("ingredients"),

  @JsonProperty("averageRating")
  RECIPE_AVERAGE_RATING("averageRating");

  private String key;

  private RecipeSearchFilterKeyEnum(String key) {
    this.key = key;
  }

}
