package cookbook.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import qble2.cookbook.exception.ResourceNotFoundException;
import qble2.cookbook.ingredient.IngredientMapper;
import qble2.cookbook.ingredient.IngredientRepository;
import qble2.cookbook.ingredient.IngredientService;
import qble2.cookbook.ingredient.dto.IngredientDto;
import qble2.cookbook.ingredient.model.Ingredient;

// unit testing
@ExtendWith(MockitoExtension.class) // allows to get rid of the autoCloseable code
class IngredientServiceTest {

  @Mock
  private IngredientRepository ingredientRepository;

  @Mock
  private IngredientMapper ingredientMapper;

  private IngredientService ingredientService; // underTest

  @BeforeEach
  void setUp() {
    ingredientService = new IngredientService(ingredientRepository, ingredientMapper);
  }

  @Test
  void can_getIngredients() {
    // given

    // when
    ingredientService.getIngredients();

    // then
    verify(ingredientRepository).findAll();
  }

  @Test
  void given_ingredientDoesNotExist_getIngredient_willThrowResourceNotFoundException() {
    // given
    UUID unknownIngredientId = UUID.randomUUID();
    given(ingredientRepository.findById(any())).willReturn(Optional.empty());

    // when
    // then
    assertThatThrownBy(() -> ingredientService.getIngredient(unknownIngredientId))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void given_ingredientExists_getIngredient_willReturnIngredient() {
    // given
    Ingredient existingIngredient =
        Ingredient.builder().id(UUID.randomUUID()).name("ingredient 1").build();
    IngredientDto existingIngredientMappedToDto = IngredientDto.builder()
        .id(existingIngredient.getId()).name(existingIngredient.getName()).build();
    given(ingredientRepository.findById(any())).willReturn(Optional.of(existingIngredient));
    given(ingredientMapper.toDto(any())).willReturn(existingIngredientMappedToDto);

    // when
    IngredientDto returnedIngredientDto =
        ingredientService.getIngredient(existingIngredient.getId());

    // then
    verify(ingredientRepository).findById(existingIngredient.getId());
    assertThat(returnedIngredientDto).isEqualTo(existingIngredientMappedToDto);
  }

  @Test
  void given_validIngredient_createIngredient_willReturnCreatedIngredient() {
    // given
    IngredientDto ingredientPayload = IngredientDto.builder().name("ingredient 1").build();
    Ingredient ingredientPayloadMappedToEntity =
        Ingredient.builder().name(ingredientPayload.getName()).build();
    given(ingredientMapper.toEntity(any())).willReturn(ingredientPayloadMappedToEntity);

    // when
    ingredientService.createIngredient(ingredientPayload);

    // then
    ArgumentCaptor<Ingredient> ingredientArgumentCaptor = ArgumentCaptor.forClass(Ingredient.class);
    verify(ingredientRepository).save(ingredientArgumentCaptor.capture());
    assertThat(ingredientArgumentCaptor.getValue()).isEqualTo(ingredientPayloadMappedToEntity);
  }

}
