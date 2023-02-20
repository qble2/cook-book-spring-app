package qble2.cookbook.recipe;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import lombok.extern.slf4j.Slf4j;
import qble2.cookbook.ingredient.dto.IngredientDto;
import qble2.cookbook.recipe.dto.RecipeDto;
import qble2.cookbook.recipe.dto.RecipesPageDto;
import qble2.cookbook.recipe.enums.RecipeTagEnum;
import qble2.cookbook.recipe.request.RecipeSearchRequest;
import qble2.cookbook.recipe.request.UpdateRecipeRequest;

@RestController
@RequestMapping(path = RecipeController.PATH,
    produces = {MediaTypes.HAL_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
@Validated
@Slf4j
public class RecipeController {

  public static final String PATH = "api/recipes";

  @Autowired
  private RecipeService recipeService;

  @GetMapping
  public ResponseEntity<RecipesPageDto> getRecipes(
      @RequestParam(name = "page", required = false, defaultValue = "0") int page,
      @RequestParam(name = "size", required = false, defaultValue = "5") int size) {
    Pageable pageable = PageRequest.of(page, size);
    RecipesPageDto recipesPageDto = this.recipeService.getRecipes(pageable);

    Link selfLink = linkTo(methodOn(RecipeController.class).getRecipes(page, size)).withSelfRel();
    recipesPageDto.add(selfLink);

    return ResponseEntity.ok().body(recipesPageDto);
  }

  @PostMapping(path = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<RecipesPageDto> getRecipesBySpecification(
      @RequestBody(required = true) RecipeSearchRequest recipeSearch,
      @RequestParam(name = "page", required = false, defaultValue = "0") int page,
      @RequestParam(name = "size", required = false, defaultValue = "5") int size) {
    Pageable pageable = PageRequest.of(page, size);
    RecipesPageDto recipesPageDto =
        this.recipeService.getRecipesBySpecification(recipeSearch, pageable);

    Link selfLink = linkTo(methodOn(RecipeController.class).getRecipes(page, size)).withSelfRel();
    recipesPageDto.add(selfLink);

    return ResponseEntity.ok().body(recipesPageDto);
  }

  @GetMapping(path = "/users/{userId}")
  public ResponseEntity<RecipesPageDto> getUserRecipes(
      @PathVariable(name = "userId", required = true) UUID userId,
      @RequestParam(name = "page", required = false, defaultValue = "0") int page,
      @RequestParam(name = "size", required = false, defaultValue = "5") int size) {
    Pageable pageable = PageRequest.of(page, size);
    RecipesPageDto recipesPageDto = this.recipeService.getUserRecipes(userId, pageable);

    Link selfLink = linkTo(methodOn(RecipeController.class).getRecipes(page, size)).withSelfRel();
    recipesPageDto.add(selfLink);

    return ResponseEntity.ok().body(recipesPageDto);
  }

  @GetMapping(path = "/{recipeId}")
  public ResponseEntity<RecipeDto> getRecipe(
      @PathVariable(name = "recipeId", required = true) UUID recipeId) {
    RecipeDto recipeDto = this.recipeService.getRecipe(recipeId);

    return ResponseEntity.ok().body(recipeDto);
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  @Validated(RecipeDto.OnCreateValidationGroup.class)
  public ResponseEntity<RecipeDto> createRecipe(
      @CurrentSecurityContext(expression = "authentication?.name") String username,
      @Valid @RequestBody(required = true) RecipeDto recipeDto) {
    RecipeDto createdRecipeDto = this.recipeService.createRecipe(username, recipeDto);
    log.info("Recipe {} has been successfully created by user {}", createdRecipeDto.getName(),
        username);

    Link selfLink =
        linkTo(methodOn(RecipeController.class).getRecipe(createdRecipeDto.getId())).withSelfRel();
    createdRecipeDto.add(selfLink);

    final URI uri = MvcUriComponentsBuilder.fromController(RecipeController.class)
        .path("/{recipeId}").buildAndExpand(createdRecipeDto.getId()).toUri();

    return ResponseEntity.created(uri).body(createdRecipeDto);
  }

  @PutMapping(path = "/{recipeId}", consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated() or hasRole('ROLE_ADMIN')")
  @Validated(RecipeDto.OnUpdateValidationGroup.class)
  public ResponseEntity<RecipeDto> updateRecipe(
      @CurrentSecurityContext(expression = "authentication?.name") String username,
      @PathVariable(name = "recipeId", required = true) UUID recipeId,
      @Valid @RequestBody(required = true) RecipeDto recipeDto) {
    RecipeDto updatedRecipeDto = this.recipeService.updateRecipe(username, recipeId, recipeDto);
    log.info("Recipe {} has been successfully updated by user {}", updatedRecipeDto.getName(),
        username);

    return ResponseEntity.ok().body(updatedRecipeDto);
  }

  @DeleteMapping(path = "/{recipeId}", consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated() or hasRole('ROLE_ADMIN')")
  public ResponseEntity<Void> deleteRecipe(
      @CurrentSecurityContext(expression = "authentication?.name") String username,
      @PathVariable(name = "recipeId", required = true) UUID recipeId) {
    this.recipeService.deleteRecipe(username, recipeId);
    log.info("Recipe {} has been successfully deleted by user {}", recipeId, username);

    return ResponseEntity.ok().build();
  }

  /** GET RECIPE PARTIAL INFO */

  @GetMapping(path = "/{recipeId}/tags")
  public CollectionModel<RecipeTagEnum> getRecipeTags(
      @PathVariable(name = "recipeId", required = true) UUID recipeId) {
    Set<RecipeTagEnum> tags = this.recipeService.getRecipeTags(recipeId);

    Link selfLink = linkTo(methodOn(RecipeController.class).getRecipeTags(recipeId)).withSelfRel();
    return CollectionModel.of(tags, selfLink);
  }

  @GetMapping(path = "/{recipeId}/ingredients")
  public CollectionModel<IngredientDto> getRecipeIngredients(
      @PathVariable(name = "recipeId", required = true) UUID recipeId) {
    List<IngredientDto> ingredients = this.recipeService.getRecipeIngredients(recipeId);

    Link selfLink =
        linkTo(methodOn(RecipeController.class).getRecipeIngredients(recipeId)).withSelfRel();
    return CollectionModel.of(ingredients, selfLink);
  }

  @GetMapping(path = "/{recipeId}/instructions")
  public CollectionModel<String> getRecipeInstructions(
      @PathVariable(name = "recipeId", required = true) UUID recipeId) {
    List<String> instructions = this.recipeService.getRecipeInstructions(recipeId);

    Link selfLink =
        linkTo(methodOn(RecipeController.class).getRecipeInstructions(recipeId)).withSelfRel();
    return CollectionModel.of(instructions, selfLink);
  }

  /** UPDATE RECIPE PARTIAL INFO */

  @PutMapping(path = "/{recipeId}/tags", consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated() or hasRole('ROLE_ADMIN')")
  public ResponseEntity<RecipeDto> updateRecipeTags(
      @CurrentSecurityContext(expression = "authentication?.name") String username,
      @PathVariable(name = "recipeId", required = true) UUID recipeId,
      @RequestBody(required = true) Set<RecipeTagEnum> tags) {
    RecipeDto updatedRecipeDto = this.recipeService.updateRecipeTags(username, recipeId, tags);
    log.info("Tags for Recipe {} have been successfully updated by user {}", recipeId, username);

    return ResponseEntity.ok().body(updatedRecipeDto);
  }

  @PostMapping(path = "/{recipeId}/ingredients", consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated() or hasRole('ROLE_ADMIN')")
  public ResponseEntity<RecipeDto> addRecipeIngredient(
      @CurrentSecurityContext(expression = "authentication?.name") String username,
      @PathVariable(name = "recipeId", required = true) UUID recipeId,
      @RequestBody(required = true) IngredientDto ingredientDto) {
    RecipeDto updatedRecipeDto =
        this.recipeService.addRecipeIngredient(username, recipeId, ingredientDto);
    log.info("Ingredient {} has been successfully added to recipe {} by user {}",
        ingredientDto.getId(), recipeId, username);

    return ResponseEntity.ok().body(updatedRecipeDto);
  }

  @PutMapping(path = "/{recipeId}/ingredients/{ingredientId}",
      consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated() or hasRole('ROLE_ADMIN')")
  public ResponseEntity<RecipeDto> updateRecipeIngredient(
      @CurrentSecurityContext(expression = "authentication?.name") String username,
      @PathVariable(name = "recipeId", required = true) UUID recipeId,
      @PathVariable(name = "ingredientId", required = true) UUID ingredientId,
      @RequestBody(required = true) IngredientDto ingredientDto) {
    RecipeDto updatedRecipeDto =
        this.recipeService.updateRecipeIngredient(username, recipeId, ingredientId, ingredientDto);
    log.info("Ingredient {} has been successfully updated for recipe {} by user {}",
        ingredientDto.getId(), recipeId, username);

    return ResponseEntity.ok().body(updatedRecipeDto);
  }

  @DeleteMapping(path = "/{recipeId}/ingredients/{ingredientId}")
  @PreAuthorize("isAuthenticated() or hasRole('ROLE_ADMIN')")
  public ResponseEntity<RecipeDto> removeRecipeIngredient(
      @CurrentSecurityContext(expression = "authentication?.name") String username,
      @PathVariable(name = "recipeId", required = true) UUID recipeId,
      @PathVariable(name = "ingredientId", required = true) UUID ingredientId) {
    RecipeDto updatedRecipeDto =
        this.recipeService.removeRecipeIngredient(username, recipeId, ingredientId);
    log.info("Ingredient {} has been successfully removed from recipe {} by user {}", ingredientId,
        recipeId, username);

    return ResponseEntity.ok().body(updatedRecipeDto);
  }

  @PutMapping(path = "/{recipeId}/instructions", consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated() or hasRole('ROLE_ADMIN')")
  public ResponseEntity<RecipeDto> updateRecipeInstructions(
      @CurrentSecurityContext(expression = "authentication?.name") String username,
      @PathVariable(name = "recipeId", required = true) UUID recipeId,
      @RequestBody(required = true) List<String> instructions) {
    RecipeDto updatedRecipeDto =
        this.recipeService.updateRecipeInstructions(username, recipeId, instructions);
    log.info("Instructions for Recipe {} have been successfully updated by user {}", recipeId,
        username);

    return ResponseEntity.ok().body(updatedRecipeDto);
  }

  /** FAVORITE RECIPES */

  @GetMapping(path = "/users/{userId}/favorites")
  @PreAuthorize("#userId == authentication.principal.id or hasRole('ROLE_ADMIN')")
  public ResponseEntity<RecipesPageDto> getFavoriteRecipes(
      @CurrentSecurityContext(expression = "authentication?.name") String username,
      @RequestParam(name = "page", required = false, defaultValue = "0") int page,
      @RequestParam(name = "size", required = false, defaultValue = "5") int size) {
    Pageable pageable = PageRequest.of(page, size);
    RecipesPageDto recipesPageDto = this.recipeService.getUserFavoriteRecipes(username, pageable);

    Link selfLink = linkTo(methodOn(RecipeController.class).getRecipes(page, size)).withSelfRel();
    recipesPageDto.add(selfLink);

    return ResponseEntity.ok().body(recipesPageDto);
  }

  @PostMapping(path = "/{recipeId}/favorites")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Void> addRecipeToFavorites(
      @CurrentSecurityContext(expression = "authentication?.name") String username,
      @PathVariable(name = "recipeId", required = true) UUID recipeId) {
    this.recipeService.addRecipeToFavorites(username, recipeId);

    return ResponseEntity.ok().build();
  }

  @DeleteMapping(path = "/{recipeId}/favorites")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Void> removeRecipeFromFavorites(
      @CurrentSecurityContext(expression = "authentication?.name") String username,
      @PathVariable(name = "recipeId", required = true) UUID recipeId) {
    this.recipeService.removeRecipeFromFavorites(username, recipeId);

    return ResponseEntity.ok().build();
  }

  /** TODO BKE WIP */

  // TODO BKE keep? relevance? split? rename?
  @PutMapping(path = "/{recipeId}/partial", consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated() or hasRole('ROLE_ADMIN')")
  @Validated(RecipeDto.OnUpdateValidationGroup.class)
  public ResponseEntity<RecipeDto> updatePartialRecipe(
      @CurrentSecurityContext(expression = "authentication?.name") String username,
      @PathVariable(name = "recipeId", required = true) UUID recipeId,
      @Valid @RequestBody(required = true) UpdateRecipeRequest updatePartialRecipeRequest) {
    RecipeDto updatedRecipeDto =
        this.recipeService.updatePartialRecipe(username, recipeId, updatePartialRecipeRequest);
    log.info("Recipe {} has been successfully updated by user {}", updatedRecipeDto.getName(),
        username);

    return ResponseEntity.ok().body(updatedRecipeDto);
  }

}
