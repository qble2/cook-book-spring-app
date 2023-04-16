package qble2.cookbook;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import lombok.extern.slf4j.Slf4j;
import qble2.cookbook.ingredient.IngredientService;
import qble2.cookbook.ingredient.dto.IngredientDto;
import qble2.cookbook.ingredient.model.UnitOfMeasureEnum;
import qble2.cookbook.recipe.RecipeService;
import qble2.cookbook.recipe.dto.RecipeDto;
import qble2.cookbook.recipe.enums.RecipeTagEnum;
import qble2.cookbook.review.ReviewService;
import qble2.cookbook.review.dto.ReviewDto;
import qble2.cookbook.role.RoleService;
import qble2.cookbook.role.dto.RoleDto;
import qble2.cookbook.role.model.RoleEnum;
import qble2.cookbook.user.UserService;
import qble2.cookbook.user.dto.UserDto;

@Configuration
@Slf4j
public class InitFakeDataConfiguration {

  private static final int RECIPES_COUNT = 20;
  private static final Random RANDOM = new Random();

  @Bean
  @Profile("!test")
  public CommandLineRunner initFakeData(RoleService roleService, UserService userService,
      RecipeService recipeService, IngredientService ingredientService,
      ReviewService reviewService) {
    return args -> {
      try {
        // role
        RoleDto userRole = RoleDto.builder().name(RoleEnum.ROLE_USER).build();
        RoleDto createdRole = roleService.createRole(userRole);
        log.info("Created role: (name: {} , id: {})", createdRole.getName(), createdRole.getId());

        // user
        List<UserDto> users = new ArrayList<>();
        UserDto userA = createUser(userService, "a", "a");
        users.add(userA);

        UserDto userB = createUser(userService, "b", "b");
        users.add(userB);

        UserDto userC = createUser(userService, "c", "c");
        users.add(userC);

        // add role to users
        users.stream().forEach(user -> {
          log.info("Adding role {} to user {}", createdRole.getName(), user.getUsername());
          roleService.addRoleToUser(user.getId(), createdRole.getId());
        });

        // ingredients
        List<IngredientDto> availableIngredients = IntStream.range(1, 6).boxed().map(counter -> {
          IngredientDto ingredientDto =
              IngredientDto.builder().name("Ingredient " + counter).build();
          IngredientDto createdIngredient = ingredientService.createIngredient(ingredientDto);
          log.info("Created ingredient: (name: {} , id: {})", createdIngredient.getName(),
              createdIngredient.getId());
          return createdIngredient;
        }).toList();

        List<IngredientDto> quantifiedRecipeIngredients =
            availableIngredients.stream().map(ingredient -> {
              ingredient.setQuantity(generateRandomInt(50, 500))
                  .setUnitOfMeasure(UnitOfMeasureEnum.GRAM);
              return ingredient;
            }).toList();

        // recipes
        List<RecipeDto> recipes = IntStream.range(1, RECIPES_COUNT + 1).boxed().map(counter -> {
          UserDto userDto = counter % 2 == 0 ? userB : userA;
          RecipeDto recipeDto = RecipeDto.builder().name("Recipe " + generateRandomString())
              .description("Description " + counter).servings(generateRandomInt(1, 7))
              .preparationTime(generateRandomLong(15, 45))
              .cookingTime(generateRandomLong(1, 3) * 60).author(userDto)
              .tags(new HashSet<>(getRandomElements(RecipeTagEnum.getAllTags(), 3)))
              .ingredients(new ArrayList<>(
                  getRandomElements(quantifiedRecipeIngredients, generateRandomInt(1, 5))))
              .instructions(List.of("do 1", "do 2", "do 3")).build();
          RecipeDto createdRecipeDto = recipeService.createRecipe(userDto.getUsername(), recipeDto);
          log.info("Created recipe: (name: {} , id: {})", createdRecipeDto.getName(),
              createdRecipeDto.getId());
          return createdRecipeDto;
        }).toList();

        // add reviews to recipes
        recipes.stream().forEach(recipe -> users.stream()
            // a user cannot review his own recipe
            .filter(user -> !user.getId().equals(recipe.getAuthor().getId())) //
            .forEach(user -> {
              ReviewDto reviewDto =
                  ReviewDto.builder().rating(generateRandomInt(0, 5))
                      .comment(String.format("review by user (%s) on recipe (%s)",
                          user.getUsername(), recipe.getName()))
                      .reviewDate(LocalDateTime.now()).build();
              ReviewDto createdRecipeReview =
                  reviewService.createReview(user.getUsername(), recipe.getId(), reviewDto);
              log.info("Created review for recipe {} by user {}",
                  createdRecipeReview.getRecipe().getName(),
                  createdRecipeReview.getAuthor().getUsername());
            }));
      } catch (Exception e) {
        log.error("An error has occured", e);
      }
    };

  }

  private UserDto createUser(UserService userService, String userName, String password) {
    UserDto userDto = UserDto.builder().username(userName).password(password)
        .firstName(String.format("John %s", userName)).lastName(String.format("Wick %s", userName))
        .email(String.format("%s@xxx.com", userName)).build();

    UserDto createdUser = userService.createUser(userDto);
    log.info("Created user: (username: {} , id: {})", createdUser.getUsername(),
        createdUser.getId());

    return createdUser;
  }

  private int generateRandomInt(int origin, int bound) {
    return RANDOM.nextInt(origin, bound);
  }

  private Long generateRandomLong(int origin, int bound) {
    return RANDOM.nextLong(origin, bound);
  }

  private String generateRandomString() {
    int leftLimit = 97; // letter 'a'
    int rightLimit = 122; // letter 'z'
    int targetStringLength = 10;

    return RANDOM.ints(leftLimit, rightLimit + 1).limit(targetStringLength)
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        .toString();
  }

  public <T> Collection<T> getRandomElements(Collection<T> collection, int count) {
    Collection<T> result = new ArrayList<>();

    if (CollectionUtils.isNotEmpty(collection)) {
      List<T> mutableList = new ArrayList<>(collection);
      Collections.shuffle(mutableList); // can only shuffle mutable collections

      IntStream.range(0, Math.min(count, collection.size() - 1))
          .forEach(counter -> result.add(mutableList.get(counter)));
    }

    return result;
  }

}
