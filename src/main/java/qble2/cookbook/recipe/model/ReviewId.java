package qble2.cookbook.recipe.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;

// composite key
@Embeddable
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Accessors(chain = true, fluent = false)
public class ReviewId implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @Column(name = "recipeId")
  @Type(type = "org.hibernate.type.UUIDCharType")
  private UUID recipeId;

  @Column(name = "authorId")
  @Type(type = "org.hibernate.type.UUIDCharType")
  private UUID authorId;

  public ReviewId(UUID recipeId, UUID authorId) {
    this.recipeId = recipeId;
    this.authorId = authorId;
  }

}
