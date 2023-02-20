package qble2.cookbook.review;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import qble2.cookbook.review.model.Review;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

  List<Review> findByRecipe_Id(UUID recipeId);

  List<Review> findByAuthor_Id(UUID authorId);

  Optional<Review> findByRecipe_IdAndAuthor_Id(UUID recipeId, UUID authorId);

}
