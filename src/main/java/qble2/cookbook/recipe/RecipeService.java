package qble2.cookbook.recipe;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.transaction.Transactional;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import qble2.cookbook.exception.InvalidArgumentException;
import qble2.cookbook.exception.ResourceNotFoundException;
import qble2.cookbook.ingredient.IngredientMapper;
import qble2.cookbook.ingredient.IngredientRepository;
import qble2.cookbook.ingredient.dto.IngredientDto;
import qble2.cookbook.ingredient.model.Ingredient;
import qble2.cookbook.recipe.dto.RecipeDto;
import qble2.cookbook.recipe.dto.RecipesPageDto;
import qble2.cookbook.recipe.dtoprojection.RecipeOverviewProjectionDto;
import qble2.cookbook.recipe.enums.RecipeTagEnum;
import qble2.cookbook.recipe.model.Recipe;
import qble2.cookbook.recipe.request.RecipeSearchRequest;
import qble2.cookbook.recipe.request.UpdateRecipeRequest;
import qble2.cookbook.recipe.specification.RecipeSpecification;
import qble2.cookbook.user.UserRepository;
import qble2.cookbook.user.UserService;
import qble2.cookbook.user.model.User;

@Service
@Transactional
@Validated
// @AllArgsConstructor needed to be able to inject mocked dependencies for unit testing
@AllArgsConstructor
public class RecipeService {

  @Autowired
  private UserService userService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RecipeRepository recipeRepository;

  @Autowired
  private RecipeMapper recipeMapper;

  @Autowired
  private IngredientRepository ingredientRepository;

  @Autowired
  private IngredientMapper ingredientMapper;

  public RecipesPageDto getRecipes(Pageable pageable) {
    return toRecipesPage(this.recipeRepository.findAll(pageable));
  }

  /***
   * @deprecated use {@link qble2.cookbook.recipe.RecipeService#getRecipesBySpecification} instead
   */
  @Deprecated
  public RecipesPageDto getRecipesByCriteria(RecipeSearchRequest recipeSearch, Pageable pageable) {
    return toRecipesPage(this.recipeRepository.filterByCriteria(recipeSearch, pageable));
  }

  public RecipesPageDto getRecipesBySpecification(RecipeSearchRequest recipeSearch,
      Pageable pageable) {
    return toRecipesPage(
        this.recipeRepository.findAll(new RecipeSpecification(recipeSearch), pageable));
  }

  public RecipesPageDto getUserRecipes(UUID userId, Pageable pageable) {
    return toRecipesPage(this.recipeRepository.findByAuthor_Id(userId, pageable));
  }

  public RecipeDto getRecipe(UUID recipeId) {
    // TODO BKE is this good practice?
    // @Transactional: lazy fields are loaded into the persistence context when MapStruct mapping is
    // being done
    return this.recipeMapper.toDetailedDto(getRecipeByIdOrThrow(recipeId));
  }

  // TODO BKE ingredients mapping with MapStruct possible?
  public RecipeDto createRecipe(String username, @Valid RecipeDto recipeDto) {
    Recipe recipe = new Recipe();
    this.recipeMapper.updateRecipe(recipeDto, recipe);
    updateRecipeIngredients(recipeDto, recipe);
    recipe.setAuthor(this.userService.getUserByUsernameOrThrow(username))
        .setCreatedAt(LocalDateTime.now());

    return this.recipeMapper.toDetailedDto(this.recipeRepository.save(recipe));
  }

  // TODO BKE ingredients mapping with MapStruct possible?
  public RecipeDto updateRecipe(String username, UUID recipeId, @Valid RecipeDto recipeDto) {
    Recipe recipe = getRecipeByIdAndCheckOwnershipOrThrow(username, recipeId);
    updateRecipeIngredients(recipeDto, recipe);
    this.recipeMapper.updateRecipe(recipeDto, recipe);
    recipe.setEditedAt(LocalDateTime.now());

    return this.recipeMapper.toDetailedDto(this.recipeRepository.save(recipe));
  }

  public void deleteRecipe(String username, UUID recipeId) {
    this.recipeRepository
        .deleteById(getRecipeByIdAndCheckOwnershipOrThrow(username, recipeId).getId());
  }

  /**
   * GET RECIPE PARTIAL INFO
   */

  public Set<RecipeTagEnum> getRecipeTags(UUID recipeId) {
    return this.recipeRepository.findByIdAndLoadTags(recipeId)
        .orElseThrow(ResourceNotFoundException::new).getTags();
  }

  public List<IngredientDto> getRecipeIngredients(UUID recipeId) {
    return this.ingredientMapper.toDtoListFromRecipeIngredientEntityList(
        this.recipeRepository.findByIdAndLoadIngredients(recipeId)
            .orElseThrow(ResourceNotFoundException::new).getRecipeIngredients());
  }

  public List<String> getRecipeInstructions(UUID recipeId) {
    return this.recipeRepository.findByIdAndLoadInstructions(recipeId)
        .orElseThrow(ResourceNotFoundException::new).getInstructions();
  }

  /**
   * UPDATE RECIPE PARTIAL INFO
   */

  public RecipeDto updateRecipeTags(String username, UUID recipeId, Set<RecipeTagEnum> tags) {
    Recipe recipe = getRecipeByIdAndCheckOwnershipOrThrow(username, recipeId);
    recipe.updateTags(tags).setEditedAt(LocalDateTime.now());

    return this.recipeMapper.toDetailedDto(this.recipeRepository.save(recipe));
  }

  // TODO BKE rework?
  public RecipeDto addRecipeIngredient(String username, UUID recipeId,
      IngredientDto ingredientDto) {
    Recipe recipe = getRecipeByIdAndCheckOwnershipOrThrow(username, recipeId);
    Ingredient ingredient = getIngredientByIdOrThrow(ingredientDto.getId());

    recipe.getRecipeIngredients().stream()
        .filter(
            recipeIngredient -> recipeIngredient.getIngredient().getId().equals(ingredient.getId()))
        .findAny().ifPresent(e -> {
          // TODO BKE use a SortedSet instead or a List?
          // TODO BKE custom exception Recipe already contains ingredient
          throw new InvalidArgumentException();
        });

    recipe.addIngredient(ingredient, ingredientDto.getQuantity(), ingredientDto.getUnitOfMeasure());

    return this.recipeMapper.toDetailedDto(this.recipeRepository.save(recipe));
  }

  public RecipeDto updateRecipeIngredient(String username, UUID recipeId, UUID ingredientId,
      IngredientDto ingredientDto) {
    Recipe recipe = getRecipeByIdAndCheckOwnershipOrThrow(username, recipeId);
    recipe.getRecipeIngredients().stream()
        .filter(recipeIngredient -> recipeIngredient.getId().getIngredientId().equals(ingredientId))
        .findFirst().ifPresentOrElse(recipeIngredient -> {
          recipeIngredient.setQuantity(ingredientDto.getQuantity())
              .setUnitOfMeasure(ingredientDto.getUnitOfMeasure());
        }, ResourceNotFoundException::new);
    recipe.setEditedAt(LocalDateTime.now());

    return this.recipeMapper.toDetailedDto(this.recipeRepository.save(recipe));
  }

  public RecipeDto removeRecipeIngredient(String username, UUID recipeId, UUID ingredientId) {
    Recipe recipe = getRecipeByIdAndCheckOwnershipOrThrow(username, recipeId)
        .removeIngredient(ingredientId).setEditedAt(LocalDateTime.now());

    return this.recipeMapper.toDetailedDto(this.recipeRepository.save(recipe));
  }

  public RecipeDto updateRecipeInstructions(String username, UUID recipeId,
      List<String> instructions) {
    Recipe recipe = getRecipeByIdAndCheckOwnershipOrThrow(username, recipeId)
        .updateInstructions(instructions).setEditedAt(LocalDateTime.now());

    return this.recipeMapper.toDetailedDto(this.recipeRepository.save(recipe));
  }

  /**
   * FAVORITE RECIPES
   */

  public RecipesPageDto getUserFavoriteRecipes(String username, Pageable pageable) {
    return toRecipesPage(this.recipeRepository.findByFavoritedBy_Username(username, pageable));
  }

  public void addRecipeToFavorites(String username, UUID recipeId) {
    User user = this.userService.getUserByUsernameOrThrow(username)
        .addFavoriteRecipe(getRecipeByIdOrThrow(recipeId));

    this.userRepository.save(user);
  }

  public void removeRecipeFromFavorites(String username, UUID recipeId) {
    User user = this.userService.getUserByUsernameOrThrow(username)
        .removeFavoriteRecipe(getRecipeByIdOrThrow(recipeId));

    this.userRepository.save(user);
  }

  /**
   * EXPERIMENTAL
   */

  public List<RecipeOverviewProjectionDto> getRecipesUsingDtoProjection(String username) {
    return this.recipeRepository
        .findByDtoProjection(this.userService.getUserByUsernameOrThrow(username).getId());
  }

  public Page<RecipeOverviewProjectionDto> getRecipesUsingDtoProjectionWithPagination(
      String username, Pageable pageable) {
    return this.recipeRepository.findByDtoProjectionWithPagination(
        this.userService.getUserByUsernameOrThrow(username).getId(), pageable);
  }

  /**
   * TODO BKE WIP
   */

  @Validated(RecipeDto.OnUpdateValidationGroup.class)
  public RecipeDto updatePartialRecipe(String username, UUID recipeId,
      @Valid UpdateRecipeRequest updateRecipeRequest) {
    Recipe recipe = getRecipeByIdAndCheckOwnershipOrThrow(username, recipeId);
    this.recipeMapper.updateRecipe(updateRecipeRequest, recipe);
    recipe.setEditedAt(LocalDateTime.now());

    return this.recipeMapper.toDetailedDto(this.recipeRepository.save(recipe));
  }

  /**
   *
   */

  // TODO BKE ingredients mapping with MapStruct possible?
  private void updateRecipeIngredients(RecipeDto sourceRecipeDto,
      Recipe targetToUpdateRecipeEntity) {
    targetToUpdateRecipeEntity.getRecipeIngredients().clear(); // handling removed ingredients
    sourceRecipeDto.getIngredients().stream()
        .forEach(ingredientDto -> targetToUpdateRecipeEntity.addIngredient(
            getIngredientByIdOrThrow(ingredientDto.getId()), ingredientDto.getQuantity(),
            ingredientDto.getUnitOfMeasure()));
  }

  public Recipe getRecipeByIdOrThrow(UUID recipeId) {
    return this.recipeRepository.findById(recipeId).orElseThrow(ResourceNotFoundException::new);
  }

  private Recipe getRecipeByIdAndCheckOwnershipOrThrow(String username, UUID recipeId) {
    Recipe recipe = getRecipeByIdOrThrow(recipeId);
    if (!this.userService.isAdmin() && !recipe.getAuthor().getId()
        .equals(this.userService.getUserByUsernameOrThrow(username).getId())) {
      throw new AccessDeniedException("You are not allowed to modify this recipe");
    }

    return recipe;
  }

  private Ingredient getIngredientByIdOrThrow(UUID ingredientId) {
    return this.ingredientRepository.findById(ingredientId)
        .orElseThrow(ResourceNotFoundException::new);
  }

  private RecipesPageDto toRecipesPage(Page<Recipe> page) {
    return RecipesPageDto.builder().recipes(this.recipeMapper.toMinimalDtoList(page.getContent()))
        .currentPage(page.getNumber()).totalPages(page.getTotalPages())
        .totalElements(page.getTotalElements()).build();
  }

}
