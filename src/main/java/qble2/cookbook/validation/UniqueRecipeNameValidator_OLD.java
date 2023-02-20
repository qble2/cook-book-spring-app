package qble2.cookbook.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import qble2.cookbook.recipe.RecipeRepository;

/**
 * @deprecated is not sufficient to cover updating a recipe without changing its name use
 *             {@link qble2.cookbook.validation.UniqueRecipeNameValidator} instead
 */
@Deprecated
public class UniqueRecipeNameValidator_OLD
    implements ConstraintValidator<UniqueRecipeName_OLD, String> {

  @Autowired
  private RecipeRepository recipeRepository;

  public UniqueRecipeNameValidator_OLD(RecipeRepository recipeRepository) {
    this.recipeRepository = recipeRepository;
  }

  @Override
  public boolean isValid(String name, ConstraintValidatorContext context) {
    return name != null && !this.recipeRepository.existsByName(name);
  }

}
