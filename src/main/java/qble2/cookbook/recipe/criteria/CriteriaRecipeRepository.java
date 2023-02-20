package qble2.cookbook.recipe.criteria;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import qble2.cookbook.recipe.model.Recipe;
import qble2.cookbook.recipe.request.RecipeSearchRequest;

public interface CriteriaRecipeRepository {

  Page<Recipe> findByCriteria(RecipeSearchRequest recipeSearch, Pageable pageable);

}
