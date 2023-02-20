package qble2.cookbook.user;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import qble2.cookbook.user.dto.UserDto;
import qble2.cookbook.user.dto.UsersPageDto;

@RestController
@RequestMapping(path = UserController.PATH,
    produces = {MediaTypes.HAL_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
@Validated
public class UserController {

  public static final String PATH = "api/users";

  @Autowired
  private UserService userService;

  @GetMapping
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public ResponseEntity<UsersPageDto> getUsers(
      @RequestParam(name = "page", required = false, defaultValue = "0") int page,
      @RequestParam(name = "size", required = false, defaultValue = "5") int size) {
    Pageable pageable = PageRequest.of(page, size);
    UsersPageDto usersPageDto = this.userService.getUsers(pageable);

    Link selfLink = linkTo(methodOn(UserController.class).getUsers(page, size)).withSelfRel();
    usersPageDto.add(selfLink);

    return ResponseEntity.ok().body(usersPageDto);
  }

  @GetMapping(path = "/{userId}")
  @PreAuthorize("#userId == authentication.principal.id or hasRole('ROLE_ADMIN')")
  public ResponseEntity<UserDto> getUser(
      @PathVariable(name = "userId", required = true) UUID userId) {
    UserDto userDto = this.userService.getUser(userId);

    return ResponseEntity.ok().body(userDto);
  }

}
