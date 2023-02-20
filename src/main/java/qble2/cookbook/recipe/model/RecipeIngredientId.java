package qble2.cookbook.recipe.model;

import java.io.Serializable;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import org.hibernate.annotations.Type;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

// composite key
@Embeddable
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Accessors(chain = true, fluent = false)
public class RecipeIngredientId implements Serializable {

  private static final long serialVersionUID = 1L;

  @Column(name = "recipeId")
  @Type(type = "org.hibernate.type.UUIDCharType")
  private UUID recipeId;

  @Column(name = "ingredientId")
  @Type(type = "org.hibernate.type.UUIDCharType")
  private UUID ingredientId;

  public RecipeIngredientId(UUID recipeId, UUID ingredientId) {
    this.recipeId = recipeId;
    this.ingredientId = ingredientId;
  }

}
