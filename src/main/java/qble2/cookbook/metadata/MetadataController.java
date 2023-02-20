package qble2.cookbook.metadata;

import java.util.List;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import qble2.cookbook.ingredient.IngredientService;
import qble2.cookbook.ingredient.dto.IngredientDto;
import qble2.cookbook.ingredient.model.UnitOfMeasureEnum;
import qble2.cookbook.metadata.dto.MetadataDto;
import qble2.cookbook.recipe.enums.RecipeTagEnum;

@RestController
@RequestMapping(path = MetadataController.PATH,
    produces = {MediaTypes.HAL_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
@Validated
public class MetadataController {

  public static final String PATH = "api/metadata";

  @Autowired
  private IngredientService ingredientService;

  @GetMapping
  public ResponseEntity<MetadataDto> getMetadata() {
    List<RecipeTagEnum> tags = Stream.of(RecipeTagEnum.values())
        .sorted((t1, t2) -> t1.toString().compareTo(t2.toString())).toList();
    List<UnitOfMeasureEnum> unitOfMeasures = Stream.of(UnitOfMeasureEnum.values()).toList();
    List<IngredientDto> ingredients = this.ingredientService.getIngredients();
    MetadataDto availableIngredientsDto = MetadataDto.builder().availableTags(tags)
        .availableUnitOfMeasures(unitOfMeasures).availableIngredients(ingredients).build();

    return ResponseEntity.ok().body(availableIngredientsDto);
  }
}
