package qble2.cookbook.recipe.specification;

import org.springframework.data.jpa.domain.Specification;
import qble2.cookbook.recipe.model.Recipe;
import qble2.cookbook.recipe.model.Recipe_;
import qble2.cookbook.recipe.request.RecipeSearchFilter;
import qble2.cookbook.review.model.Review;
import qble2.cookbook.review.model.Review_;

import javax.persistence.criteria.*;
import java.io.Serial;
import java.util.UUID;

public class RecipeAverageRatingSpecification implements Specification<Recipe> {

    @Serial
    private static final long serialVersionUID = 1L;

    private RecipeSearchFilter recipeSearchFilter;

    public RecipeAverageRatingSpecification(RecipeSearchFilter recipeSearchFilter) {
        this.recipeSearchFilter = recipeSearchFilter;
    }

    @Override
    public Predicate toPredicate(Root<Recipe> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Number value = recipeSearchFilter.getValueAsNumber();
        if (value == null) {
            return null;
        }

        return switch (recipeSearchFilter.getOperator()) {
            case GTE -> {
                // XXX BKE this version messes up the count query
                // ListJoin<Recipe, RecipeReview> recipeReviews = root
                // .join(Recipe_.recipeReviews, JoinType.LEFT);
                // Join<RecipeReview, ReviewEntity> review = recipeReviews.join(Review_.review,
                // JoinType.LEFT);
                // query.groupBy(root.get(Recipe_.id));
                // query.having(cb.ge(cb.avg(review.get(Review_.rating)), value));
                //
                // // return cb.and();
                // return cb.equal(cb.literal(1), cb.literal(1));

                // using a subquery
                Subquery<UUID> subQuery = query.subquery(UUID.class);
                Root<Review> subRoot = subQuery.from(Review.class);
                subQuery.select(subRoot.get(Review_.recipe).get(Recipe_.id));
                subQuery.groupBy(subRoot.get(Review_.recipe).get(Recipe_.id));
                subQuery.having(cb.ge(cb.avg(subRoot.get(Review_.rating)), value));

                yield root.get(Recipe_.id).in(subQuery);
            }

            default -> throw new IllegalArgumentException(
                    "Unexpected value: " + recipeSearchFilter.getOperator());
        };
    }

}
