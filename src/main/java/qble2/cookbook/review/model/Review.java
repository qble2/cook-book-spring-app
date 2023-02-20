package qble2.cookbook.review.model;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import qble2.cookbook.recipe.model.Recipe;
import qble2.cookbook.recipe.model.ReviewId;
import qble2.cookbook.user.model.User;

@Entity(name = "Review")
@Table(name = "Review")
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@Setter
@Accessors(chain = true, fluent = false)
public class Review {

  // @Id
  // @GeneratedValue(generator = "UUID")
  // @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  // @Type(type = "org.hibernate.type.UUIDCharType")
  // @Column(name = "id", updatable = false, nullable = false)
  // @EqualsAndHashCode.Include
  // private UUID id;

  // composite key
  @EmbeddedId
  @EqualsAndHashCode.Include
  private ReviewId id;

  @Column(name = "rating", nullable = false)
  private Integer rating = 0;

  @Lob
  @Column(name = "comment", nullable = true)
  private String comment;

  @Column(name = "reviewDate", nullable = false)
  private LocalDateTime reviewDate;

  @MapsId("recipeId")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "recipeId", nullable = false)
  @ToString.Exclude // excluding lazily fetched associations from your toString() to avoid
                    // additional query from hibernate
  private Recipe recipe;

  @MapsId("authorId")
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "authorId", nullable = false)
  @ToString.Exclude // excluding lazily fetched associations from your toString() to avoid
                    // additional query from hibernate
  private User author;

  /**
   * XXX BKE - do not call the noArg constructor to instanciate an entity with a composite primary
   * key, because Hibernate would not be able to set the value of the @EmbededId field via
   * reflection - using @Builder is not ideal because of the composite key -- setting the id
   * property alone is not enough, attributes also need to be set
   */
  public Review(Recipe recipe, User user) {
    this(recipe, user, null, null);
  }

  public Review(Recipe recipe, User user, Integer rating, String comment) {
    // create primary key
    this.id = new ReviewId(recipe.getId(), user.getId());

    // initialize attributes
    this.recipe = recipe;
    this.author = user;
    this.rating = rating;
    this.comment = comment;

    // update relationships to assure referential integrity
  }

}
