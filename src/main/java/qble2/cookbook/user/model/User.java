package qble2.cookbook.user.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedSubgraph;
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
import qble2.cookbook.recipe.model.Recipe;
import qble2.cookbook.review.model.Review;
import qble2.cookbook.role.model.Role;

@Entity(name = "User")
@Table(name = "\"User\"") // user is an sql reserved keyword
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@Setter
@Accessors(chain = true, fluent = false)
@Builder
@AllArgsConstructor
@NamedEntityGraph(name = "User-entity-graph-with-recipes",
    attributeNodes = @NamedAttributeNode("recipes"))
@NamedEntityGraph(name = "User-entity-graph-with-reviews",
    attributeNodes = {@NamedAttributeNode(value = "reviews", subgraph = "reviews-review")},
    subclassSubgraphs = {@NamedSubgraph(name = "reviews-review",
        attributeNodes = {@NamedAttributeNode(value = "review")})})
public class User {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  @Type(type = "org.hibernate.type.UUIDCharType")
  @Column(name = "id", updatable = false, nullable = false)
  @EqualsAndHashCode.Include
  private UUID id;

  @Column(name = "username", nullable = false, unique = true)
  private String username; // used as login

  @Column(name = "password", nullable = false)
  private String password;

  @Column(name = "firstName", nullable = false)
  private String firstName;

  @Column(name = "lastName", nullable = false)
  private String lastName;

  // not used as the primary key, to allow a user to change his email
  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @ManyToMany(fetch = FetchType.EAGER) // has to be always fetched
  @Builder.Default
  private Set<Role> roles = new HashSet<>();

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY,
      mappedBy = "author")
  @JsonManagedReference
  @ToString.Exclude // excluding lazily fetched associations from your toString() to avoid
  // additional query from hibernate
  @Builder.Default
  private List<Recipe> recipes = new ArrayList<>();

  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinTable(name = "FAVORITE_RECIPE", joinColumns = @JoinColumn(name = "USER_ID"),
      inverseJoinColumns = @JoinColumn(name = "RECIPE_ID"))
  @ToString.Exclude // excluding lazily fetched associations from your toString() to avoid
  // additional query from hibernate
  @Builder.Default
  private Set<Recipe> favoriteRecipes = new HashSet<>();

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY,
      mappedBy = "author")
  @ToString.Exclude // excluding lazily fetched associations from your toString() to avoid
  // additional query from hibernate
  @Builder.Default
  private List<Review> reviews = new ArrayList<>();

  public User addRecipe(Recipe recipe) {
    this.recipes.add(recipe);
    recipe.setAuthor(this);

    return this;
  }

  public User addFavoriteRecipe(Recipe recipe) {
    this.favoriteRecipes.add(recipe);
    recipe.getFavoritedBy().add(this);

    return this;
  }

  public User removeFavoriteRecipe(Recipe recipe) {
    this.favoriteRecipes.remove(recipe);
    recipe.getFavoritedBy().remove(this);

    return this;
  }

  public User addRole(Role role) {
    if (!this.roles.contains(role)) {
      this.roles.add(role);
      role.getUsers().add(this);
    }

    return this;
  }

}
