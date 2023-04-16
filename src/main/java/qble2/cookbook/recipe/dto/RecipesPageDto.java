package qble2.cookbook.recipe.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.hateoas.RepresentationModel;

@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Getter
@Setter
@Accessors(chain = true, fluent = false)
@NoArgsConstructor
@Builder
@AllArgsConstructor
@JsonPropertyOrder({"currentPage", "totalPages", "totalElements", "recipes"})
public class RecipesPageDto extends RepresentationModel<RecipesPageDto> {

  @Builder.Default
  List<RecipeDto> recipes = new ArrayList<>();

  @Builder.Default
  int currentPage = 0;

  @Builder.Default
  int totalPages = 0;

  @Builder.Default
  long totalElements = 0;

}
