package qble2.cookbook.recipe;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import java.util.UUID;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import qble2.cookbook.ingredient.IngredientMapper;
import qble2.cookbook.recipe.dto.RecipeDto;
import qble2.cookbook.recipe.model.Recipe;
import qble2.cookbook.recipe.request.UpdateRecipeRequest;
import qble2.cookbook.review.ReviewController;
import qble2.cookbook.review.ReviewMapper;
import qble2.cookbook.user.UserMapper;

// disabling Lombok @Buidler is needed to make @AfterMapping work with @MappingTarget
@Mapper(componentModel = "spring",
    uses = {UserMapper.class, IngredientMapper.class, ReviewMapper.class},
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    builder = @Builder(disableBuilder = true))
public interface RecipeMapper {

  @Named(value = "toUltraMinimalRecipeDtoList")
  @IterableMapping(qualifiedByName = "toUltraMinimalRecipeDto")
  List<RecipeDto> toUltraMinimalDtoList(List<Recipe> listSource);

  @Named(value = "toUltraMinimalRecipeDto")
  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "id", source = "id")
  @Mapping(target = "name", source = "name")
  RecipeDto toUltraMinimalDto(Recipe source);

  @Named(value = "toMinimalRecipeDtoList")
  @IterableMapping(qualifiedByName = "toMinimalRecipeDto")
  List<RecipeDto> toMinimalDtoList(List<Recipe> listSource);

  /**
   * <pre>
   * No details:
   * ingredients are not mapped
   * instructions are not mapped
   * reviews are not mapped
   * </pre>
   */
  @Named(value = "toMinimalRecipeDto")
  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "id", source = "id")
  @Mapping(target = "name", source = "name")
  @Mapping(target = "description", source = "description")
  @Mapping(target = "servings", source = "servings")
  @Mapping(target = "preparationTime", source = "preparationTime")
  @Mapping(target = "cookingTime", source = "cookingTime")
  @Mapping(target = "createdAt", source = "createdAt")
  @Mapping(target = "editedAt", source = "editedAt")
  @Mapping(target = "author", source = "author", qualifiedByName = "toMinimalUserDto")
  @Mapping(target = "tags", source = "tags")
  // calculated properties
  @Mapping(target = "averageRating", source = "averageRating")
  RecipeDto toMinimalDto(Recipe source);

  @Named(value = "toDetailedRecipeDto")
  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "id", source = "id")
  @Mapping(target = "name", source = "name")
  @Mapping(target = "description", source = "description")
  @Mapping(target = "servings", source = "servings")
  @Mapping(target = "preparationTime", source = "preparationTime")
  @Mapping(target = "cookingTime", source = "cookingTime")
  @Mapping(target = "createdAt", source = "createdAt")
  @Mapping(target = "editedAt", source = "editedAt")
  @Mapping(target = "author", source = "author", qualifiedByName = "toMinimalUserDto")
  @Mapping(target = "tags", source = "tags")
  @Mapping(target = "ingredients", source = "recipeIngredients",
      qualifiedByName = "toIngredientDtoListFromRecipeIngredientEntityList")
  @Mapping(target = "instructions", source = "instructions")
  @Mapping(target = "reviews", source = "reviews", qualifiedByName = "toReviewDtoList")
  @Mapping(target = "pictures", ignore = true)
  // calculated properties
  @Mapping(target = "averageRating", source = "averageRating")
  RecipeDto toDetailedDto(Recipe source);

  @AfterMapping
  default void addLinks(Recipe source, @MappingTarget RecipeDto target) {
    UUID recipeId = source.getId();

    Link selfLink = WebMvcLinkBuilder.linkTo(RecipeController.class).slash(recipeId).withSelfRel();
    target.add(selfLink);

    Link tagsLink =
        linkTo(methodOn(RecipeController.class).getRecipeTags(recipeId)).withRel("tags");
    target.add(tagsLink);

    Link ingredientsLink = linkTo(methodOn(RecipeController.class).getRecipeIngredients(recipeId))
        .withRel("ingredients");
    target.add(ingredientsLink);

    Link instructionsLink = linkTo(methodOn(RecipeController.class).getRecipeInstructions(recipeId))
        .withRel("instructions");
    target.add(instructionsLink);

    Link reviewsLink =
        linkTo(methodOn(ReviewController.class).getRecipeReviews(recipeId)).withRel("reviews");
    target.add(reviewsLink);
  }

  @Named(value = "updateRecipeFromUpdateRecipeRequest")
  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "name", source = "name")
  @Mapping(target = "description", source = "description")
  @Mapping(target = "servings", source = "servings")
  @Mapping(target = "preparationTime", source = "preparationTime")
  @Mapping(target = "cookingTime", source = "cookingTime")
  @Mapping(target = "tags", source = "tags")
  void updateRecipe(UpdateRecipeRequest source, @MappingTarget Recipe target);

  /**
   * <pre>
   * author is not mapped
   * ingredients are not mapped
   * reviews are not mapped
   * </pre>
   */
  @Named(value = "updateRecipeFromRecipeDto")
  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "name", source = "name")
  @Mapping(target = "description", source = "description")
  @Mapping(target = "servings", source = "servings")
  @Mapping(target = "preparationTime", source = "preparationTime")
  @Mapping(target = "cookingTime", source = "cookingTime")
  @Mapping(target = "tags", source = "tags")
  @Mapping(target = "instructions", source = "instructions")
  void updateRecipe(RecipeDto source, @MappingTarget Recipe target);

  // TODO BKE deprecated?

  /**
   * <pre>
   * author is not mapped
   * ingredients are not mapped
   * reviews are not mapped
   * </pre>
   */
  @Named(value = "toRecipeEntity")
  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "name", source = "name")
  @Mapping(target = "description", source = "description")
  @Mapping(target = "servings", source = "servings")
  @Mapping(target = "preparationTime", source = "preparationTime")
  @Mapping(target = "cookingTime", source = "cookingTime")
  @Mapping(target = "tags", source = "tags")
  @Mapping(target = "instructions", source = "instructions")
  Recipe toRecipe(RecipeDto source);

}
