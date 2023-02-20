package qble2.cookbook.recipe.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public enum RecipeSearchSortDirectionEnum {

  @JsonProperty("asc")
  ASC("asc"),

  @JsonProperty("desc")
  DESC("desc");

  private String value;

  private RecipeSearchSortDirectionEnum(String value) {
    this.value = value;
  }

}
