package qble2.cookbook.recipe.specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import qble2.cookbook.recipe.model.Recipe;
import qble2.cookbook.recipe.model.Recipe_;
import qble2.cookbook.recipe.request.RecipeSearchFilter;

public class RecipeCookingTimeSpecification implements Specification<Recipe> {

  private static final long serialVersionUID = 1L;

  private RecipeSearchFilter recipeSearchFilter;

  public RecipeCookingTimeSpecification(RecipeSearchFilter recipeSearchFilter) {
    this.recipeSearchFilter = recipeSearchFilter;
  }

  @Override
  public Predicate toPredicate(Root<Recipe> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
    Number value = recipeSearchFilter.getValueAsNumber();
    if (value != null) {
      switch (recipeSearchFilter.getOperator()) {
        case GTE:
          return cb.ge(root.get(Recipe_.cookingTime), value.longValue());

        case LTE:
          return cb.le(root.get(Recipe_.cookingTime), value.longValue());

        default:
          break;
      }
    }

    return null;
  }

}
