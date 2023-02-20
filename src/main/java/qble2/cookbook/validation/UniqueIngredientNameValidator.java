package qble2.cookbook.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import qble2.cookbook.ingredient.IngredientRepository;

public class UniqueIngredientNameValidator
    implements ConstraintValidator<UniqueIngredientName, String> {

  @Autowired
  private IngredientRepository ingredientRepository;

  public UniqueIngredientNameValidator(IngredientRepository ingredientRepository) {
    this.ingredientRepository = ingredientRepository;
  }

  @Override
  public boolean isValid(String name, ConstraintValidatorContext context) {
    return name != null && !this.ingredientRepository.existsByName(name);
  }

}
