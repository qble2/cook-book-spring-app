package qble2.cookbook.recipe.specification;

import org.springframework.data.jpa.domain.Specification;
import qble2.cookbook.recipe.model.Recipe;
import qble2.cookbook.recipe.model.Recipe_;
import qble2.cookbook.recipe.request.RecipeSearchFilter;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.Serial;

public class RecipeServingsSpecification implements Specification<Recipe> {

    @Serial
    private static final long serialVersionUID = 1L;

    private RecipeSearchFilter recipeSearchFilter;

    public RecipeServingsSpecification(RecipeSearchFilter recipeSearchFilter) {
        this.recipeSearchFilter = recipeSearchFilter;
    }

    @Override
    public Predicate toPredicate(Root<Recipe> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Number value = recipeSearchFilter.getValueAsNumber();
        if (value == null) {
            return null;
        }

        return switch (recipeSearchFilter.getOperator()) {
            case EQUAL -> cb.equal(root.get(Recipe_.servings), value);
            case GTE -> cb.ge(root.get(Recipe_.servings), value);
            case LTE -> cb.le(root.get(Recipe_.servings), value);

            default -> throw new IllegalArgumentException(
                    "Unexpected value: " + recipeSearchFilter.getOperator());
        };
    }

}
