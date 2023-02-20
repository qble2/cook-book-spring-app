package qble2.cookbook.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import qble2.cookbook.recipe.RecipeRepository;
import qble2.cookbook.recipe.dto.RecipeDto;
import qble2.cookbook.recipe.model.Recipe;

public class UniqueRecipeNameValidator implements ConstraintValidator<UniqueRecipeName, RecipeDto> {

  @Autowired
  private RecipeRepository recipeRepository;

  public UniqueRecipeNameValidator(RecipeRepository recipeRepository) {
    this.recipeRepository = recipeRepository;
  }

  @Override
  public boolean isValid(RecipeDto recipeDto, ConstraintValidatorContext context) {
    // only returning invalid if name already taken by another recipe (having a different id)
    if (recipeDto.getName() != null) {
      Recipe findByName = this.recipeRepository.findByName(recipeDto.getName());
      if (findByName != null) {
        return findByName.getId().equals(recipeDto.getId());
      }
    }

    return true;
  }

}
