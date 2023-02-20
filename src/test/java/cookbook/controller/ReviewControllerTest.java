package cookbook.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import cookbook.utils.TestUtils;
import qble2.cookbook.exception.ExceptionsControllerAdvice;
import qble2.cookbook.exception.ResourceNotFoundException;
import qble2.cookbook.recipe.RecipeService;
import qble2.cookbook.review.ReviewController;
import qble2.cookbook.review.ReviewRepository;
import qble2.cookbook.review.ReviewService;
import qble2.cookbook.review.dto.ReviewDto;
import qble2.cookbook.user.UserService;

@WebMvcTest(controllers = ReviewController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:ValidationMessages.properties")
class ReviewControllerTest {

  @Autowired
  private Environment env;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private UserService userService;

  @MockBean
  private RecipeService recipeService;

  @MockBean
  private ReviewService reviewService;

  @MockBean
  private ReviewRepository reviewRepository; // called during Validation

  /////
  ///// NOMINAL CASES
  /////

  @Test
  void given_recipeExists_getRecipeReviews_willReturnRecipeReviews() throws Exception {
    // given
    UUID existingRecipeId = UUID.randomUUID();
    URI uri = TestUtils.toUri(TestUtils.REVIEW_PATH + "/recipes/{recipeId}", existingRecipeId);
    String urlTemplate = TestUtils.toHttpUriString(uri);
    given(reviewService.getRecipeReviews(any())).willReturn(anyList());

    // when
    // then
    String selfLink = urlTemplate;

    this.mockMvc.perform(get(urlTemplate).accept(MediaTypes.HAL_JSON_VALUE)).andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
        .andExpect(status().isOk()) //
        .andExpect(jsonPath("$._links.self.href", is(selfLink)));
  }

  @Test
  void given_userExists_getUserReviews_willReturnUserReviews() throws Exception {
    // given
    UUID existingUserId = UUID.randomUUID();
    URI uri = TestUtils.toUri(TestUtils.REVIEW_PATH + "/users/{userId}", existingUserId);
    String urlTemplate = TestUtils.toHttpUriString(uri);
    given(reviewService.getUserReviews(any())).willReturn(anyList());

    // when
    // then
    String selfLink = urlTemplate;

    this.mockMvc.perform(get(urlTemplate).accept(MediaTypes.HAL_JSON_VALUE)).andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
        .andExpect(status().isOk()) //
        .andExpect(jsonPath("$._links.self.href", is(selfLink)));
  }

  @Test
  void given_reviewExists_getReview_willReturnReview() throws Exception {
    // given
    UUID recipeId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    ReviewDto existingReview = TestUtils.createReview(recipeId, userId);
    URI uri = TestUtils.toUri(TestUtils.REVIEW_PATH + "/recipes/{recipeId}/users/{userId}",
        existingReview.getAuthor().getId(), existingReview.getRecipe().getId());
    String urlTemplate = TestUtils.toHttpUriString(uri);
    given(reviewService.getReview(any(), any())).willReturn(existingReview);

    // when
    // then
    // String selfLink = urlTemplate;

    this.mockMvc.perform(get(urlTemplate).accept(MediaTypes.HAL_JSON_VALUE)).andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
        .andExpect(status().isOk()) //
        .andExpect(jsonPath("$.rating", is(existingReview.getRating())))
        .andExpect(jsonPath("$.comment", is(existingReview.getComment())))
        .andExpect(jsonPath("$.reviewDate").exists())
    // TODO BKE links created with MapStruct @AfterMapping are not generated in testing context
    // .andExpect(jsonPath("$._links.self.href", is(selfLink)))
    ;
  }

  @Test
  void given_userAndRecipeExist_createReview_willReturnCreatedReview() throws Exception {
    // given
    UUID existingRecipeId = UUID.randomUUID();
    UUID existingUserId = UUID.randomUUID();
    ReviewDto reviewPayload = TestUtils.createReview(null, null);
    ReviewDto createdReview =
        TestUtils.createReviewFrom(existingRecipeId, existingUserId, reviewPayload);
    URI uri = TestUtils.toUri(TestUtils.REVIEW_PATH + "/recipes/{recipeId}", existingRecipeId);
    String urlTemplate = TestUtils.toHttpUriString(uri);
    given(reviewService.createReview(any(), any(), any())).willReturn(createdReview);

    // when
    // then
    String selfLink =
        TestUtils.toHttpUriString(TestUtils.REVIEW_PATH + "/recipes/{recipeId}/users/{userId}",
            createdReview.getRecipe().getId(), createdReview.getAuthor().getId()).toString();

    this.mockMvc
        .perform(post(urlTemplate).accept(MediaTypes.HAL_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(reviewPayload)))
        .andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
        .andExpect(status().isCreated()) //
        .andExpect(jsonPath("$._links.self.href", is(selfLink)));
  }

  @Test
  void given_userAndRecipeExist_updateReview_willReturnUpdatedReview() throws Exception {
    // given
    UUID existingRecipeId = UUID.randomUUID();
    UUID existingUserId = UUID.randomUUID();
    ReviewDto reviewPayload = TestUtils.createReview(existingRecipeId, existingUserId);
    ReviewDto updatedReview =
        TestUtils.createReviewFrom(existingRecipeId, existingUserId, reviewPayload);
    URI uri = TestUtils.toUri(TestUtils.REVIEW_PATH + "/recipes/{recipeId}/users/{userId}",
        existingRecipeId, existingUserId);
    String urlTemplate = TestUtils.toHttpUriString(uri);
    given(reviewService.updateReview(any(), any(), any(), any())).willReturn(updatedReview);

    // when
    // then
    // String selfLink = urlTemplate;

    this.mockMvc
        .perform(put(urlTemplate).accept(MediaTypes.HAL_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(reviewPayload)))
        .andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
        .andExpect(status().isOk())
    // TODO BKE links created with MapStruct @AfterMapping are not generated in testing context
    // .andExpect(jsonPath("$._links.self.href", is(selfLink)))
    ;
  }

  /////
  ///// NON-NOMINAL CASES
  /////

  @Test
  void given_reviewDoesNotExist_getReview_willReturnResourceNotFound() throws Exception {
    // given
    UUID recipeId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    URI uri = TestUtils.toUri(TestUtils.REVIEW_PATH + "/recipes/{recipeId}/users/{userId}",
        recipeId, userId);
    String urlTemplate = TestUtils.toHttpUriString(uri);
    given(reviewService.getReview(any(), any())).willThrow(new ResourceNotFoundException());

    // when
    // then
    final ResultActions resultActions =
        this.mockMvc.perform(get(urlTemplate).accept(MediaTypes.HAL_JSON_VALUE)).andDo(print())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE));

    TestUtils.verifyResponseError(resultActions, uri, status().isNotFound(), HttpStatus.NOT_FOUND,
        ResourceNotFoundException.getFormattedMessage());
  }

  @Test
  void given_missingRating_createReview_willReturnBadRequest() throws Exception {
    // given
    UUID recipeId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    URI uri = TestUtils.toUri(TestUtils.REVIEW_PATH + "/recipes/{recipeId}", recipeId);
    String urlTemplate = TestUtils.toHttpUriString(uri);
    ReviewDto reviewPayload = TestUtils.createReview(recipeId, userId).setRating(null);

    // when
    // then
    final ResultActions resultActions = this.mockMvc
        .perform(post(urlTemplate).accept(MediaTypes.HAL_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(reviewPayload)))
        .andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE));

    TestUtils.verifyResponseError(resultActions, uri, status().isBadRequest(),
        HttpStatus.BAD_REQUEST, ExceptionsControllerAdvice.METHOD_ARGUMENT_NOT_VALID_MESSAGE,
        env.getProperty("review.rating.NotNull.message"));
  }

  /////
  /////
  /////

}
