package qble2.cookbook.ingredient;

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
import qble2.cookbook.ingredient.dto.IngredientDto;
import qble2.cookbook.ingredient.model.Ingredient;
import qble2.cookbook.recipe.RecipeController;
import qble2.cookbook.recipe.model.RecipeIngredient;

// disabling Lombok @Buidler is needed to make @AfterMapping work with @MappingTarget
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface IngredientMapper {

  @Named(value = "toIngredientDtoList")
  @IterableMapping(qualifiedByName = "toIngredientDto")
  List<IngredientDto> toDtoList(List<Ingredient> listSource);

  @Named(value = "toIngredientEntityList")
  @IterableMapping(qualifiedByName = "toIngredientEntity")
  List<Ingredient> toEntityList(List<IngredientDto> listSource);

  @Named(value = "toIngredientDto")
  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "id", source = "id")
  @Mapping(target = "name", source = "name")
  @Mapping(target = "defaultUnitOfMeasure", source = "defaultUnitOfMeasure")
  IngredientDto toDto(Ingredient source);

  @AfterMapping
  default void addLinks(Ingredient source, @MappingTarget IngredientDto target) {
    Link selfLink =
        WebMvcLinkBuilder.linkTo(IngredientController.class).slash(source.getId()).withSelfRel();
    target.add(selfLink);
  }

  @Named(value = "toIngredientEntity")
  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "name", source = "name")
  @Mapping(target = "defaultUnitOfMeasure", source = "defaultUnitOfMeasure")
  Ingredient toEntity(IngredientDto source);

  /**
   *
   **/

  @Named(value = "toIngredientDtoListFromRecipeIngredientEntityList")
  @IterableMapping(qualifiedByName = "toIngredientDtoFromRecipeIngredientEntity")
  List<IngredientDto> toDtoListFromRecipeIngredientEntityList(List<RecipeIngredient> listSource);

  @Named(value = "toIngredientDtoFromRecipeIngredientEntity")
  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "id", source = "ingredient.id")
  @Mapping(target = "name", source = "ingredient.name")
  @Mapping(target = "defaultUnitOfMeasure", source = "ingredient.defaultUnitOfMeasure")
  @Mapping(target = "quantity", source = "quantity")
  @Mapping(target = "unitOfMeasure", source = "unitOfMeasure")
  IngredientDto toIngredientDtoFromRecipeIngredientEntity(RecipeIngredient recipeIngredientSource);

  @AfterMapping
  default void addLinks(RecipeIngredient source, @MappingTarget IngredientDto target) {
    Link selfLink = WebMvcLinkBuilder.linkTo(IngredientController.class)
        .slash(source.getIngredient().getId()).withSelfRel();
    target.add(selfLink);

    Link rootLink =
        linkTo(methodOn(IngredientController.class).getIngredients()).withRel("ingredients");
    target.add(rootLink);

    Link recipeLink = linkTo(methodOn(RecipeController.class).getRecipe(source.getRecipe().getId()))
        .withRel("recipe");
    target.add(recipeLink);
  }

}
