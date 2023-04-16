package qble2.cookbook.ingredient;

import java.util.List;
import java.util.UUID;
import javax.transaction.Transactional;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import qble2.cookbook.exception.ResourceNotFoundException;
import qble2.cookbook.ingredient.dto.IngredientDto;
import qble2.cookbook.ingredient.model.Ingredient;

@Service
@Transactional
@Validated
// @AllArgsConstructor needed to be able to inject mocked dependencies for unit testing
@AllArgsConstructor
public class IngredientService {

  @Autowired
  private IngredientRepository ingredientRepository;

  @Autowired
  private IngredientMapper ingredientMapper;

  public List<IngredientDto> getIngredients() {
    List<Ingredient> listOfIngredientEntity = this.ingredientRepository.findAll();

    return this.ingredientMapper.toDtoList(listOfIngredientEntity);
  }

  public IngredientDto getIngredient(UUID ingredientId) {
    return this.ingredientRepository.findById(ingredientId).map(ingredientMapper::toDto)
        .orElseThrow(ResourceNotFoundException::new);
  }

  @Validated(IngredientDto.OnCreateValidationGroup.class)
  public IngredientDto createIngredient(@Valid IngredientDto ingredientDto) {
    Ingredient ingredient = this.ingredientMapper.toEntity(ingredientDto);
    ingredient = this.ingredientRepository.save(ingredient);

    return this.ingredientMapper.toDto(ingredient);
  }

}
