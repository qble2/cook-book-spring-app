package qble2.cookbook.review;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import lombok.extern.slf4j.Slf4j;
import qble2.cookbook.review.dto.ReviewDto;

@RestController
@RequestMapping(path = ReviewController.PATH,
    produces = {MediaTypes.HAL_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
@Validated
@Slf4j
public class ReviewController {

  public static final String PATH = "api/reviews";

  @Autowired
  private ReviewService reviewService;

  // TODO BKE pagination
  @GetMapping(path = "/recipes/{recipeId}")
  public CollectionModel<ReviewDto> getRecipeReviews(
      @PathVariable(name = "recipeId", required = true) UUID recipeId) {
    List<ReviewDto> reviews = this.reviewService.getRecipeReviews(recipeId);

    Link selfLink =
        linkTo(methodOn(ReviewController.class).getRecipeReviews(recipeId)).withSelfRel();
    return CollectionModel.of(reviews, selfLink);
  }

  // TODO BKE pagination
  @GetMapping(path = "/users/{userId}")
  public CollectionModel<ReviewDto> getUserReviews(
      @PathVariable(name = "userId", required = true) UUID userId) {
    List<ReviewDto> reviews = this.reviewService.getUserReviews(userId);

    Link selfLink = linkTo(methodOn(ReviewController.class).getUserReviews(userId)).withSelfRel();
    return CollectionModel.of(reviews, selfLink);
  }

  @GetMapping(path = "/recipes/{recipeId}/users/{userId}")
  public ResponseEntity<ReviewDto> getReview(
      @CurrentSecurityContext(expression = "authentication?.name") String username,
      @PathVariable(name = "recipeId", required = true) UUID recipeId,
      @PathVariable(name = "userId", required = true) UUID userId) {
    ReviewDto reviewDto = this.reviewService.getReview(recipeId, userId);

    return ResponseEntity.ok().body(reviewDto);
  }

  // FIXME BKE average rating is desynched on client-side after this operation
  // ?return new review metadata which includes updated review + new average rating?
  @PostMapping(path = "/recipes/{recipeId}", consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  @Validated(ReviewDto.OnCreateValidationGroup.class)
  public ResponseEntity<ReviewDto> createReview(
      @CurrentSecurityContext(expression = "authentication?.name") String username,
      @PathVariable(name = "recipeId", required = true) UUID recipeId,
      @Valid @RequestBody(required = true) ReviewDto reviewDto) {
    ReviewDto createdReviewDto = this.reviewService.createReview(username, recipeId, reviewDto);
    Link selfLink = linkTo(
        methodOn(ReviewController.class).getReview(createdReviewDto.getAuthor().getUsername(),
            createdReviewDto.getRecipe().getId(), createdReviewDto.getAuthor().getId()))
                .withSelfRel();
    createdReviewDto.add(selfLink);

    log.info("Review has been successfully created by user {} for recipe {}", username, recipeId);

    final URI uri = MvcUriComponentsBuilder.fromController(ReviewController.class)
        .path("/recipes/{recipeId}/users/{userId}")
        .buildAndExpand(createdReviewDto.getRecipe().getId(), createdReviewDto.getAuthor().getId())
        .toUri();

    return ResponseEntity.created(uri).body(createdReviewDto);
  }

  // FIXME BKE average rating is desynched on client-side after this operation
  // ?return new review metadata which includes updated review + new average rating?
  @PutMapping(path = "/recipes/{recipeId}/users/{userId}",
      consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated() or hasRole('ROLE_ADMIN')")
  @Validated(ReviewDto.OnUpdateValidationGroup.class)
  public ResponseEntity<ReviewDto> updateReview(
      @CurrentSecurityContext(expression = "authentication?.name") String username,
      @PathVariable(name = "recipeId", required = true) UUID recipeId,
      @PathVariable(name = "userId", required = true) UUID userId,
      @Valid @RequestBody(required = true) ReviewDto reviewDto) {
    ReviewDto updatedReviewDto =
        this.reviewService.updateReview(username, recipeId, userId, reviewDto);
    log.info("Review has been successfully updated by user {} for recipe {}", username, recipeId);

    return ResponseEntity.ok().body(updatedReviewDto);
  }

  // FIXME BKE average rating is desynched on client-side after this operation
  // ?return new review metadata which includes updated review + new average rating?
  @DeleteMapping(path = "/recipes/{recipeId}/users/{userId}")
  @PreAuthorize("isAuthenticated() or hasRole('ROLE_ADMIN')")
  public ResponseEntity<Void> deleteRecipeRecipe(
      @CurrentSecurityContext(expression = "authentication?.name") String username,
      @PathVariable(name = "recipeId", required = true) UUID recipeId,
      @PathVariable(name = "userId", required = true) UUID userId) {
    this.reviewService.deleteRecipeRecipe(recipeId, userId);
    log.info("Review has been successfully deleted by user {} for recipe {}", username, recipeId);

    return ResponseEntity.ok().build();
  }

}
