package qble2.cookbook.user;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import qble2.cookbook.user.model.User;

public interface UserRepository extends JpaRepository<User, UUID> {

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);

  @Query("SELECT c FROM User c" + " WHERE c.id = ?1")
  @EntityGraph(value = "User-entity-graph-with-recipes")
  Optional<User> findByIdAndLoadRecipes(UUID id);

  @Query("SELECT c FROM User c" + " WHERE c.id = ?1")
  @EntityGraph(value = "User-entity-graph-with-reviews")
  Optional<User> findByIdAndLoadReviews(UUID id);

  Optional<User> findByUsername(String username);

}
