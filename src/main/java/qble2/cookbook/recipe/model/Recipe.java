package qble2.cookbook.recipe.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedSubgraph;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import qble2.cookbook.ingredient.model.Ingredient;
import qble2.cookbook.ingredient.model.UnitOfMeasureEnum;
import qble2.cookbook.recipe.enums.RecipeTagEnum;
import qble2.cookbook.review.model.Review;
import qble2.cookbook.user.model.User;

@Entity(name = "Recipe")
@Table(name = "Recipe")
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@Setter
@Accessors(chain = true, fluent = false)
@Builder
@AllArgsConstructor
@NamedEntityGraph(name = "Recipe-entity-graph-with-ingredients",
    attributeNodes = {
        @NamedAttributeNode(value = "recipeIngredients", subgraph = "recipeIngredients-subgraph")},
    subgraphs = {@NamedSubgraph(name = "recipeIngredients-subgraph",
        attributeNodes = {@NamedAttributeNode("ingredient")})})
@NamedEntityGraph(name = "Recipe-entity-graph-with-instructions",
    attributeNodes = {@NamedAttributeNode(value = "instructions")})
@NamedEntityGraph(name = "Recipe-entity-graph-with-tags",
    attributeNodes = {@NamedAttributeNode(value = "tags")})
@NamedEntityGraph(name = "Recipe-entity-graph-with-reviews",
    attributeNodes = {@NamedAttributeNode(value = "reviews")})
public class Recipe {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  @Type(type = "org.hibernate.type.UUIDCharType")
  @Column(name = "id", updatable = false, nullable = false)
  @EqualsAndHashCode.Include
  private UUID id;

  @Column(name = "name", nullable = false, unique = true)
  private String name;

  @Column(name = "description", nullable = true)
  private String description;

  @Column(name = "servings", nullable = true)
  private Integer servings;

  @Column(name = "preparationTime", nullable = true)
  private Long preparationTime;

  @Column(name = "cookingTime", nullable = true)
  private Long cookingTime;

  @Column(name = "createdAt", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "editedAt", nullable = true)
  private LocalDateTime editedAt;

  @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @JoinColumn(name = "authorId", nullable = false)
  @ToString.Exclude // excluding lazily fetched associations from your toString() to avoid
                    // additional query from hibernate
  private User author;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "RecipeTag") // Hibernate uses plural form by default
  @Column(name = "tag") // Hibernate uses plural form by default
  @OrderBy("ASC")
  // @Enumerated(EnumType.STRING) // using a AttributeConverter instead
  @ToString.Exclude // excluding lazily fetched associations from your toString() to avoid
                    // additional query from hibernate
  @Builder.Default
  private Set<RecipeTagEnum> tags = new HashSet<>();

  // ManyToMany Recipe<->Ingredient relationship split into 2x relationships
  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true,
      mappedBy = "recipe")
  @ToString.Exclude // excluding lazily fetched associations from your toString() to avoid
                    // additional query from hibernate
  @Builder.Default
  private List<RecipeIngredient> recipeIngredients = new ArrayList<>();

  @Lob
  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "RecipeInstruction") // Hibernate uses plural form by default
  @Column(name = "instruction") // Hibernate uses plural form by default
  @ToString.Exclude // excluding lazily fetched associations from your toString() to avoid
                    // additional query from hibernate
  @Builder.Default
  private List<String> instructions = new ArrayList<>();

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true,
      mappedBy = "recipe")
  @ToString.Exclude // excluding lazily fetched associations from your toString() to avoid
                    // additional query from hibernate
  @Builder.Default
  private List<Review> reviews = new ArrayList<>();

  @Lob
  @Basic(fetch = FetchType.LAZY)
  @ToString.Exclude // excluding lazily fetched associations from your toString() to avoid
                    // additional query from hibernate
  private byte[] pictures;

  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "favoriteRecipes")
  @ToString.Exclude // excluding lazily fetched associations from your toString() to avoid
                    // additional query from hibernate
  @Builder.Default
  private Set<User> favoritedBy = new HashSet<>();

  public Recipe addIngredient(Ingredient ingredient, Integer quantity,
      UnitOfMeasureEnum unitOfMeasureEnum) {
    // always call this constructor
    RecipeIngredient recipeIngredient = new RecipeIngredient(this, ingredient);
    recipeIngredient.setQuantity(quantity).setUnitOfMeasure(unitOfMeasureEnum);
    this.recipeIngredients.add(recipeIngredient);

    return this;
  }

  public Recipe removeIngredient(UUID ingredientId) {
    this.recipeIngredients.removeIf(
        recipeIngredient -> recipeIngredient.getId().getIngredientId().equals(ingredientId));

    return this;
  }

  public Recipe addReview(Review review) {
    this.reviews.add(review);
    review.setRecipe(this);

    return this;
  }

  public Recipe removeReview(Review review) {
    this.reviews.remove(review);
    review.setRecipe(null);
    review.setAuthor(null);

    return this;
  }

  public Recipe updateTags(Set<RecipeTagEnum> tags) {
    this.tags.clear();
    this.tags.addAll(tags);

    return this;
  }

  public Recipe updateInstructions(List<String> instructions) {
    this.instructions.clear();
    this.instructions.addAll(instructions);

    return this;
  }

  // public Recipe updateIngredients(List<RecipeIngredient> recipeIngredients) {
  // this.recipeIngredients.clear();
  // recipeIngredients.stream().forEach(recipeIngredient->addIngredient(recipeIngredients, quantity,
  // unitOfMeasureEnum));
  //
  // return this;
  // }

  /** Calculated Properties **/

  // @Formula: custom calculation of a transient property on database level
  @Formula("select cast(avg(rev.rating) as decimal(4, 2))" + " from review rev"
      + " where rev.recipe_id = id")
  private Double averageRating;

  // @PostLoad: custom calculation of a transient property on application level
  // @PostLoad
  // private void postLoad() {
  // // ....
  // }

}
