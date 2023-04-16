package qble2.cookbook.recipe.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "tags")
public enum RecipeTagEnum {

  @JsonProperty("Starter course")
  STARTER_COURSE("Starter course"),

  @JsonProperty("Main course")
  MAIN_COURSE("Main course"),

  @JsonProperty("Dessert")
  DESSERT("Dessert"),

  @JsonProperty("Breakfast")
  BREAKFAST("Breakfast"),

  @JsonProperty("Lunch")
  LUNCH("Lunch"),

  @JsonProperty("Dinner")
  DINNER("Dinner"),

  @JsonProperty("Soup")
  SOUP("Soup"),

  @JsonProperty("Salad")
  SALAD("Salad"),

  @JsonProperty("Drink")
  DRINK("Drink"),

  @JsonProperty("Cake")
  CAKE("Cake"),

  @JsonProperty("Pie")
  PIE("Pie"),

  @JsonProperty("Bread")
  BREAD("Bread"),

  @JsonProperty("Sweet")
  SWEET("Sweet"),

  @JsonProperty("Salty")
  SALTY("Salty"),

  ;

  private final String code;

  private RecipeTagEnum(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }

  public static Set<RecipeTagEnum> getAllTags() {
    return Set.of(RecipeTagEnum.values());
  }
}
