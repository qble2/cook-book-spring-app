package qble2.cookbook.role;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import qble2.cookbook.role.dto.RoleDto;

@RestController
@RequestMapping(path = RoleController.PATH,
    produces = {MediaTypes.HAL_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
@Validated
public class RoleController {

  public static final String PATH = "api/roles";

  @Autowired
  private RoleService roleService;

  @GetMapping
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public CollectionModel<RoleDto> getRoles() {
    List<RoleDto> roles = this.roleService.getRoles();

    Link selfLink = linkTo(RoleController.class).withSelfRel();
    return CollectionModel.of(roles, selfLink);
  }

  @GetMapping(path = "/{roleId}")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public ResponseEntity<RoleDto> getRole(
      @PathVariable(name = "roleId", required = true) UUID roleId) {
    RoleDto roleDto = this.roleService.getRole(roleId);

    return ResponseEntity.ok().body(roleDto);
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @Validated(RoleDto.OnCreateValidationGroup.class)
  public ResponseEntity<RoleDto> createRole(@Valid @RequestBody(required = true) RoleDto roleDto) {
    RoleDto createdRoleDto = this.roleService.createRole(roleDto);

    Link selfLink =
        linkTo(methodOn(RoleController.class).getRole(createdRoleDto.getId())).withSelfRel();
    createdRoleDto.add(selfLink);

    final URI uri = MvcUriComponentsBuilder.fromController(RoleController.class).path("/{roleId}")
        .buildAndExpand(createdRoleDto.getId()).toUri();

    return ResponseEntity.created(uri).body(createdRoleDto);
  }

  @PostMapping(path = "/{roleId}/users/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public ResponseEntity<Void> addRoleToUser(
      @PathVariable(name = "roleId", required = true) UUID roleId,
      @PathVariable(name = "userId", required = true) UUID userId) {
    this.roleService.addRoleToUser(userId, roleId);

    return ResponseEntity.ok().build();
  }

}
