package qble2.cookbook.recipe.specification;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.data.jpa.domain.Specification;
import qble2.cookbook.recipe.model.Recipe;
import qble2.cookbook.recipe.model.Recipe_;
import qble2.cookbook.recipe.request.RecipeSearchFilter;
import qble2.cookbook.user.model.User_;

import javax.persistence.criteria.*;
import java.io.Serial;
import java.util.UUID;

public class IsUserFavoriteRecipesSpecification implements Specification<Recipe> {

    @Serial
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
