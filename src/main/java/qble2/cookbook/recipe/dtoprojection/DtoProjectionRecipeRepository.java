package qble2.cookbook.recipe.dtoprojection;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

// XXX BKE DTO Project + Specification not yet supported by Spring
public interface DtoProjectionRecipeRepository {

  // Spring Data JPA DTO projection
  @Query("select new qble2.cookbook.recipe.dtoprojection.RecipeOverviewProjectionDto(" + "r.id as id"
      + ", r.name" + ", r.description" + ", r.servings" + ", r.preparationTime" + ", r.cookingTime"
      + ", r.createdAt" + ", r.editedAt" + " , (r.author.id = ?1)"
      + " , case when exists (select 1 from r.favoritedBy frBy where frBy.id = ?1) then true else false end"
      + ")" + "	from Recipe r" + " left join User u on r.author.id = u.id" + "	order by r.id")
  List<RecipeOverviewProjectionDto> findByDtoProjection(UUID userId);

  // Spring Data JPA DTO projection + pagination
  // if the "userId" parameter is not used in the countQuery, the following error will occur:
  // QueryParameterSetter$ErrorHandling - Silently ignoring
  // Could not locate ordinal parameter [1], expecting one of []
  @Query(value = "select new qble2.cookbook.recipe.dtoprojection.RecipeOverviewProjectionDto("
      + "r.id as id" + ", r.name" + ", r.description" + ", r.servings" + ", r.preparationTime"
      + ", r.cookingTime" + ", r.createdAt" + ", r.editedAt" + ", (r.author.id = ?1)"
      + ", case when exists (select 1 from r.favoritedBy frBy where frBy.id = ?1) then true else false end"
      + ")" + "	from Recipe r" + " left join User u on r.author.id = u.id" + "	order by r.id",
      countQuery = "select count(*) from Recipe r where ?1 is not null") // fake use of the
                                                                         // unnecessary "userId"
                                                                         // parameter
  Page<RecipeOverviewProjectionDto> findByDtoProjectionWithPagination(UUID userId,
      Pageable pageable);

}
