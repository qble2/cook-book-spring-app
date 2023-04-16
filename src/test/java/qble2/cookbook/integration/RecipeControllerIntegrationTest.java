package qble2.cookbook.integration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.net.URI;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import qble2.cookbook.ingredient.IngredientService;
import qble2.cookbook.ingredient.dto.IngredientDto;
import qble2.cookbook.ingredient.model.UnitOfMeasureEnum;
import qble2.cookbook.recipe.RecipeService;
import qble2.cookbook.recipe.dto.RecipeDto;
import qble2.cookbook.user.UserService;
import qble2.cookbook.user.dto.UserDto;
import qble2.cookbook.utils.TestUtils;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@EnableAutoConfiguration(
    exclude = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // to use @BeforeAll in a non-static setup
class RecipeControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserService userService;

  @Autowired
  private RecipeService recipeService;

  @Autowired
  private IngredientService ingredientService;

  private UserDto user;
  private IngredientDto ingredient;

  @BeforeAll
  void setUp() {
    user = userService.createUser(TestUtils.createUser(null));
    ingredient = ingredientService.createIngredient(TestUtils.createIngredient(null));
  }

  @Test
  void given_recipeExists_getRecipe_willReturnRecipe() throws Exception {
    // given
    RecipeDto recipe = recipeService.createRecipe(user.getUsername(), TestUtils.createRecipe(null,
        ingredient.setQuantity(1).setUnitOfMeasure(UnitOfMeasureEnum.GRAM)));

    URI uri = TestUtils.toUri(TestUtils.RECIPES_PATH + "/{recipeId}", recipe.getId());
    String urlTemplate = TestUtils.toHttpUriString(uri);

    // when
    // then
    final ResultActions resultActions =
        this.mockMvc.perform(get(urlTemplate).accept(MediaTypes.HAL_JSON_VALUE)).andDo(print())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
            .andExpect(status().isOk()) //
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.name", is(recipe.getName())))
            .andExpect(jsonPath("$.description", is(recipe.getDescription())));

    TestUtils.verifyRecipeLinks(resultActions, recipe.getId());
  }

}
