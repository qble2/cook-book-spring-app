package qble2.cookbook.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.net.URI;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import com.fasterxml.jackson.databind.ObjectMapper;
import qble2.cookbook.exception.ExceptionsControllerAdvice;
import qble2.cookbook.exception.ResourceNotFoundException;
import qble2.cookbook.ingredient.IngredientController;
import qble2.cookbook.ingredient.IngredientRepository;
import qble2.cookbook.ingredient.IngredientService;
import qble2.cookbook.ingredient.dto.IngredientDto;
import qble2.cookbook.utils.TestUtils;

@WebMvcTest(controllers = IngredientController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:ValidationMessages.properties")
class IngredientControllerTest {

  @Autowired
  private Environment env;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private IngredientService ingredientService;

  @MockBean
  private IngredientRepository ingredientRepository; // called during Validation

  /////
  ///// NOMINAL CASES
  /////

  @Test
  void given_none_getIngredients_willReturnIngredients() throws Exception {
    // given
    URI uri = TestUtils.toUri(TestUtils.INGREDIENT_PATH);
    String urlTemplate = TestUtils.toHttpUriString(uri);

    // when
    // then
    String selfLink = urlTemplate;

    this.mockMvc.perform(get(urlTemplate).accept(MediaTypes.HAL_JSON_VALUE)).andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
        .andExpect(status().isOk()) //
        .andExpect(jsonPath("$._links.self.href", is(selfLink)));
  }

  @Test
  void given_ingredientExists_getIngredient_willReturnIngredient() throws Exception {
    // given
    IngredientDto existingIngredient = TestUtils.createIngredient(UUID.randomUUID());
    URI uri =
        TestUtils.toUri(TestUtils.INGREDIENT_PATH + "/{ingredientId}", existingIngredient.getId());
    String urlTemplate = TestUtils.toHttpUriString(uri);
    given(ingredientService.getIngredient(any())).willReturn(existingIngredient);

    // when
    // then
    // String selfLink = urlTemplate;

    final ResultActions resultActions =
        this.mockMvc.perform(get(urlTemplate).accept(MediaTypes.HAL_JSON_VALUE)).andDo(print())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
            .andExpect(status().isOk())
    // TODO BKE links created with MapStruct @AfterMapping are not generated in testing context
    // .andExpect(jsonPath("$._links.self.href", is(selfLink)))
    ;

    verifyResponse(resultActions, existingIngredient);
  }

  @Test
  void given_validIngredient_createIngredient_willReturnCreatedIngredient() throws Exception {
    // given
    URI uri = TestUtils.toUri(TestUtils.INGREDIENT_PATH);
    String urlTemplate = TestUtils.toHttpUriString(uri);
    IngredientDto ingredientPayload = TestUtils.createIngredient(null);
    IngredientDto createdIngredient =
        IngredientDto.builder().id(UUID.randomUUID()).name(ingredientPayload.getName()).build();
    given(ingredientService.createIngredient(any())).willReturn(createdIngredient);

    // when
    // then
    // String selfLink = TestUtils
    // .toHttpUriString(TestUtils.INGREDIENT_PATH + "/{ingredientId}",
    // createdIngredient.getId()).toString();

    final ResultActions resultActions = this.mockMvc
        .perform(post(urlTemplate).accept(MediaTypes.HAL_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(ingredientPayload)))
        .andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
        .andExpect(status().isCreated())
    // TODO BKE links created with MapStruct @AfterMapping are not generated in testing context
    // .andExpect(jsonPath("$._links.self.href", is(selfLink)))
    ;

    verifyResponse(resultActions, ingredientPayload);
  }

  /////
  ///// NON-NOMINAL CASES
  /////

  @Test
  void given_ingredientDoesNotExist_getIngredient_willReturnResourceNotFound() throws Exception {
    // given
    UUID unknownIngredientId = UUID.randomUUID();
    URI uri = TestUtils.toUri(TestUtils.INGREDIENT_PATH + "/{ingredientId}", unknownIngredientId);
    String urlTemplate = TestUtils.toHttpUriString(uri);
    given(ingredientService.getIngredient(any())).willThrow(new ResourceNotFoundException());

    // when
    // then
    final ResultActions resultActions =
        this.mockMvc.perform(get(urlTemplate).accept(MediaTypes.HAL_JSON_VALUE)).andDo(print())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE));

    TestUtils.verifyResponseError(resultActions, uri, status().isNotFound(), HttpStatus.NOT_FOUND,
        ResourceNotFoundException.getFormattedMessage());
  }

  @Test
  void given_InvalidIdProperty_createIngredient_willReturnBadRequest() throws Exception {
    // given
    URI uri = TestUtils.toUri(TestUtils.INGREDIENT_PATH);
    String urlTemplate = TestUtils.toHttpUriString(uri);
    IngredientDto ingredientPayload = TestUtils.createIngredient(UUID.randomUUID());

    // when
    // then
    final ResultActions resultActions = this.mockMvc
        .perform(post(urlTemplate).accept(MediaTypes.HAL_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(ingredientPayload)))
        .andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE));

    TestUtils.verifyResponseError(resultActions, uri, status().isBadRequest(),
        HttpStatus.BAD_REQUEST, ExceptionsControllerAdvice.CONSTRAINT_VIOLATION_MESSAGE,
        env.getProperty("ingredient.id.OnCreate.Null.message"));
  }

  @Test
  void given_nameAlreadyTaken_createIngredient_willReturnBadRequest() throws Exception {
    // given
    URI uri = TestUtils.toUri(TestUtils.INGREDIENT_PATH);
    String urlTemplate = TestUtils.toHttpUriString(uri);
    IngredientDto ingredientPayload = TestUtils.createIngredient(null);
    given(ingredientRepository.existsByName(any())).willReturn(true);

    // when
    // then
    final ResultActions resultActions = this.mockMvc
        .perform(post(urlTemplate).accept(MediaTypes.HAL_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(ingredientPayload)))
        .andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE));

    TestUtils.verifyResponseError(resultActions, uri, status().isBadRequest(),
        HttpStatus.BAD_REQUEST, ExceptionsControllerAdvice.CONSTRAINT_VIOLATION_MESSAGE,
        env.getProperty("ingredient.name.Taken.message"));
  }

  /////
  /////
  /////

  private void verifyResponse(final ResultActions resultActions, IngredientDto ingredient)
      throws Exception {
    resultActions.andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.name", is(ingredient.getName())));
  }
}
