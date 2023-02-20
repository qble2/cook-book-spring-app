package qble2.cookbook.recipe.model;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import qble2.cookbook.ingredient.model.Ingredient;
import qble2.cookbook.ingredient.model.UnitOfMeasureEnum;

@Entity(name = "RecipeIngredient")
@Table(name = "RecipeIngredient")
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@Setter
@Accessors(chain = true, fluent = false)
public class RecipeIngredient {

  // composite primary key
  @EmbeddedId
  @EqualsAndHashCode.Include
  private RecipeIngredientId id;

  @MapsId("recipeId")
  @ManyToOne
  @JoinColumn(name = "recipeId", nullable = false)
  private Recipe recipe;

  @MapsId("ingredientId")
  @ManyToOne
  @JoinColumn(name = "ingredientId", nullable = false)
  private Ingredient ingredient;

  @Column(name = "quantity", nullable = false)
  private Integer quantity = 1;

  @Column(name = "unitOfMeasure", nullable = false)
  @Enumerated(EnumType.STRING)
  private UnitOfMeasureEnum unitOfMeasure;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY,
      mappedBy = "alternativeRecipeIngredient")
  @ToString.Exclude // excluding lazily fetched associations from your toString() to avoid
                    // additional query from hibernate
  private List<Ingredient> alternativeIngredients = new ArrayList<>();

  /**
   * XXX BKE - do not call the noArg constructor to instanciate an entity with a composite primary
   * key, because Hibernate would not be able to set the value of the @EmbededId field via
   * reflection - using @Builder is not ideal because of the composite key -- setting the id
   * property alone is not enough, attributes also need to be set
   */
  public RecipeIngredient(Recipe recipe, Ingredient ingredient) {
    // create primary key
    this.id = new RecipeIngredientId(recipe.getId(), ingredient.getId());

    // initialize attributes
    this.recipe = recipe;
    this.ingredient = ingredient;
  }

}
