package cookbook.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import com.fasterxml.jackson.databind.ObjectMapper;
import cookbook.utils.TestUtils;
import qble2.cookbook.exception.ExceptionsControllerAdvice;
import qble2.cookbook.exception.ResourceNotFoundException;
import qble2.cookbook.recipe.RecipeController;
import qble2.cookbook.recipe.RecipeRepository;
import qble2.cookbook.recipe.RecipeService;
import qble2.cookbook.recipe.dto.RecipeDto;
import qble2.cookbook.recipe.dto.RecipesPageDto;
import qble2.cookbook.recipe.model.Recipe;

@WebMvcTest(controllers = RecipeController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:ValidationMessages.properties")
class RecipeControllerTest {

  @Autowired
  private Environment env;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private RecipeService recipeService;

  @MockBean
  private RecipeRepository recipeRepository; // called during Validation

  /////
  ///// NOMINAL CASES
  /////

  @Test
  void given_none_getRecipes_willReturnRecipes() throws Exception {
    // given
    URI uri = TestUtils.toUri(TestUtils.RECIPE_PATH);
    String urlTemplate = TestUtils.toHttpUriString(uri);
    int page = 0;
    int size = 5;
    Pageable pageable = PageRequest.of(page, size);
    RecipesPageDto recipesPageDto = RecipesPageDto.builder()
        .recipes(List.of(TestUtils.createRecipe(UUID.randomUUID()))).build();
    given(recipeService.getRecipes(pageable)).willReturn(recipesPageDto);

    // when
    // then
    String selfLink = TestUtils.toHttpUriString(TestUtils.RECIPE_PATH,
        new TreeMap<>(Map.of("page", page, "size", size)));

    this.mockMvc.perform(get(urlTemplate).accept(MediaTypes.HAL_JSON_VALUE)).andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
        .andExpect(status().isOk()) //
        .andExpect(jsonPath("$._links.self.href", is(selfLink)));
  }

  @Test
  void given_recipeExists_getRecipe_willReturnRecipe() throws Exception {
    // given
    UUID existingRecipeId = UUID.randomUUID();
    RecipeDto existingRecipe = TestUtils.createRecipe(existingRecipeId);
    URI uri = TestUtils.toUri(TestUtils.RECIPE_PATH + "/{recipeId}", existingRecipeId);
    String urlTemplate = TestUtils.toHttpUriString(uri);
    given(recipeService.getRecipe(any())).willReturn(existingRecipe);

    // when
    // then
    this.mockMvc.perform(get(urlTemplate).accept(MediaTypes.HAL_JSON_VALUE)).andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(existingRecipe.getId().toString())))
        .andExpect(jsonPath("$.name", is(existingRecipe.getName())));
  }

  // TODO BKE check returned tags
  @Test
  void given_recipeExists_getRecipeTags_willReturnStatusOk() throws Exception {
    UUID existingRecipeId = UUID.randomUUID();
    RecipeDto existingRecipe = TestUtils.createRecipe(existingRecipeId);
    URI uri = TestUtils.toUri(TestUtils.RECIPE_PATH + "/{recipeId}/tags", existingRecipeId);
    String urlTemplate = TestUtils.toHttpUriString(uri);
    given(recipeService.getRecipe(any())).willReturn(existingRecipe);

    // when
    // then
    String selfLink = urlTemplate;

    this.mockMvc.perform(get(urlTemplate).accept(MediaTypes.HAL_JSON_VALUE)).andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
        .andExpect(status().isOk()) //
        .andExpect(jsonPath("$._links.self.href", is(selfLink)));
  }

  // TODO BKE check returned ingredients
  @Test
  void given_recipeExists_getRecipeIngredients_willReturnRecipeIngredients() throws Exception {
    // given
    UUID existingRecipeId = UUID.randomUUID();
    RecipeDto existingRecipe = TestUtils.createRecipe(existingRecipeId);
    URI uri = TestUtils.toUri(TestUtils.RECIPE_PATH + "/{recipeId}/ingredients", existingRecipeId);
    String urlTemplate = TestUtils.toHttpUriString(uri);
    given(recipeService.getRecipe(any())).willReturn(existingRecipe);

    // when
    // then
    String selfLink = urlTemplate;

    this.mockMvc.perform(get(urlTemplate).accept(MediaTypes.HAL_JSON_VALUE)).andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
        .andExpect(status().isOk()) //
        .andExpect(jsonPath("$._links.self.href", is(selfLink)));
  }

  // TODO BKE check returned instructions
  @Test
  void given_recipeExists_getRecipeInstructions_willReturnRecipeInstructions() throws Exception {
    // given
    UUID existingRecipeId = UUID.randomUUID();
    RecipeDto existingRecipe = TestUtils.createRecipe(existingRecipeId);
    URI uri = TestUtils.toUri(TestUtils.RECIPE_PATH + "/{recipeId}/instructions", existingRecipeId);
    String urlTemplate = TestUtils.toHttpUriString(uri);
    given(recipeService.getRecipe(any())).willReturn(existingRecipe);

    // when
    // then
    String selfLink = urlTemplate;

    this.mockMvc.perform(get(urlTemplate).accept(MediaTypes.HAL_JSON_VALUE)).andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
        .andExpect(status().isOk()) //
        .andExpect(jsonPath("$._links.self.href", is(selfLink)));
  }

  @Test
  void given_userExistsAndValidRecipe_createRecipe_willReturnCreatedRecipe() throws Exception {
    // given
    RecipeDto recipePayload = TestUtils.createRecipe(null);
    RecipeDto createdRecipe = TestUtils.createRecipeFrom(UUID.randomUUID(), recipePayload);
    URI uri = TestUtils.toUri(TestUtils.RECIPE_PATH);
    String urlTemplate = TestUtils.toHttpUriString(uri);
    given(recipeService.createRecipe(any(), any())).willReturn(createdRecipe);

    // when
    // then
    // String selfLink =
    // TestUtils.toHttpUriString(TestUtils.RECIPE_PATH + "/{recipeId}",
    // createdRecipe.getId()).toString();
    // String tagsLink = TestUtils
    // .toHttpUriString(TestUtils.RECIPE_PATH + "/{recipeId}/tags",
    // createdRecipe.getId()).toString();
    // String ingredientsLink = TestUtils
    // .toHttpUriString(TestUtils.RECIPE_PATH + "/{recipeId}/ingredients",
    // createdRecipe.getId()).toString();
    // String instructionsLink =
    // TestUtils.toHttpUriString(TestUtils.RECIPE_PATH + "/{recipeId}/instructions",
    // createdRecipe.getId())
    // .toString();

    this.mockMvc
        .perform(post(urlTemplate).accept(MediaTypes.HAL_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(recipePayload)))
        .andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(createdRecipe.getId().toString())))
        .andExpect(jsonPath("$.name", is(recipePayload.getName())))
    // TODO BKE links created with MapStruct @AfterMapping are not generated in testing context
    // .andExpect(jsonPath("$._links.self.href", is(selfLink)))
    // .andExpect(jsonPath("$._links.tags.href", is(tagsLink)))
    // .andExpect(jsonPath("$._links.ingredients.href", is(ingredientsLink)))
    // .andExpect(jsonPath("$._links.instructions.href", is(instructionsLink)))
    ;
  }

  @Test
  void given_userExistsAndValidRecipe_updateRecipe_willReturnUpdatedRecipe() throws Exception {
    // given
    UUID existingRecipeId = UUID.randomUUID();
    RecipeDto recipePayload = TestUtils.createRecipe(existingRecipeId);
    RecipeDto updatedRecipe = TestUtils.createRecipeFrom(existingRecipeId, recipePayload);
    URI uri = TestUtils.toUri(TestUtils.RECIPE_PATH + "/{recipeId}", existingRecipeId);
    String urlTemplate = TestUtils.toHttpUriString(uri);
    given(recipeService.updateRecipe(any(), any(), any())).willReturn(updatedRecipe);

    // when
    // then
    // String selfLink =
    // TestUtils.toHttpUriString(TestUtils.RECIPE_PATH + "/{recipeId}",
    // existingRecipeId).toString();
    // String tagsLink =
    // TestUtils.toHttpUriString(TestUtils.RECIPE_PATH + "/{recipeId}/tags",
    // existingRecipeId).toString();
    // String ingredientsLink = TestUtils
    // .toHttpUriString(TestUtils.RECIPE_PATH + "/{recipeId}/ingredients",
    // existingRecipeId).toString();
    // String instructionsLink = TestUtils
    // .toHttpUriString(TestUtils.RECIPE_PATH + "/{recipeId}/instructions",
    // existingRecipeId).toString();

    this.mockMvc
        .perform(put(urlTemplate).accept(MediaTypes.HAL_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(recipePayload)))
        .andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
        .andExpect(status().isOk()) //
        .andExpect(jsonPath("$.id", is(existingRecipeId.toString())))
        .andExpect(jsonPath("$.name", is(recipePayload.getName())))
    // TODO BKE links created with MapStruct @AfterMapping are not generated in testing context
    // .andExpect(jsonPath("$._links.self.href", is(selfLink)))
    // .andExpect(jsonPath("$._links.tags.href", is(tagsLink)))
    // .andExpect(jsonPath("$._links.ingredients.href", is(ingredientsLink)))
    // .andExpect(jsonPath("$._links.instructions.href", is(instructionsLink)))
    ;
  }

  /////
  ///// NON-NOMINAL CASES
  /////

  @Test
  void given_recipeDoesNotExist_getRecipe_willReturnResourceNotFound() throws Exception {
    // given
    UUID unknownRecipeId = UUID.randomUUID();
    URI uri = TestUtils.toUri(TestUtils.RECIPE_PATH + "/{recipeId}", unknownRecipeId);
    String urlTemplate = TestUtils.toHttpUriString(uri);
    given(recipeService.getRecipe(any())).willThrow(new ResourceNotFoundException());

    // when
    // then
    final ResultActions resultActions =
        this.mockMvc.perform(get(urlTemplate).accept(MediaTypes.HAL_JSON_VALUE)).andDo(print())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE));

    TestUtils.verifyResponseError(resultActions, uri, status().isNotFound(), HttpStatus.NOT_FOUND,
        ResourceNotFoundException.getFormattedMessage());
  }

  @Test
  void given_userDoesNotExist_createRecipe_willReturnResourceNotFound() throws Exception {
    // given
    RecipeDto recipePayload = TestUtils.createRecipe(null);
    URI uri = TestUtils.toUri(TestUtils.RECIPE_PATH);
    String urlTemplate = TestUtils.toHttpUriString(uri);
    given(recipeService.createRecipe(any(), any())).willThrow(new ResourceNotFoundException());

    // when
    // then
    final ResultActions resultActions = this.mockMvc
        .perform(post(urlTemplate).accept(MediaTypes.HAL_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(recipePayload)))
        .andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE));

    TestUtils.verifyResponseError(resultActions, uri, status().isNotFound(), HttpStatus.NOT_FOUND,
        ResourceNotFoundException.getFormattedMessage());
  }

  @Test
  void given_userExistsAndInvalidRecipeIdProperty_createRecipe_willReturnBadRequest()
      throws Exception {
    // given
    RecipeDto recipePayload = TestUtils.createRecipe(UUID.randomUUID());
    URI uri = TestUtils.toUri(TestUtils.RECIPE_PATH);
    String urlTemplate = TestUtils.toHttpUriString(uri);

    // when
    // then
    final ResultActions resultActions = this.mockMvc
        .perform(post(urlTemplate).accept(MediaTypes.HAL_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(recipePayload)))
        .andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE));

    TestUtils.verifyResponseError(resultActions, uri, status().isBadRequest(),
        HttpStatus.BAD_REQUEST, ExceptionsControllerAdvice.CONSTRAINT_VIOLATION_MESSAGE,
        env.getProperty("recipe.id.OnCreate.Null.message"));
  }

  @Test
  void given_userExistsAndRecipeNameAlreadyTaken_createRecipe_willReturnStatusConflict()
      throws Exception {
    // given
    RecipeDto recipePayload = TestUtils.createRecipe(null);
    Recipe existingRecipe =
        Recipe.builder().id(UUID.randomUUID()).name(recipePayload.getName()).build();
    URI uri = TestUtils.toUri(TestUtils.RECIPE_PATH);
    String urlTemplate = TestUtils.toHttpUriString(uri);
    given(recipeRepository.findByName(recipePayload.getName())).willReturn(existingRecipe);

    // when
    // then
    final ResultActions resultActions = this.mockMvc
        .perform(post(urlTemplate).accept(MediaTypes.HAL_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(recipePayload)))
        .andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE));

    TestUtils.verifyResponseError(resultActions, uri, status().isBadRequest(),
        HttpStatus.BAD_REQUEST, ExceptionsControllerAdvice.CONSTRAINT_VIOLATION_MESSAGE,
        env.getProperty("recipe.name.Taken.message"));
  }

  /////
  /////
  /////

}
