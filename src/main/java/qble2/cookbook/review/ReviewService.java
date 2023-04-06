package qble2.cookbook.review;

import java.util.List;
import java.util.UUID;
import javax.transaction.Transactional;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import lombok.AllArgsConstructor;
import qble2.cookbook.exception.ResourceNotFoundException;
import qble2.cookbook.recipe.RecipeRepository;
import qble2.cookbook.recipe.RecipeService;
import qble2.cookbook.recipe.model.Recipe;
import qble2.cookbook.review.dto.ReviewDto;
import qble2.cookbook.review.model.Review;
import qble2.cookbook.user.UserService;
import qble2.cookbook.user.model.User;

@Service
@Transactional
@Validated
// @AllArgsConstructor needed to be able to inject mocked dependencies for unit testing
@AllArgsConstructor
public class ReviewService {

  @Autowired
  private UserService userService;

  @Autowired
  private RecipeService recipeService;

  @Autowired
  private RecipeRepository recipeRepository;

  @Autowired
  private ReviewRepository reviewRepository;

  @Autowired
  private ReviewMapper reviewMapper;

  // TODO BKE pagination
  public List<ReviewDto> getRecipeReviews(UUID recipeId) {
    return this.reviewMapper.toDtoList(this.reviewRepository.findByRecipe_Id(recipeId));
  }

  // TODO BKE pagination
  public List<ReviewDto> getUserReviews(UUID userId) {
    return this.reviewMapper.toDtoList(this.reviewRepository.findByAuthor_Id(userId));
  }

  public ReviewDto getReview(UUID recipeId, UUID userId) {
    return this.reviewMapper.toDto(getReviewOrThrow(recipeId, userId));
  }

  @Validated(ReviewDto.OnCreateValidationGroup.class)
  public ReviewDto createReview(String username, UUID recipeId, @Valid ReviewDto reviewDto) {
    User user = this.userService.getUserByUsernameOrThrow(username);
    Recipe recipe = this.recipeService.getRecipeByIdOrThrow(recipeId);
    if (!this.userService.isAdmin() && user.getId().equals(recipe.getAuthor().getId())) {
      throw new AccessDeniedException("You are not allowed to review your own recipe");
    }

    Review review = new Review(recipe, user);
    this.reviewMapper.updateReviewEntity(reviewDto, review);
    this.recipeRepository.save(recipe.addReview(review));

    return this.reviewMapper
        .toDto(this.reviewRepository.findByRecipe_IdAndAuthor_Id(recipeId, user.getId())
            .orElseThrow(ResourceNotFoundException::new));
  }

  @Validated(ReviewDto.OnUpdateValidationGroup.class)
  public ReviewDto updateReview(String username, UUID recipeId, UUID userId,
      @Valid ReviewDto reviewDto) {
    Review review = getReviewAndCheckOwnershipOrThrow(recipeId, userId);
    this.reviewMapper.updateReviewEntity(reviewDto, review);

    return this.reviewMapper.toDto(review);
  }

  public void deleteRecipeRecipe(String username, UUID recipeId, UUID userId) {
    Review review = getReviewAndCheckOwnershipOrThrow(recipeId, userId);
    this.recipeRepository.save(review.getRecipe().removeReview(review));
  }

  private Review getReviewOrThrow(UUID recipeId, UUID userId) {
    return this.reviewRepository.findByRecipe_IdAndAuthor_Id(recipeId, userId)
        .orElseThrow(ResourceNotFoundException::new);
  }

  private Review getReviewAndCheckOwnershipOrThrow(UUID recipeId, UUID userId) {
    Review review = getReviewOrThrow(recipeId, userId);

    // TODO BKE this can be now done at the controller level userId == authentication.id
    if (!this.userService.isAdmin() && !review.getAuthor().getId()
        .equals(this.userService.getUserByIdOrThrow(userId).getId())) {
      throw new AccessDeniedException("You are not allowed to modify this review");
    }

    return review;
  }

}
