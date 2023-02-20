package qble2.cookbook.role;

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
import qble2.cookbook.role.dto.RoleDto;
import qble2.cookbook.role.model.Role;

// disabling Lombok @Buidler is needed to make @AfterMapping work with @MappingTarget
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface RoleMapper {

  @Named(value = "toRoleDtoList")
  @IterableMapping(qualifiedByName = "toRoleDto")
  List<RoleDto> toDtoList(List<Role> listSource);

  @Named(value = "toRoleDto")
  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "id", source = "id")
  @Mapping(target = "name", source = "name")
  RoleDto toDto(Role source);

  @AfterMapping
  default void addLinks(Role source, @MappingTarget RoleDto target) {
    Link selfLink =
        WebMvcLinkBuilder.linkTo(RoleController.class).slash(source.getId()).withSelfRel();
    target.add(selfLink);

    Link rootLink = linkTo(methodOn(RoleController.class).getRoles()).withRel("roles");
    target.add(rootLink);
  }

  @Named(value = "toRoleEntityList")
  @IterableMapping(qualifiedByName = "toRoleEntity")
  List<Role> toRoleList(List<RoleDto> listSource);

  @Named(value = "toRoleEntity")
  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "name", source = "name")
  Role toRole(RoleDto source);

}
