package cookbook.service;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.springframework.beans.factory.annotation.Autowired;
import qble2.cookbook.exception.ResourceNotFoundException;
import qble2.cookbook.recipe.RecipeRepository;
import qble2.cookbook.recipe.RecipeService;
import qble2.cookbook.recipe.dto.RecipeDto;
import qble2.cookbook.recipe.model.Recipe;
import qble2.cookbook.review.ReviewMapper;
import qble2.cookbook.review.ReviewRepository;
import qble2.cookbook.review.ReviewService;
import qble2.cookbook.review.dto.ReviewDto;
import qble2.cookbook.review.model.Review;
import qble2.cookbook.user.UserService;

// unit testing
@ExtendWith(MockitoExtension.class) // allows to get rid of the autoCloseable code
class ReviewServiceTest {

  @Autowired
  private UserService userService;

  @Mock
  private RecipeService recipeService;

  @Mock
  private RecipeRepository recipeRepository;

  @Mock
  private ReviewRepository reviewRepository;

  @Mock
  private ReviewMapper reviewMapper;

  private ReviewService reviewService; // underTest

  @BeforeEach
  void setUp() {
    reviewService = new ReviewService(userService, recipeService, recipeRepository,
        reviewRepository, reviewMapper);
  }

  @Test
  void given_reviewDoesNotExist_getReview_willThrowResourceNotFoundException() {
    // given
    UUID unknownRecipeId = UUID.randomUUID();
    UUID unknownAuthorId = UUID.randomUUID();
    given(reviewRepository.findById(any())).willReturn(Optional.empty());

    // when
    // then
    assertThatThrownBy(() -> reviewService.getReview(unknownRecipeId, unknownAuthorId))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void given_reviewExists_getReview_willReturnReview() {
    // given
    Review existingReview = new Review(any(), any()).setComment("comment");
    ReviewDto existingReviewMappedToDto = ReviewDto.builder()
        .recipe(RecipeDto.builder().id(existingReview.getRecipe().getId()).build())
        .comment(existingReview.getComment()).build();
    given(reviewRepository.findById(any())).willReturn(Optional.of(existingReview));
    given(reviewMapper.toDto(any())).willReturn(existingReviewMappedToDto);

    // when
    ReviewDto returnedReviewDto = reviewService.getReview(existingReview.getRecipe().getId(),
        existingReview.getAuthor().getId());

    // then
    verify(reviewRepository).findByRecipe_IdAndAuthor_Id(existingReview.getRecipe().getId(),
        existingReview.getAuthor().getId());
    assertThat(returnedReviewDto).isEqualTo(existingReviewMappedToDto);
  }

  @Test
  void given_userDoesNotExist_getUserReviews_willThrowResourceNotFoundException() {
    // given
    UUID unknownUserId = UUID.randomUUID();
    given(reviewRepository.findByAuthor_Id(any())).willThrow(new ResourceNotFoundException());

    // when
    // then
    assertThatThrownBy(() -> reviewService.getUserReviews(unknownUserId))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void given_userExists_getUserReviews_willReturnUserReviews() {
    // given
    UUID existingUserId = UUID.randomUUID();

    // when
    reviewService.getUserReviews(existingUserId);

    // then
    InOrder inOrder = Mockito.inOrder(reviewMapper);
    inOrder.verify(reviewMapper).toDtoList(any());
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void given_recipeDoesNotExist_getRecipeReviews_willThrowResourceNotFoundException() {
    // given
    UUID unknownRecipeId = UUID.randomUUID();
    given(recipeRepository.findByIdAndLoadReviews(any()))
        .willThrow(new ResourceNotFoundException());

    // when
    // then
    assertThatThrownBy(() -> reviewService.getRecipeReviews(unknownRecipeId))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void given_recipeExists_getRecipeReviews_willReturnRecipeReviews() {
    // given
    UUID existingRecipeId = UUID.randomUUID();
    given(recipeRepository.findByIdAndLoadReviews(any())).willReturn(Optional.of(new Recipe()));

    // when
    reviewService.getRecipeReviews(existingRecipeId);

    // then
    InOrder inOrder = Mockito.inOrder(recipeRepository, reviewMapper);
    inOrder.verify(recipeRepository).findByIdAndLoadReviews(any());
    inOrder.verify(reviewMapper).toDtoList(any());
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void given_userAndRecipeExist_getUserRecipeReview_willReturnUserRecipeReview() {
    // given
    UUID existingUserId = UUID.randomUUID();
    UUID existingRecipeId = UUID.randomUUID();
    given(reviewRepository.findByRecipe_IdAndAuthor_Id(any(), any()))
        .willReturn(Optional.of(new Review()));

    // when
    reviewService.getReview(existingUserId, existingRecipeId);

    // then
    InOrder inOrder = Mockito.inOrder(reviewRepository, reviewMapper);
    inOrder.verify(reviewRepository).findByRecipe_IdAndAuthor_Id(any(), any());
    inOrder.verify(reviewMapper).toDtoList(any());
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void given_userAndRecipeExist_createOrUpdateReview_willReturnCreatedUserRecipeReview() {
    // given
    String existingUsername = "johnwick";
    UUID existingRecipeId = UUID.randomUUID();
    ReviewDto reviewPayload = ReviewDto.builder().rating(5).comment("comment").build();
    given(recipeRepository.findById(any())).willReturn(Optional.of(new Recipe()));
    given(reviewMapper.toReview(any())).willReturn(new Review());

    // when
    reviewService.createReview(existingUsername, existingRecipeId, reviewPayload);

    // then
    InOrder inOrder = Mockito.inOrder(recipeRepository, reviewMapper, reviewRepository);
    inOrder.verify(recipeRepository).findById(any());
    inOrder.verify(reviewMapper).toReview(any());
    inOrder.verify(reviewRepository).save(any());
    inOrder.verify(reviewMapper).toDtoList(any());
    inOrder.verifyNoMoreInteractions();
  }

}
