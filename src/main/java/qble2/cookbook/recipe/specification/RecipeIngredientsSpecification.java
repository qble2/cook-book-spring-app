package qble2.cookbook.recipe.specification;

import java.io.Serial;
import java.util.List;
import java.util.UUID;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import qble2.cookbook.ingredient.model.Ingredient_;
import qble2.cookbook.recipe.model.Recipe;
import qble2.cookbook.recipe.model.RecipeIngredient;
import qble2.cookbook.recipe.model.RecipeIngredient_;
import qble2.cookbook.recipe.model.Recipe_;
import qble2.cookbook.recipe.request.RecipeSearchFilter;

public class RecipeIngredientsSpecification implements Specification<Recipe> {

  @Serial
  private static final long serialVersionUID = 1L;

  private RecipeSearchFilter recipeSearchFilter;

  public RecipeIngredientsSpecification(RecipeSearchFilter recipeSearchFilter) {
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
      case NONE -> cb.not(containsAny(recipeSearchFilter.getValues()).toPredicate(root, query, cb));

      default -> throw new IllegalArgumentException(
          "Unexpected value: " + recipeSearchFilter.getOperator());
    };
  }

  private static Specification<Recipe> containsAny(List<Object> listOfIngredientId) {
    return (root, query, cb) -> {
      ListJoin<Recipe, RecipeIngredient> recipeIngredients =
          root.join(Recipe_.recipeIngredients, JoinType.INNER);

      return recipeIngredients.get(RecipeIngredient_.ingredient).get(Ingredient_.id)
          .in(listOfIngredientId);
    };
  }

  public static Specification<Recipe> containsAll(List<Object> listOfIngredientId) {
    // XXX BKE this version messes up the count query
    // return (root, query, cb) -> {
    // ListJoin<Recipe, RecipeIngredient> recipeIngredients = root
    // .join(Recipe_.recipeIngredients, JoinType.INNER);
    // query.groupBy(root.get(Recipe_.id));
    // query.having(cb.equal(cb.count(root.get(Recipe_.id)), listOfIngredientId.size()));
    //
    // return recipeIngredients.get(RecipeIngredient_.ingredient)
    // .get(Ingredient_.id)
    // .in(listOfIngredientId);
    // };

    // using a subquery
    return (root, query, cb) -> {
      Subquery<UUID> subQuery = query.subquery(UUID.class);
      Root<RecipeIngredient> subRoot = subQuery.from(RecipeIngredient.class);
      subQuery.select(subRoot.get(RecipeIngredient_.recipe).get(Recipe_.id));
      subQuery.where(
          subRoot.get(RecipeIngredient_.ingredient).get(Ingredient_.id).in(listOfIngredientId));
      subQuery.having(cb.equal(cb.count(subRoot.get(RecipeIngredient_.recipe).get(Recipe_.id)),
          listOfIngredientId.size()));
      subQuery.groupBy(subRoot.get(RecipeIngredient_.recipe).get(Recipe_.id));

      return root.get(Recipe_.id).in(subQuery);
    };
  }

}
