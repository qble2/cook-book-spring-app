package qble2.cookbook.recipe.specification;

import java.io.Serial;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import qble2.cookbook.recipe.model.Recipe;
import qble2.cookbook.recipe.model.Recipe_;
import qble2.cookbook.recipe.request.RecipeSearchFilter;

public class RecipeDescriptionSpecification implements Specification<Recipe> {

  @Serial
  private static final long serialVersionUID = 1L;

  private RecipeSearchFilter recipeSearchFilter;

  public RecipeDescriptionSpecification(RecipeSearchFilter recipeSearchFilter) {
    this.recipeSearchFilter = recipeSearchFilter;
  }

  @Override
  public Predicate toPredicate(Root<Recipe> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
    if (StringUtils.isBlank((String) recipeSearchFilter.getValue())) {
      return null;
    }

    return switch (recipeSearchFilter.getOperator()) {
      case LIKE -> cb.like(cb.lower(root.get(Recipe_.description)),
          "%" + recipeSearchFilter.getValue().toString().toLowerCase() + "%");

      default -> throw new IllegalArgumentException(
          "Unexpected value: " + recipeSearchFilter.getOperator());
    };
  }

}
