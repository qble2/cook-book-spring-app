package qble2.cookbook.recipe.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum RecipeSearchOperatorEnum {

  @JsonProperty("equal")
  EQUAL,

  @JsonProperty("notEqual")
  NOT_EQUAL,

  @JsonProperty("like")
  LIKE,

  @JsonProperty("gte")
  GTE,

  @JsonProperty("lte")
  LTE,

  @JsonProperty("any")
  ANY, // any matching value

  @JsonProperty("all")
  ALL, // all values should match

  @JsonProperty("none")
  NONE, // none of the values should match

}
