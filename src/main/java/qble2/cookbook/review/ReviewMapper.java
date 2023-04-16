package qble2.cookbook.review;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import qble2.cookbook.recipe.RecipeController;
import qble2.cookbook.recipe.dto.RecipeDto;
import qble2.cookbook.recipe.model.Recipe;
import qble2.cookbook.review.dto.ReviewDto;
import qble2.cookbook.review.model.Review;
import qble2.cookbook.user.UserController;
import qble2.cookbook.user.UserMapper;

// disabling Lombok @Buidler is needed to make @AfterMapping work with @MappingTarget
@Mapper(componentModel = "spring", uses = UserMapper.class,
    builder = @Builder(disableBuilder = true))
public interface ReviewMapper {

  @Named(value = "toReviewDtoList")
  @IterableMapping(qualifiedByName = "toReviewDto")
  List<ReviewDto> toDtoList(List<Review> listSource);

  @Named(value = "toReviewDto")
  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "rating", source = "rating")
  @Mapping(target = "comment", source = "comment")
  @Mapping(target = "reviewDate", source = "reviewDate")
  @Mapping(target = "recipe", source = "recipe", qualifiedByName = "toMinimalRecipeDto_local")
  @Mapping(target = "author", source = "author", qualifiedByName = "toMinimalUserDto")
  ReviewDto toDto(Review source);

  @Named(value = "toMinimalRecipeDto_local")
  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "id", source = "id")
  @Mapping(target = "name", source = "name")
  RecipeDto toMinimalRecipeDto_local(Recipe source);

  @AfterMapping
  default void addLinks(Review source, @MappingTarget ReviewDto resource) {
    Link selfLink =
        WebMvcLinkBuilder.linkTo(ReviewController.class).slash(source.getId()).withSelfRel();
    resource.add(selfLink);

    // no route link for all reviews

    Link authorLink = linkTo(methodOn(UserController.class).getUser(source.getAuthor().getId()))
        .withRel("author");
    resource.add(authorLink);

    Link recipeLink = linkTo(methodOn(RecipeController.class).getRecipe(source.getRecipe().getId()))
        .withRel("recipe");
    resource.add(recipeLink);
  }

  @Named(value = "toReviewEntity")
  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "rating", source = "rating")
  @Mapping(target = "comment", source = "comment")
  @Mapping(target = "reviewDate", source = "reviewDate")
  Review toReview(ReviewDto source);

  @Named(value = "updateReviewEntityFromReviewDto")
  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "comment", source = "comment")
  @Mapping(target = "rating", source = "rating")
  @Mapping(target = "reviewDate", expression = "java(java.time.LocalDateTime.now())")
  void updateReviewEntity(ReviewDto source, @MappingTarget Review target);

}
