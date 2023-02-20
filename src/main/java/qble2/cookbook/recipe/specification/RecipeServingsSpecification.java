package qble2.cookbook.recipe.specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import qble2.cookbook.recipe.model.Recipe;
import qble2.cookbook.recipe.model.Recipe_;
import qble2.cookbook.recipe.request.RecipeSearchFilter;

public class RecipeServingsSpecification implements Specification<Recipe> {

  private static final long serialVersionUID = 1L;

  private RecipeSearchFilter recipeSearchFilter;

  public RecipeServingsSpecification(RecipeSearchFilter recipeSearchFilter) {
    this.recipeSearchFilter = recipeSearchFilter;
  }

  @Override
  public Predicate toPredicate(Root<Recipe> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
    Number value = recipeSearchFilter.getValueAsNumber();
    if (value != null) {
      switch (recipeSearchFilter.getOperator()) {
        case EQUAL:
          return cb.equal(root.get(Recipe_.servings), value);

        case GTE:
          return cb.ge(root.get(Recipe_.servings), value);

        case LTE:
          return cb.le(root.get(Recipe_.servings), value);

        default:
          break;
      }
    }

    return null;
  }

}
