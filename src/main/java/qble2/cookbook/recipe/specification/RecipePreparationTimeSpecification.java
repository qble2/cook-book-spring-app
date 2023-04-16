package qble2.cookbook.recipe.specification;

import java.io.Serial;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import qble2.cookbook.recipe.model.Recipe;
import qble2.cookbook.recipe.model.Recipe_;
import qble2.cookbook.recipe.request.RecipeSearchFilter;

public class RecipePreparationTimeSpecification implements Specification<Recipe> {

  @Serial
  private static final long serialVersionUID = 1L;

  private RecipeSearchFilter recipeSearchFilter;

  public RecipePreparationTimeSpecification(RecipeSearchFilter recipeSearchFilter) {
    this.recipeSearchFilter = recipeSearchFilter;
  }

  @Override
  public Predicate toPredicate(Root<Recipe> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
    Number value = recipeSearchFilter.getValueAsNumber();
    if (value == null) {
      return null;
    }

    return switch (recipeSearchFilter.getOperator()) {
      case GTE -> cb.ge(root.get(Recipe_.preparationTime), value.longValue());
      case LTE -> cb.le(root.get(Recipe_.preparationTime), value.longValue());

      default -> throw new IllegalArgumentException(
          "Unexpected value: " + recipeSearchFilter.getOperator());
    };
  }

}
