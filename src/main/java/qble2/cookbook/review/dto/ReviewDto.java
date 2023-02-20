package qble2.cookbook.review.dto;

import java.time.LocalDateTime;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
import qble2.cookbook.recipe.dto.RecipeDto;
import qble2.cookbook.user.dto.UserDto;

@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Getter
@Setter
@Accessors(chain = true, fluent = false)
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Relation(collectionRelation = "reviews")
@JsonPropertyOrder({"id", "rating", "comment", "reviewDate", "author", "recipe"})
@Validated
public class ReviewDto extends RepresentationModel<ReviewDto> {

  public interface OnCreateValidationGroup {
  }

  public interface OnUpdateValidationGroup {
  }

  // @Null(groups = OnCreateOrUpdateValidationGroup.class,
  // message = "{review.id.OnCreate.Null.message}")
  // @NotNull(groups = OnCreateOrUpdateValidationGroup.class,
  // message = "{review.id.OnUpdate.NotNull.message}")
  // @EqualsAndHashCode.Include
  // @JsonProperty("id")
  // private UUID id;

  @NotNull(message = "{review.rating.NotNull.message}")
  @Min(value = 0, message = "{review.rating.Min.message} {value}")
  @Max(value = 5, message = "{review.rating.Max.message} {value}")
  @JsonProperty("rating")
  private Integer rating;

  @NotBlank(message = "{review.comment.NotBlank.message}")
  @JsonProperty("comment")
  private String comment;

  @JsonProperty("reviewDate")
  private LocalDateTime reviewDate;

  private RecipeDto recipe;

  private UserDto author;

}
