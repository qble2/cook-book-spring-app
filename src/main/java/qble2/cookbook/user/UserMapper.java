package qble2.cookbook.user;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import qble2.cookbook.recipe.RecipeController;
import qble2.cookbook.review.ReviewController;
import qble2.cookbook.role.RoleMapper;
import qble2.cookbook.user.dto.UserDto;
import qble2.cookbook.user.model.User;

// disabling Lombok @Buidler is needed to make @AfterMapping work with @MappingTarget
@Mapper(componentModel = "spring", uses = RoleMapper.class,
    builder = @Builder(disableBuilder = true))
public interface UserMapper {

  @Named(value = "toUserList")
  @IterableMapping(qualifiedByName = "toUser")
  List<User> toUserList(List<UserDto> listSource);

  @Named(value = "toUser")
  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "username", source = "username")
  @Mapping(target = "firstName", source = "firstName")
  @Mapping(target = "lastName", source = "lastName")
  @Mapping(target = "email", source = "email")
  User toUser(UserDto source);

  @Named(value = "toUserDtoList")
  @IterableMapping(qualifiedByName = "toUserDto")
  List<UserDto> toDetailedDtoList(List<User> listSource);

  @Named(value = "toUserDto")
  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "id", source = "id")
  @Mapping(target = "username", source = "username")
  @Mapping(target = "firstName", source = "firstName")
  @Mapping(target = "lastName", source = "lastName")
  @Mapping(target = "email", source = "email")
  @Mapping(target = "roles", source = "roles", qualifiedByName = "toRoleDto")
  UserDto toDto(User source);

  @Named(value = "toMinimalUserDtoList")
  @IterableMapping(qualifiedByName = "toMinimalUserDto")
  List<UserDto> toMinimalDtoList(List<User> listSource);

  @Named(value = "toMinimalUserDto")
  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "id", source = "id")
  @Mapping(target = "username", source = "username")
  UserDto toMinmalDto(User source);

  @AfterMapping
  default void addLinks(User source, @MappingTarget UserDto target) {
    Link selfLink =
        WebMvcLinkBuilder.linkTo(UserController.class).slash(source.getId()).withSelfRel();
    target.add(selfLink);

    Link recipesLink = linkTo(methodOn(RecipeController.class).getUserRecipes(source.getId(), 0, 5))
        .withRel("recipes");
    target.add(recipesLink);

    Link reviewsLink =
        linkTo(methodOn(ReviewController.class).getUserReviews(source.getId())).withRel("reviews");
    target.add(reviewsLink);
  }

  @Named(value = "updateUserFromUserDto")
  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "username", source = "username")
  @Mapping(target = "firstName", source = "firstName")
  @Mapping(target = "lastName", source = "lastName")
  @Mapping(target = "email", source = "email")
    // @Mapping(target = "roles", source = "roles", qualifiedByName = "toRoleDto")
  void updateUser(UserDto source, @MappingTarget User target);

}
