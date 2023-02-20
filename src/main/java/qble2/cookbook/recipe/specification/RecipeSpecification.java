package qble2.cookbook.recipe.specification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import lombok.extern.slf4j.Slf4j;
import qble2.cookbook.recipe.RecipeQuerySorter;
import qble2.cookbook.recipe.model.Recipe;
import qble2.cookbook.recipe.request.RecipeSearchRequest;

@Slf4j
public class RecipeSpecification implements Specification<Recipe> {

  private static final long serialVersionUID = 1L;

  private RecipeSearchRequest recipeSearch;

  public RecipeSpecification(RecipeSearchRequest recipeSearch) {
    this.recipeSearch = recipeSearch;
  }

  /**
   * Spring executes two queries: one to retrieve the results and an additional count query to find
   * out how many pages there are in total. Thus, the Specification instance has to be applied to
   * both of them, hence ….toPredicate(…) is called twice. Thus, logs can appear twice
   */
  @Override
  public Predicate toPredicate(Root<Recipe> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
    List<Predicate> predicates = new ArrayList<>();

    Optional.ofNullable(recipeSearch.getFilters()).orElse(Collections.emptyList()).stream()
        .forEach(recipeSearchFilter -> {
          log.info("recipe search filter:: key: {} , operator: {} , value: {}, values: {}",
              recipeSearchFilter.getKey(), recipeSearchFilter.getOperator(),
              recipeSearchFilter.getValue(), recipeSearchFilter.getValues());

          switch (recipeSearchFilter.getKey()) {

            case USER_RECIPES:
              predicates
                  .add(new IsUserRecipesSpecification(recipeSearchFilter, recipeSearch.getUserId())
                      .toPredicate(root, query, cb));
              break;

            case FAVORITE_RECIPES:
              predicates.add(new IsUserFavoriteRecipesSpecification(recipeSearchFilter,
                  recipeSearch.getUserId()).toPredicate(root, query, cb));
              break;

            case RECIPE_AUTHOR:
              predicates.add(
                  new RecipeAuhtorSpecification(recipeSearchFilter).toPredicate(root, query, cb));
              break;

            case RECIPE_NAME:
              predicates.add(
                  new RecipeNameSpecification(recipeSearchFilter).toPredicate(root, query, cb));
              break;

            case RECIPE_DESCRIPTION:
              predicates.add(new RecipeDescriptionSpecification(recipeSearchFilter)
                  .toPredicate(root, query, cb));
              break;

            case RECIPE_SERVINGS:
              predicates.add(
                  new RecipeServingsSpecification(recipeSearchFilter).toPredicate(root, query, cb));
              break;

            case RECIPE_PREPARATION_TIME:
              predicates.add(new RecipePreparationTimeSpecification(recipeSearchFilter)
                  .toPredicate(root, query, cb));
              break;

            case RECIPE_COOKING_TIME:
              predicates.add(new RecipeCookingTimeSpecification(recipeSearchFilter)
                  .toPredicate(root, query, cb));
              break;

            case RECIPE_TAGS:
              predicates.add(
                  new RecipeTagsSpecification(recipeSearchFilter).toPredicate(root, query, cb));
              break;

            case RECIPE_INGREDIENTS:
              predicates.add(new RecipeIngredientsSpecification(recipeSearchFilter)
                  .toPredicate(root, query, cb));
              break;

            case RECIPE_AVERAGE_RATING:
              predicates.add(new RecipeAverageRatingSpecification(recipeSearchFilter)
                  .toPredicate(root, query, cb));
              break;
          }
        });

    // TODO BKE move sorting outside of this method, since its been called twice
    // sort
    new RecipeQuerySorter().sortQuery(root, query, cb, recipeSearch.getSort());

    return cb.and(predicates.toArray(new Predicate[0]));
  }

}
