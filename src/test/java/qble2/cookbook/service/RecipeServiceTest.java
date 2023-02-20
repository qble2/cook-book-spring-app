package qble2.cookbook.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import qble2.cookbook.exception.ResourceNotFoundException;
import qble2.cookbook.ingredient.IngredientMapper;
import qble2.cookbook.ingredient.IngredientRepository;
import qble2.cookbook.recipe.RecipeMapper;
import qble2.cookbook.recipe.RecipeRepository;
import qble2.cookbook.recipe.RecipeService;
import qble2.cookbook.recipe.dto.RecipeDto;
import qble2.cookbook.recipe.model.Recipe;
import qble2.cookbook.recipe.request.UpdateRecipeRequest;
import qble2.cookbook.user.UserRepository;
import qble2.cookbook.user.UserService;
import qble2.cookbook.user.model.User;

// unit testing
@ExtendWith(MockitoExtension.class) // allows to get rid of the autoCloseable code
class RecipeServiceTest {

  @Mock
  private UserService userService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private RecipeRepository recipeRepository;

  @Mock
  private RecipeMapper recipeMapper;

  @Mock
  private IngredientRepository ingredientRepository;

  @Mock
  private IngredientMapper ingredientMapper;

  private RecipeService recipeService; // underTest

  @BeforeEach
  void setUp() {
    recipeService = new RecipeService(userService, userRepository, recipeRepository, recipeMapper,
        ingredientRepository, ingredientMapper);
  }

  @Test
  void can_getRecipes() {
    // given

    // when
    recipeService.getRecipes(PageRequest.of(1, 5));

    // then
    verify(recipeRepository).findAll();
  }

  @Test
  void given_recipeDoesNotExist_getRecipe_willThrowResourceNotFoundException() {
    // given
    UUID unknownRecipeId = UUID.randomUUID();
    given(recipeRepository.findById(any())).willReturn(Optional.empty());

    // when
    // then
    assertThatThrownBy(() -> recipeService.getRecipe(unknownRecipeId))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void given_recipeExists_getRecipe_willReturnRecipe() {
    // given
    UUID existingRecipeId = UUID.randomUUID();
    given(recipeRepository.findById(any())).willReturn(Optional.of(new Recipe()));
    given(recipeMapper.toDetailedDto(any())).willReturn(new RecipeDto());

    // when
    recipeService.getRecipe(existingRecipeId);

    // then
    InOrder inOrder = Mockito.inOrder(recipeRepository, recipeMapper);
    inOrder.verify(recipeRepository).findById(any());
    inOrder.verify(recipeMapper).toDetailedDto(any());
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void given_recipeExists_getRecipeIngredients_willReturnRecipeIngredients() {
    // given
    UUID existingRecipeId = UUID.randomUUID();
    given(recipeRepository.findByIdAndLoadIngredients(any())).willReturn(Optional.of(new Recipe()));

    // when
    recipeService.getRecipeIngredients(existingRecipeId);

    // then
    InOrder inOrder = Mockito.inOrder(recipeRepository, ingredientMapper);
    inOrder.verify(recipeRepository).findByIdAndLoadIngredients(any());
    inOrder.verify(ingredientMapper).toDtoList(any());
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void given_recipeExists_getRecipeInstructions_willReturnRecipeInstructions() {
    // given
    UUID existingRecipeId = UUID.randomUUID();
    given(recipeRepository.findByIdAndLoadInstructions(any()))
        .willReturn(Optional.of(new Recipe()));

    // when
    recipeService.getRecipeInstructions(existingRecipeId);

    // then
    InOrder inOrder = Mockito.inOrder(recipeRepository);
    inOrder.verify(recipeRepository).findByIdAndLoadInstructions(any());
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void given_recipeExists_getRecipeTags_willReturnRecipeTags() {
    // given
    UUID existingRecipeId = UUID.randomUUID();
    given(recipeRepository.findByIdAndLoadTags(any())).willReturn(Optional.of(new Recipe()));

    // when
    recipeService.getRecipeTags(existingRecipeId);

    // then
    InOrder inOrder = Mockito.inOrder(recipeRepository);
    inOrder.verify(recipeRepository).findByIdAndLoadTags(any());
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void given_userDoesNotExist_createRecipe_willThrowResourceNotFoundException() {
    // given
    String unknownUsername = "unknown";
    RecipeDto recipePayload = RecipeDto.builder().name("recipe 1").build();
    given(userRepository.findByUsername(any())).willThrow(ResourceNotFoundException.class);

    // when
    // then
    assertThatThrownBy(() -> recipeService.createRecipe(unknownUsername, recipePayload))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void given_userExistsAndValidRecipe_createRecipe_willReturnCreatedRecipe() {
    // given
    String existingUsername = "johnwick";
    RecipeDto recipePayload = RecipeDto.builder().name("recipe 1").build();
    given(userRepository.findByUsername(any())).willReturn(Optional.of(new User()));
    given(recipeMapper.toRecipe(any())).willReturn(new Recipe());
    // given(ingredientRepository.findById(any())).willReturn(Optional.of(new Ingredient()));
    given(recipeRepository.save(any())).willReturn(new Recipe());

    // when
    recipeService.createRecipe(existingUsername, recipePayload);

    // then
    InOrder inOrder = Mockito.inOrder(userRepository, recipeRepository, recipeMapper);
    inOrder.verify(userRepository).findByUsername(any());
    inOrder.verify(recipeMapper).toRecipe(any());
    // inOrder.verify(ingredientRepository, times(2)).findById(any());
    inOrder.verify(recipeRepository).save(any());
    inOrder.verify(recipeMapper).toDetailedDto(any());
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void given_userExistsAndValidRecipe_updateRecipe_willReturnCreatedRecipe() {
    // given
    String existingUsername = "johnwick";
    Recipe existingRecipeEntity =
        Recipe.builder().author(User.builder().id(UUID.randomUUID()).build()).build();
    UpdateRecipeRequest recipePayload =
        UpdateRecipeRequest.builder().description("description 1").build();
    given(userRepository.findByUsername(any()))
        .willReturn(Optional.of(existingRecipeEntity.getAuthor()));
    given(recipeRepository.findById(any())).willReturn(Optional.of(existingRecipeEntity));
    given(recipeMapper.toRecipe(any())).willReturn(new Recipe());
    // given(ingredientRepository.findById(any())).willReturn(Optional.of(new Ingredient()));
    given(recipeRepository.save(any())).willReturn(new Recipe());

    // when
    recipeService.updatePartialRecipe(existingUsername, existingRecipeEntity.getId(),
        recipePayload);

    // then
    InOrder inOrder = Mockito.inOrder(userRepository, recipeRepository, recipeMapper);
    inOrder.verify(userRepository).findByUsername(any());
    inOrder.verify(recipeRepository).findById(any());
    inOrder.verify(recipeMapper).toRecipe(any());
    // inOrder.verify(ingredientRepository, times(2)).findById(any());
    inOrder.verify(recipeRepository).save(any());
    inOrder.verify(recipeMapper).toDetailedDto(any());
    inOrder.verifyNoMoreInteractions();
  }

}
