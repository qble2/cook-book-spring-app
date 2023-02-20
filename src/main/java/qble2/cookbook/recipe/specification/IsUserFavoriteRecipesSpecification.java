package qble2.cookbook.recipe.specification;

import java.util.UUID;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.data.jpa.domain.Specification;
import qble2.cookbook.recipe.model.Recipe;
import qble2.cookbook.recipe.model.Recipe_;
import qble2.cookbook.recipe.request.RecipeSearchFilter;
import qble2.cookbook.user.model.User_;

public class IsUserFavoriteRecipesSpecification implements Specification<Recipe> {

  private static final long serialVersionUID = 1L;

  private RecipeSearchFilter recipeSearchFilter;
  private UUID userId;

  public IsUserFavoriteRecipesSpecification(RecipeSearchFilter recipeSearchFilter, UUID userId) {
    this.recipeSearchFilter = recipeSearchFilter;
    this.userId = userId;
  }

  @Override
  public Predicate toPredicate(Root<Recipe> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
    // operator does not matter
    if (userId != null && BooleanUtils.isTrue((Boolean) recipeSearchFilter.getValue())) {
      return cb.equal(root.join(Recipe_.favoritedBy, JoinType.INNER).get(User_.id), userId);
    }

    return null;
  }

}
