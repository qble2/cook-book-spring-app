package qble2.cookbook.ingredient.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import qble2.cookbook.recipe.model.RecipeIngredient;

@Entity(name = "Ingredient")
@Table(name = "Ingredient")
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@Setter
@Accessors(chain = true, fluent = false)
@Builder
@AllArgsConstructor
public class Ingredient {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  @Type(type = "org.hibernate.type.UUIDCharType")
  @Column(name = "id", updatable = false, nullable = false)
  @EqualsAndHashCode.Include
  private UUID id;

  // we do not use the name property as the primary key, to be able to rename an ingredient
  @Column(name = "name", nullable = false, unique = true)
  private String name;

  @Column(name = "defaultUnitOfMeasure", nullable = false)
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private UnitOfMeasureEnum defaultUnitOfMeasure = UnitOfMeasureEnum.GRAM;

  // ManyToMany Recipe<->Ingredient relationship split into 2x relationships
  @OneToMany(fetch = FetchType.LAZY, mappedBy = "ingredient")
  @ToString.Exclude // excluding lazily fetched associations from your toString() to avoid
  // additional query from hibernate
  @Builder.Default
  private List<RecipeIngredient> recipeIngredients = new ArrayList<>();

  @ManyToOne
  @JoinColumn(name = "recipeId") // composite key part 1/2
  @JoinColumn(name = "ingredientId") // composite key part 1/2
  @ToString.Exclude // excluding lazily fetched associations from your toString() to avoid
  // additional query from hibernate
  private RecipeIngredient alternativeRecipeIngredient;

}
