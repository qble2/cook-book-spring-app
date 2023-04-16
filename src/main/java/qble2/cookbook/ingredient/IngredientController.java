package qble2.cookbook.ingredient;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import qble2.cookbook.ingredient.dto.IngredientDto;

@RestController
@RequestMapping(path = IngredientController.PATH,
    produces = {MediaTypes.HAL_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
@Validated
public class IngredientController {

  public static final String PATH = "api/ingredients";

  @Autowired
  private IngredientService ingredientService;

  @GetMapping
  public CollectionModel<IngredientDto> getIngredients() {
    List<IngredientDto> ingredients = this.ingredientService.getIngredients();

    Link selfLink = linkTo(IngredientController.class).withSelfRel();
    return CollectionModel.of(ingredients, selfLink);
  }

  @GetMapping(path = "/{ingredientId}")
  public ResponseEntity<IngredientDto> getIngredient(
      @PathVariable(name = "ingredientId", required = true) UUID ingredientId) {
    IngredientDto ingredientDto = this.ingredientService.getIngredient(ingredientId);

    return ResponseEntity.ok().body(ingredientDto);
  }

  @PostMapping
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @Validated(IngredientDto.OnCreateValidationGroup.class)
  public ResponseEntity<IngredientDto> createIngredient(
      @Valid @RequestBody(required = true) IngredientDto ingredientDto) {
    IngredientDto createdIngredientDto = this.ingredientService.createIngredient(ingredientDto);

    final URI uri = MvcUriComponentsBuilder.fromController(IngredientController.class)
        .path("/{ingredientId}").buildAndExpand(createdIngredientDto.getId()).toUri();

    return ResponseEntity.created(uri).body(createdIngredientDto);
  }

}
