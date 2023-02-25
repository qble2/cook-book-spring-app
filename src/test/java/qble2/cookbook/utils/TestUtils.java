package qble2.cookbook.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.web.util.UriComponentsBuilder;
import qble2.cookbook.ingredient.dto.IngredientDto;
import qble2.cookbook.ingredient.model.UnitOfMeasureEnum;
import qble2.cookbook.recipe.RecipeController;
import qble2.cookbook.recipe.dto.RecipeDto;
import qble2.cookbook.review.ReviewController;
import qble2.cookbook.review.dto.ReviewDto;
import qble2.cookbook.role.dto.RoleDto;
import qble2.cookbook.role.model.RoleEnum;
import qble2.cookbook.user.UserController;
import qble2.cookbook.user.dto.UserDto;

public class TestUtils {

  public static final String HOST = "localhost";
  public static final String AUTH_PATH = "/api/auth";
  public static final String ROLES_PATH = "/api/roles";
  public static final String USERS_PATH = "/api/users";
  public static final String RECIPES_PATH = "/api/recipes";
  public static final String INGREDIENTS_PATH = "/api/ingredients";
  public static final String REVIEWS_PATH = "/api/reviews";

  public static URI toUri(String path) {
    return UriComponentsBuilder.fromPath(path).build().toUri();
  }

  public static URI toUri(String path, Object... uriVariables) {
    return UriComponentsBuilder.fromPath(path).build(uriVariables);
  }

  public static String toHttpUriString(URI uri) {
    return UriComponentsBuilder.fromUri(uri).scheme("http").host(HOST).toUriString();
  }

  public static String toHttpUriString(String path, Object... uriVariables) {
    return toHttpUriString(path, null, uriVariables);
  }

  public static String toHttpUriString(String path, Map<String, Object> params,
      Object... uriVariables) {
    UriComponentsBuilder uriComponentsBuilder =
        UriComponentsBuilder.newInstance().scheme("http").host(HOST).path(path);
    if (params != null) {
      params.keySet().forEach(key -> uriComponentsBuilder.queryParam(key, params.get(key)));
    }

    return toUriComponentsBuilder(path, params, uriVariables).scheme("http").host(HOST)
        .build(uriVariables).toString();
  }

  private static UriComponentsBuilder toUriComponentsBuilder(String path,
      Map<String, Object> params, Object... uriVariables) {
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance().path(path);
    if (params != null) {
      params.keySet().forEach(key -> uriComponentsBuilder.queryParam(key, params.get(key)));
    }

    return uriComponentsBuilder;
  }

  //

  public static RoleDto createRole(UUID id, RoleEnum roleEnum) {
    return RoleDto.builder().id(id).name(roleEnum).build();
  }

  public static RoleDto createRoleFrom(UUID id, RoleDto source) {
    return RoleDto.builder().id(id).name(source.getName()).build();
  }

  public static UserDto createUser(UUID id) {
    return UserDto.builder().id(id).username("johnwick").password("password").firstName("john")
        .lastName("wick").email("john_wick@xyz.com").build();
  }

  public static UserDto createUserFrom(UUID id, UserDto source) {
    return UserDto.builder().id(id).username(source.getUsername()).password(null)
        .firstName(source.getFirstName()).lastName(source.getLastName()).email(source.getEmail())
        .build();
  }

  public static IngredientDto createIngredient(UUID id) {
    return IngredientDto.builder().id(id).name("ingredient 1").unitOfMeasure(UnitOfMeasureEnum.GRAM)
        .quantity(1).build();
  }

  public static RecipeDto createRecipe(UUID id, IngredientDto ingredientDto) {
    return RecipeDto.builder().id(id).name("recipe 1").description("description 1")
        .ingredients(List.of(ingredientDto)).instructions(List.of("instruction 1")).build();
  }

  public static RecipeDto createRecipe(UUID id) {
    return RecipeDto.builder().id(id).name("recipe 1").description("description 1")
        .ingredients(List.of(IngredientDto.builder().id(UUID.randomUUID()).name("ingredient 1")
            .unitOfMeasure(UnitOfMeasureEnum.GRAM).quantity(1).build()))
        .instructions(List.of("instruction 1")).build();
  }

  public static RecipeDto createRecipeFrom(UUID id, RecipeDto source) {
    return RecipeDto.builder().id(id).name(source.getName()).description(source.getDescription())
        .ingredients(source.getIngredients()).instructions(source.getInstructions()).build();
  }

  public static ReviewDto createReview(UUID recipeId, UUID userId) {
    return ReviewDto.builder().rating(1).comment("comment").reviewDate(LocalDateTime.now())
        .recipe(createRecipe(UUID.randomUUID())).author(createUser(UUID.randomUUID())).build();
  }

  public static ReviewDto createReviewFrom(UUID recipeId, UUID userId, ReviewDto source) {
    return ReviewDto.builder().rating(source.getRating()).comment(source.getComment())
        .reviewDate(source.getReviewDate()).recipe(createRecipe(recipeId))
        .author(createUser(userId)).build();
  }

  //

  public static void verifyResponseError(final ResultActions resultActions, URI uri,
      ResultMatcher statusResultMatcher, HttpStatus httpStatus, String message, String... details)
      throws Exception {
    verifyResponseError(resultActions, uri, statusResultMatcher, httpStatus, message);
    resultActions.andExpect(jsonPath("$.details", containsInAnyOrder(details)));
  }

  public static void verifyResponseError(final ResultActions resultActions, URI uri,
      ResultMatcher statusResultMatcher, HttpStatus httpStatus, String message) throws Exception {
    resultActions.andExpect(statusResultMatcher).andExpect(jsonPath("$.path", is(uri.toString())))
        .andExpect(jsonPath("$.status", is(httpStatus.value())))
        .andExpect(jsonPath("$.error", is(httpStatus.getReasonPhrase())))
        .andExpect(jsonPath("$.message", is(message)));
  }

  public static void verifyUserLinks(final ResultActions resultActions, UUID userId)
      throws Exception {
    String selfLink = WebMvcLinkBuilder.linkTo(UserController.class).slash(userId).toString();
    String userRecipesLink =
        linkTo(methodOn(RecipeController.class).getUserRecipes(userId, 0, 5)).toString();
    String userReviewsLink =
        linkTo(methodOn(ReviewController.class).getUserReviews(userId)).toString();

    resultActions.andExpect(jsonPath("$._links.self.href", is(selfLink)))
        .andExpect(jsonPath("$._links.recipes.href", is(userRecipesLink)))
        .andExpect(jsonPath("$._links.reviews.href", is(userReviewsLink)));
  }

  public static void verifyRecipeLinks(final ResultActions resultActions, UUID recipeID)
      throws Exception {
    String selfLink = WebMvcLinkBuilder.linkTo(RecipeController.class).slash(recipeID).toString();
    String recipeTagsLink =
        linkTo(methodOn(RecipeController.class).getRecipeTags(recipeID)).toString();
    String recipeIngredientsLink =
        linkTo(methodOn(RecipeController.class).getRecipeIngredients(recipeID)).toString();
    String recipeInstructionsLink =
        linkTo(methodOn(RecipeController.class).getRecipeInstructions(recipeID)).toString();
    String recipeReviewsLink =
        linkTo(methodOn(ReviewController.class).getRecipeReviews(recipeID)).toString();

    resultActions.andExpect(jsonPath("$._links.self.href", is(selfLink)))
        .andExpect(jsonPath("$._links.tags.href", is(recipeTagsLink)))
        .andExpect(jsonPath("$._links.ingredients.href", is(recipeIngredientsLink)))
        .andExpect(jsonPath("$._links.instructions.href", is(recipeInstructionsLink)))
        .andExpect(jsonPath("$._links.reviews.href", is(recipeReviewsLink)));
  }

  //
}
