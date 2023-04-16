package qble2.cookbook.recipe;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import qble2.cookbook.recipe.dto.RecipesPageDto;
import qble2.cookbook.recipe.dtoprojection.RecipeOverviewProjectionDto;
import qble2.cookbook.recipe.request.RecipeSearchRequest;

@RestController
@RequestMapping(path = ExperimentalRecipeController.PATH,
    produces = {MediaTypes.HAL_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
@Validated
public class ExperimentalRecipeController {

  public static final String PATH = "api/experimental/recipes";

  @Autowired
  private RecipeService recipeService;

  @GetMapping
  public CollectionModel<RecipeOverviewProjectionDto> getRecipesByDtoProjectionWithPagination(
      @CurrentSecurityContext(expression = "authentication?.name") String username,
      @RequestParam(name = "page", required = false, defaultValue = "0") int page,
      @RequestParam(name = "size", required = false, defaultValue = "5") int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<RecipeOverviewProjectionDto> listOfRecipeOverview =
        this.recipeService.getRecipesUsingDtoProjectionWithPagination(username, pageable);

    Link selfLink = linkTo(methodOn(ExperimentalRecipeController.class)
        .getRecipesByDtoProjectionWithPagination(username, page, size)).withSelfRel();
    return CollectionModel.of(listOfRecipeOverview, selfLink);
  }

  @SuppressWarnings("deprecation")
  @PostMapping(path = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<RecipesPageDto> getRecipesByCriteria(
      @RequestParam(name = "page", required = false, defaultValue = "0") int page,
      @RequestParam(name = "size", required = false, defaultValue = "5") int size,
      @RequestBody(required = true) RecipeSearchRequest recipeSearch) {
    Pageable pageable = PageRequest.of(page, size);

    RecipesPageDto recipesPageDto = this.recipeService.getRecipesByCriteria(recipeSearch, pageable);

    return ResponseEntity.ok().body(recipesPageDto);
  }

}
