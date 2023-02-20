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
import qble2.cookbook.user.model.User_;

public class RecipeAuhtorSpecification implements Specification<Recipe> {

  private static final long serialVersionUID = 1L;

  private RecipeSearchFilter recipeSearchFilter;

  public RecipeAuhtorSpecification(RecipeSearchFilter recipeSearchFilter) {
    this.recipeSearchFilter = recipeSearchFilter;
  }

  @Override
  public Predicate toPredicate(Root<Recipe> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
    if (StringUtils.isNotBlank((String) recipeSearchFilter.getValue())) {
      switch (recipeSearchFilter.getOperator()) {
        case EQUAL:
          return cb.equal(root.get(Recipe_.author).get(User_.username),
              recipeSearchFilter.getValue());

        case NOT_EQUAL:
          return cb.notEqual(root.get(Recipe_.author).get(User_.username),
              recipeSearchFilter.getValue());

        case LIKE:
          return cb.like(cb.lower(root.get(Recipe_.author).get(User_.username)),
              "%" + recipeSearchFilter.getValue().toString().toLowerCase() + "%");

        default:
          break;
      }

    }

    return null;
  }

}
