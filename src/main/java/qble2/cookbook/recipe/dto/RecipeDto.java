package qble2.cookbook.recipe.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;
import org.springframework.validation.annotation.Validated;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import qble2.cookbook.ingredient.dto.IngredientDto;
import qble2.cookbook.recipe.enums.RecipeTagEnum;
import qble2.cookbook.review.dto.ReviewDto;
import qble2.cookbook.user.dto.UserDto;
import qble2.cookbook.validation.UniqueRecipeName;

@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Getter
@Setter
@Accessors(chain = true, fluent = false)
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Relation(collectionRelation = "recipes")
@JsonPropertyOrder({"id", "name", "description", "servings", "preparationTime", "cookingTime",
    "createdAt", "editedAt", "averageRating", "author", "tags", "ingredients", "instructions",
    "reviews"})
@Validated
@UniqueRecipeName(
    groups = {RecipeDto.OnCreateValidationGroup.class, RecipeDto.OnUpdateValidationGroup.class})
public class RecipeDto extends RepresentationModel<RecipeDto> {

  public interface OnCreateValidationGroup {
  }

  public interface OnUpdateValidationGroup {
  }

  @Null(groups = OnCreateValidationGroup.class, message = "{recipe.id.OnCreate.Null.message}")
  @NotNull(groups = OnUpdateValidationGroup.class, message = "{recipe.id.OnUpdate.NotNull.message}")
  @EqualsAndHashCode.Include
  @JsonProperty("id")
  private UUID id;

  @NotBlank(message = "{recipe.name.NotBlank.message}")
  @JsonProperty("name")
  private String name;

  @JsonProperty("description")
  private String description;

  @Min(value = 1, message = "{recipe.servings.Min.message} {value}")
  @JsonProperty("servings")
  private Integer servings;

  @JsonProperty("preparationTime")
  private Long preparationTime;

  @JsonProperty("cookingTime")
  private Long cookingTime;

  @JsonProperty("createdAt")
  private LocalDateTime createdAt;

  @JsonProperty("editedAt")
  private LocalDateTime editedAt;

  @JsonProperty("author")
  private UserDto author;

  @JsonProperty("tags")
  @Builder.Default
  private Set<RecipeTagEnum> tags = new HashSet<>();

  @NotEmpty(groups = {OnCreateValidationGroup.class, OnUpdateValidationGroup.class},
      message = "{recipe.ingredients.NotEmpty.message}")
  @Valid
  @JsonProperty("ingredients")
  @Builder.Default
  private List<IngredientDto> ingredients = new ArrayList<>();

  @NotEmpty(groups = {OnCreateValidationGroup.class, OnUpdateValidationGroup.class},
      message = "{recipe.instructions.NotEmpty.message}")
  @JsonProperty("instructions")
  @Builder.Default
  private List<String> instructions = new ArrayList<>();

  @JsonProperty("reviews")
  @Builder.Default
  private List<ReviewDto> reviews = new ArrayList<>();

  @JsonProperty("pictures")
  private byte[] pictures;

  /** Calculated Properties **/

  @JsonProperty("averageRating")
  private Double averageRating;

}
