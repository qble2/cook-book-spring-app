package qble2.cookbook.recipe;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import qble2.cookbook.recipe.enums.RecipeSearchSortDirectionEnum;
import qble2.cookbook.recipe.enums.RecipeSearchSortKeyEnum;
import qble2.cookbook.recipe.model.Recipe;
import qble2.cookbook.recipe.model.Recipe_;
import qble2.cookbook.recipe.request.RecipeSearchSort;
import qble2.cookbook.user.model.User_;

@Slf4j
public class RecipeQuerySorter {

  public void sortQuery(Root<Recipe> root, CriteriaQuery<?> query, CriteriaBuilder cb,
      RecipeSearchSort recipeSearchSort) {

    if (recipeSearchSort == null) {
      log.info("No sorting was asked, setting a default one");
      recipeSearchSort = new RecipeSearchSort(RecipeSearchSortKeyEnum.RECIPE_CREATED_AT,
          RecipeSearchSortDirectionEnum.DESC);
    }

    log.info("Sorting by {} {}", recipeSearchSort.getKey(), recipeSearchSort.getDirection());
    switch (recipeSearchSort.getKey()) {
      case RECIPE_AUTHOR:
        // fetch needed to order by author name
        root.fetch(Recipe_.author, JoinType.LEFT);
        orderBy(query, cb, root.get(Recipe_.author).get(User_.username),
            recipeSearchSort.getDirection());
        break;

      case RECIPE_NAME:
        orderBy(query, cb, root.get(Recipe_.name), recipeSearchSort.getDirection());
        break;

      case RECIPE_PREPARATION_TIME:
        orderBy(query, cb, root.get(Recipe_.preparationTime), recipeSearchSort.getDirection());
        break;

      case RECIPE_COOKING_TIME:
        orderBy(query, cb, root.get(Recipe_.cookingTime), recipeSearchSort.getDirection());
        break;

      case RECIPE_AVERAGE_RATING:
        orderBy(query, cb, root.get(Recipe_.averageRating), recipeSearchSort.getDirection());
        break;

      case RECIPE_CREATED_AT:
        orderBy(query, cb, root.get(Recipe_.createdAt), recipeSearchSort.getDirection());
        break;
    }
  }

  private void orderBy(CriteriaQuery<?> query, CriteriaBuilder cb, Path<?> path,
      RecipeSearchSortDirectionEnum direction) {
    switch (direction) {
      case ASC:
        query.orderBy(cb.asc(path));
        break;

      case DESC:
        query.orderBy(cb.desc(path));
        break;

      default:
        throw new IllegalArgumentException("Unexpected value: " + direction);
    }
  }

}
