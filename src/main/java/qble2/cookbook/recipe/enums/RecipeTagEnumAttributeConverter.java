package qble2.cookbook.recipe.enums;

import java.util.Arrays;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class RecipeTagEnumAttributeConverter implements AttributeConverter<RecipeTagEnum, String> {

  @Override
  public String convertToDatabaseColumn(RecipeTagEnum category) {
    if (category == null) {
      return null;
    }
    return category.getCode();
  }

  @Override
  public RecipeTagEnum convertToEntityAttribute(String recipeTagStringInDatabase) {
    if (recipeTagStringInDatabase == null) {
      return null;
    }
    return Arrays.stream(RecipeTagEnum.values())
        .filter(t -> t.getCode().equals(recipeTagStringInDatabase)).findFirst()
        .orElseThrow(IllegalArgumentException::new);
  }

}
