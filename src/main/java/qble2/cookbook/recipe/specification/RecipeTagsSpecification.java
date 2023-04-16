package qble2.cookbook.recipe.specification;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import qble2.cookbook.recipe.enums.RecipeTagEnum;
import qble2.cookbook.recipe.model.Recipe;
import qble2.cookbook.recipe.model.Recipe_;
import qble2.cookbook.recipe.request.RecipeSearchFilter;

import javax.persistence.criteria.*;
import java.io.Serial;
import java.util.Arrays;
import java.util.List;

public class RecipeTagsSpecification implements Specification<Recipe> {

    @Serial
    private static final long serialVersionUID = 1L;

    private RecipeSearchFilter recipeSearchFilter;

    public RecipeTagsSpecification(RecipeSearchFilter recipeSearchFilter) {
        this.recipeSearchFilter = recipeSearchFilter;
    }

    @Override
    public Predicate toPredicate(Root<Recipe> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        if (CollectionUtils.isEmpty(recipeSearchFilter.getValues())) {
            return null;
        }

        return switch (recipeSearchFilter.getOperator()) {
            case ANY -> containsAny(recipeSearchFilter.getValues()).toPredicate(root, query, cb);
            case ALL -> containsAll(recipeSearchFilter.getValues()).toPredicate(root, query, cb);

            default -> throw new IllegalArgumentException(
                    "Unexpected value: " + recipeSearchFilter.getOperator());
        };
    }

    private static Specification<Recipe> containsAny(List<Object> tags) {
        return (root, query, cb) -> {
            SetJoin<Recipe, RecipeTagEnum> recipeTags = root.join(Recipe_.tags, JoinType.INNER);

            return recipeTags.in(convertToEnum(tags));
        };
    }

    private static Specification<Recipe> containsAll(List<Object> tags) {
        return (root, query, cb) -> {
            SetJoin<Recipe, RecipeTagEnum> recipeTags = root.join(Recipe_.tags, JoinType.INNER);
            query.groupBy(root.get(Recipe_.id));
            query.having(cb.equal(cb.count(root.get(Recipe_.id)), tags.size()));

            return recipeTags.in(convertToEnum(tags));
        };
    }

    private static List<RecipeTagEnum> convertToEnum(List<Object> tags) {
        return Arrays.stream(RecipeTagEnum.values()).filter(t -> tags.contains(t.getCode())).toList();
    }

}
