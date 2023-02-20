package qble2.cookbook.user;

import java.util.Collection;
import java.util.UUID;
import javax.transaction.Transactional;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import lombok.AllArgsConstructor;
import qble2.cookbook.exception.ResourceNotFoundException;
import qble2.cookbook.role.model.RoleEnum;
import qble2.cookbook.security.CurrentUserDetails;
import qble2.cookbook.user.dto.UserDto;
import qble2.cookbook.user.dto.UsersPageDto;
import qble2.cookbook.user.model.User;

@Service
@Transactional
@Validated
// @AllArgsConstructor needed to be able to inject mocked dependencies for unit testing
@AllArgsConstructor
public class UserService implements UserDetailsService {

  @Autowired
  private final PasswordEncoder passwordEncoder;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserMapper userMapper;

  @Override
  public org.springframework.security.core.userdetails.User loadUserByUsername(String username)
      throws UsernameNotFoundException {
    User user =
        this.userRepository.findByUsername(username).orElseThrow(ResourceNotFoundException::new);

    Collection<SimpleGrantedAuthority> authorities = user.getRoles().stream()
        .map(role -> new SimpleGrantedAuthority(role.getName().toString())).toList();

    // return new User(user.getUsername(), user.getPassword(), authorities);
    return new CurrentUserDetails(user.getId(), user.getUsername(), user.getPassword(),
        authorities);
  }

  public UsersPageDto getUsers(Pageable pageable) {
    return toUsersPage(this.userRepository.findAll(pageable));

  }

  public UserDto getUser(UUID userId) {
    return this.userMapper.toDto(getUserByIdOrThrow(userId));
  }

  public UserDto getUserByUsername(String username) {
    return this.userRepository.findByUsername(username).map(this.userMapper::toDto)
        .orElseThrow(ResourceNotFoundException::new);
  }

  @Validated(UserDto.OnCreateValidationGroup.class)
  public UserDto createUser(@Valid UserDto userDto) {
    User user = this.userMapper.toUser(userDto);

    // encode user password before saving it in the database
    user.setPassword(passwordEncoder.encode(userDto.getPassword()));
    // this.userMapper.updateUser(userDto, user);

    user = this.userRepository.save(user);
    return this.userMapper.toDto(user);
  }

  public User getUserByUsernameOrThrow(String username) {
    return this.userRepository.findByUsername(username).orElseThrow(ResourceNotFoundException::new);
  }

  public User getUserByIdOrThrow(UUID userId) {
    return this.userRepository.findById(userId).orElseThrow(ResourceNotFoundException::new);
  }

  public boolean isAdmin() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication != null && authentication.getAuthorities().stream()
        .anyMatch(authority -> authority.getAuthority().equals(RoleEnum.ROLE_ADMIN.toString()));
  }

  // TODO BKE update user

  //

  private UsersPageDto toUsersPage(Page<User> page) {
    return UsersPageDto.builder().users(this.userMapper.toDetailedDtoList(page.getContent()))
        .currentPage(page.getNumber()).totalPages(page.getTotalPages())
        .totalElements(page.getTotalElements()).build();
  }
}
