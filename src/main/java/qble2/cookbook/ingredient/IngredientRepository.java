package qble2.cookbook.ingredient;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import qble2.cookbook.ingredient.model.Ingredient;

public interface IngredientRepository extends JpaRepository<Ingredient, UUID> {

  boolean existsByName(String name);

}
