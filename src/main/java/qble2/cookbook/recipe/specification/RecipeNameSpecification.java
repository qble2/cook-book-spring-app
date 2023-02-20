package qble2.cookbook.recipe.specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import qble2.cookbook.recipe.model.Recipe;
import qble2.cookbook.recipe.model.Recipe_;
import qble2.cookbook.recipe.request.RecipeSearchFilter;

public class RecipeNameSpecification implements Specification<Recipe> {

  private static final long serialVersionUID = 1L;

  private RecipeSearchFilter recipeSearchFilter;

  public RecipeNameSpecification(RecipeSearchFilter recipeSearchFilter) {
    this.recipeSearchFilter = recipeSearchFilter;
  }

  @Override
  public Predicate toPredicate(Root<Recipe> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
    if (StringUtils.isNotBlank((String) recipeSearchFilter.getValue())) {
      switch (recipeSearchFilter.getOperator()) {
        case LIKE:
          return cb.like(cb.lower(root.get(Recipe_.name)),
              "%" + recipeSearchFilter.getValue().toString().toLowerCase() + "%");

        default:
          break;
      }
    }

    return null;
  }

}
