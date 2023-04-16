package qble2.cookbook.recipe.dtoprojection;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Value;
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "recipes")
@Value
public class RecipeOverviewProjectionDto {

  private UUID id;
  private String name;
  private String description;
  private Integer servings;
  private Long preparationTime;
  private Long cookingTime;
  private LocalDateTime createdAt;
  private LocalDateTime editedAt;

  // private String author;
  // private Set<RecipeTagEnum> tags = new HashSet<>();
  // private List<RecipeIngredient> recipeIngredients = new ArrayList<>();
  // private List<String> instructions = new ArrayList<>();
  // private List<Review> reviews = new ArrayList<>();
  // private byte[] pictures;
  // private Set<User> favoritedBy = new HashSet<>();

  private Boolean isAuthor;
  private Boolean isFavorite;

}
