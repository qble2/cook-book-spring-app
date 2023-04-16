package qble2.cookbook.recipe;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import qble2.cookbook.recipe.criteria.CriteriaRecipeRepository;
import qble2.cookbook.recipe.dtoprojection.DtoProjectionRecipeRepository;
import qble2.cookbook.recipe.model.Recipe;

import java.util.Optional;
import java.util.UUID;

public interface RecipeRepository extends JpaRepository<Recipe, UUID>, CriteriaRecipeRepository,
        DtoProjectionRecipeRepository, JpaSpecificationExecutor<Recipe> {

    /**
     * Paging
     **/

    // TODO BKE confirm this is working
    Page<Recipe> findByAuthor_Id(UUID userId, Pageable pageable);

    Page<Recipe> findByFavoritedBy_Username(String username, Pageable pageable);

    @Query("SELECT r FROM Recipe r")
    @EntityGraph(type = EntityGraphType.FETCH, value = "Recipe-entity-graph-with-tags")
    Page<Recipe> findAllAndLoadTags(Pageable pageable);

    /**
     *
     **/

    boolean existsByName(String name);

    Recipe findByName(String name);

    @Query("SELECT r FROM Recipe r" + " WHERE r.id = ?1")
    @EntityGraph(value = "Recipe-entity-graph-with-tags")
    Optional<Recipe> findByIdAndLoadTags(UUID recipeId);

    @Query("SELECT r FROM Recipe r" + " WHERE r.id = ?1")
    @EntityGraph(value = "Recipe-entity-graph-with-ingredients")
    Optional<Recipe> findByIdAndLoadIngredients(UUID id);

    @Query("SELECT r FROM Recipe r" + " WHERE r.id = ?1")
    @EntityGraph(value = "Recipe-entity-graph-with-instructions")
    Optional<Recipe> findByIdAndLoadInstructions(UUID id);

    @Query("SELECT r FROM Recipe r" + " WHERE r.id = ?1")
    @EntityGraph(value = "Recipe-entity-graph-with-reviews")
    Optional<Recipe> findByIdAndLoadReviews(UUID recipeId);

}
