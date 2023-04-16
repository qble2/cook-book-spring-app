package qble2.cookbook.recipe.criteria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import javax.persistence.criteria.Subquery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import qble2.cookbook.ingredient.model.Ingredient_;
import qble2.cookbook.recipe.RecipeQuerySorter;
import qble2.cookbook.recipe.enums.RecipeTagEnum;
import qble2.cookbook.recipe.model.Recipe;
import qble2.cookbook.recipe.model.RecipeIngredient;
import qble2.cookbook.recipe.model.RecipeIngredient_;
import qble2.cookbook.recipe.model.Recipe_;
import qble2.cookbook.recipe.request.RecipeSearchRequest;
import qble2.cookbook.review.model.Review;
import qble2.cookbook.review.model.Review_;
import qble2.cookbook.user.model.User;
import qble2.cookbook.user.model.User_;

/**
 * @deprecated use {@link qble2.cookbook.recipe.specification.RecipeSpecification} instead
 */
@Deprecated
@Repository
@Slf4j
public class CriteriaRecipeRepositoryImpl implements CriteriaRecipeRepository {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Page<Recipe> filterByCriteria(RecipeSearchRequest recipeSearch, Pageable pageable) {
    log.info("Page: {} , Size: {}", pageable.getOffset(), pageable.getPageSize());

    CriteriaBuilder cb = this.entityManager.getCriteriaBuilder();

    // count query
    CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
    Root<Recipe> countRoot = buildQuery(cb, countQuery, recipeSearch);
    countQuery.select(cb.countDistinct(countRoot));
    Long count = entityManager.createQuery(countQuery).getSingleResult();
    log.info("Count query: {}", count);

    // actual query
    CriteriaQuery<Recipe> actualQuery = cb.createQuery(Recipe.class);
    Root<Recipe> root = buildQuery(cb, actualQuery, recipeSearch);
    actualQuery.select(root).distinct(true);

    // sort actual query
    new RecipeQuerySorter().sortQuery(root, actualQuery, cb, recipeSearch.getSort());

    TypedQuery<Recipe> query = entityManager.createQuery(actualQuery)
        .setFirstResult((int) pageable.getOffset()).setMaxResults(pageable.getPageSize());

    return new PageImpl<>(query.getResultList(), pageable, count);
  }

  private Root<Recipe> buildQuery(CriteriaBuilder cb, CriteriaQuery<?> query,
      RecipeSearchRequest recipeSearch) {
    Root<Recipe> root = query.from(Recipe.class);

    List<Predicate> predicates = new ArrayList<>();
    Optional.ofNullable(recipeSearch.getFilters()).orElse(Collections.emptyList()).stream()
        .forEach(searchFilter -> {
          log.info("SearchFilter:: key: {} , operator: {} , value: {} , values: {}",
              searchFilter.getKey(), searchFilter.getOperator(), searchFilter.getValue(),
              searchFilter.getValues());

          switch (searchFilter.getKey()) {

            case USER_RECIPES -> {
              if (recipeSearch.getUserId() != null
                  && BooleanUtils.isTrue((Boolean) searchFilter.getValue())) {
                Join<Recipe, User> author = root.join(Recipe_.author, JoinType.INNER);
                predicates.add(cb.equal(author.get(User_.id), recipeSearch.getUserId()));
              }
            }

            case FAVORITE_RECIPES -> {
              if (recipeSearch.getUserId() != null
                  && BooleanUtils.isTrue((Boolean) searchFilter.getValue())) {
                SetJoin<Recipe, User> users = root.join(Recipe_.favoritedBy);
                predicates.add(cb.equal(users.get(User_.id), recipeSearch.getUserId()));
              }
            }

            case RECIPE_AUTHOR -> {
              if (StringUtils.isNotBlank((String) searchFilter.getValue())) {
                switch (searchFilter.getOperator()) {
                  case LIKE -> predicates
                      .add(cb.like(cb.lower(root.get(Recipe_.author).get(User_.username)),
                          "%" + searchFilter.getValue().toString().toLowerCase() + "%"));
                  default -> throw new IllegalArgumentException(
                      "Unexpected value: " + searchFilter.getOperator());
                }
              }
            }

            case RECIPE_NAME -> {
              if (StringUtils.isNotBlank((String) searchFilter.getValue())) {
                switch (searchFilter.getOperator()) {
                  case LIKE -> predicates.add(cb.like(cb.lower(root.get(Recipe_.name)),
                      "%" + searchFilter.getValue().toString().toLowerCase() + "%"));
                  default -> throw new IllegalArgumentException(
                      "Unexpected value: " + searchFilter.getOperator());
                }
              }
            }

            case RECIPE_DESCRIPTION -> {
              if (StringUtils.isNotBlank((String) searchFilter.getValue())) {
                switch (searchFilter.getOperator()) {
                  case LIKE -> predicates.add(cb.like(cb.lower(root.get(Recipe_.description)),
                      "%" + searchFilter.getValue().toString().toLowerCase() + "%"));
                  default -> throw new IllegalArgumentException(
                      "Unexpected value: " + searchFilter.getOperator());
                }
              }
            }

            case RECIPE_SERVINGS -> {
              Number value = searchFilter.getValueAsNumber();
              if (value != null) {
                switch (searchFilter.getOperator()) {
                  case GTE -> predicates.add(cb.ge(root.get(Recipe_.servings), value));
                  case LTE -> predicates.add(cb.le(root.get(Recipe_.servings), value));
                  default -> throw new IllegalArgumentException(
                      "Unexpected value: " + searchFilter.getOperator());
                }
              }
            }

            case RECIPE_PREPARATION_TIME -> {
              Number value = searchFilter.getValueAsNumber();
              if (value != null) {
                switch (searchFilter.getOperator()) {
                  case GTE -> predicates.add(cb.ge(root.get(Recipe_.preparationTime),
                      searchFilter.getValueAsNumber().longValue()));
                  case LTE -> predicates.add(cb.le(root.get(Recipe_.preparationTime),
                      searchFilter.getValueAsNumber().longValue()));
                  default -> throw new IllegalArgumentException(
                      "Unexpected value: " + searchFilter.getOperator());
                }
              }
            }

            case RECIPE_COOKING_TIME -> {
              Number value = searchFilter.getValueAsNumber();
              if (value != null) {
                switch (searchFilter.getOperator()) {
                  case GTE -> predicates.add(cb.ge(root.get(Recipe_.cookingTime),
                      searchFilter.getValueAsNumber().longValue()));
                  case LTE -> predicates.add(cb.le(root.get(Recipe_.cookingTime),
                      searchFilter.getValueAsNumber().longValue()));
                  default -> throw new IllegalArgumentException(
                      "Unexpected value: " + searchFilter.getOperator());
                }
              }
            }

            case RECIPE_TAGS -> {
              List<RecipeTagEnum> listOfTagEnum = parseEnumList(searchFilter.getValues());
              switch (searchFilter.getOperator()) {
                case ANY -> {
                  SetJoin<Recipe, RecipeTagEnum> tags = root.join(Recipe_.tags, JoinType.INNER);
                  predicates.add(tags.in(listOfTagEnum));
                }

                case ALL -> {
                  SetJoin<Recipe, RecipeTagEnum> tags = root.join(Recipe_.tags, JoinType.INNER);
                  query.groupBy(root.get(Recipe_.id));
                  query.having(cb.equal(root.get(Recipe_.id), listOfTagEnum.size()));
                  predicates.add(tags.in(listOfTagEnum));
                }

                default -> throw new IllegalArgumentException(
                    "Unexpected value: " + searchFilter.getOperator());
              }
            }

            case RECIPE_INGREDIENTS -> {
              switch (searchFilter.getOperator()) {
                case ANY -> {
                  ListJoin<Recipe, RecipeIngredient> recipeIngredients =
                      root.join(Recipe_.recipeIngredients, JoinType.INNER);
                  predicates.add(recipeIngredients.get(RecipeIngredient_.ingredient)
                      .get(Ingredient_.id).in(searchFilter.getValues()));
                }

                case ALL -> {
                  // XXX BKE this version messes up the count query
                  // ListJoin<Recipe, RecipeIngredient> recipeIngredients = root
                  // .join(Recipe_.recipeIngredients, JoinType.INNER);
                  // Join<RecipeIngredient, Ingredient> ingredients = recipeIngredients
                  // .join(RecipeIngredient_.ingredient, JoinType.INNER);
                  // query.groupBy(root.get(Recipe_.id));
                  // query.having(
                  // cb.equal(cb.count(root.get(Recipe_.id)), searchFilter.getValues()
                  // .size()));
                  // predicates.add(ingredients.get(Ingredient_.id)
                  // .in(searchFilter.getValues()));

                  // using a subquery
                  Subquery<UUID> subQuery = query.subquery(UUID.class);
                  Root<RecipeIngredient> subRoot = subQuery.from(RecipeIngredient.class);
                  subQuery.select(subRoot.get(RecipeIngredient_.recipe).get(Recipe_.id));
                  subQuery.where(subRoot.get(RecipeIngredient_.ingredient).get(Ingredient_.id)
                      .in(searchFilter.getValues()));
                  subQuery.having(
                      cb.equal(cb.count(subRoot.get(RecipeIngredient_.recipe).get(Recipe_.id)),
                          searchFilter.getValues().size()));
                  subQuery.groupBy(subRoot.get(RecipeIngredient_.recipe).get(Recipe_.id));
                  predicates.add(root.get(Recipe_.id).in(subQuery));
                }

                default -> throw new IllegalArgumentException(
                    "Unexpected value: " + searchFilter.getOperator());
              }
            }

            case RECIPE_AVERAGE_RATING -> {
              Number value = searchFilter.getValueAsNumber();
              if (value != null) {
                switch (searchFilter.getOperator()) {
                  case GTE -> {
                    // XXX BKE this version messes up the count query
                    // ListJoin<Recipe, RecipeReview> recipeReviews = root
                    // .join(Recipe_.recipeReviews, JoinType.LEFT);
                    // Join<RecipeReview, ReviewEntity> reviews = recipeReviews
                    // .join(Review_.review, JoinType.LEFT);
                    //
                    // query.groupBy(root.get(Recipe_.id));
                    // query.having(cb.ge(cb.avg(reviews.get(Review_.rating)), value));

                    // using a subquery
                    Subquery<UUID> subQuery = query.subquery(UUID.class);
                    Root<Review> subRoot = subQuery.from(Review.class);
                    subQuery.select(subRoot.get(Review_.recipe).get(Recipe_.id));
                    subQuery.groupBy(subRoot.get(Review_.recipe).get(Recipe_.id));
                    subQuery.having(cb.ge(cb.avg(subRoot.get(Review_.rating)), value));
                    predicates.add(root.get(Recipe_.id).in(subQuery));
                  }

                  default -> throw new IllegalArgumentException(
                      "Unexpected value: " + searchFilter.getOperator());
                }
              }
            }

            default -> throw new IllegalArgumentException(
                "Unexpected value: " + searchFilter.getKey());
          }
        });

    Predicate and = cb.and(predicates.toArray(new Predicate[0]));
    query.where(and);

    return root;
  }

  private static List<RecipeTagEnum> parseEnumList(List<Object> tags) {
    return Arrays.stream(RecipeTagEnum.values()).filter(t -> tags.contains(t.getCode())).toList();
  }

}
