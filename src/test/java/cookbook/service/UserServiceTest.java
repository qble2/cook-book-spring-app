package cookbook.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import qble2.cookbook.exception.ResourceNotFoundException;
import qble2.cookbook.recipe.RecipeMapper;
import qble2.cookbook.user.UserMapper;
import qble2.cookbook.user.UserRepository;
import qble2.cookbook.user.UserService;
import qble2.cookbook.user.dto.UserDto;
import qble2.cookbook.user.model.User;

// unit testing
@ExtendWith(MockitoExtension.class) // allows to get rid of the autoCloseable code
class UserServiceTest {

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserMapper userMapper;

  @Mock
  private RecipeMapper recipeMapper;

  private UserService userService; // underTest

  @BeforeEach
  void setUp() {
    userService = new UserService(passwordEncoder, userRepository, userMapper);
  }

  @Test
  void given_userExists_loadUserByUsername_willReturnUserDetails() {
    // given
    User existingUser = User.builder().id(UUID.randomUUID()).username("username")
        .password("password").firstName("john").lastName("wick").email("john.wick@xyz.com").build();
    given(userRepository.findByUsername(any())).willReturn(Optional.of(existingUser));

    // when
    userService.loadUserByUsername(existingUser.getUsername());

    // then
    InOrder inOrder = Mockito.inOrder(userRepository, userMapper);
    inOrder.verify(userRepository).findByUsername(existingUser.getUsername());
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void given_userDoesNotExist_getUser_willThrowResourceNotFoundException() {
    // given
    UUID unknownUserId = UUID.randomUUID();
    given(userRepository.findById(any())).willReturn(Optional.empty());

    // when
    // then
    assertThatThrownBy(() -> userService.getUser(unknownUserId))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void given_userExists_getUser_willReturnUser() {
    // given
    UUID existingUserId = UUID.randomUUID();
    given(userRepository.findById(any())).willReturn(Optional.of(new User()));
    given(userMapper.toDto(any())).willReturn(new UserDto());

    // when
    userService.getUser(existingUserId);

    // then
    InOrder inOrder = Mockito.inOrder(userRepository, userMapper);
    inOrder.verify(userRepository).findById(existingUserId);
    inOrder.verify(userMapper).toDto(any());
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void given_userDoesNotExist_getUserByUsername_willThrowResourceNotFoundException() {
    // given
    String unknownUsername = "unknown";
    given(userRepository.findByUsername(any())).willReturn(Optional.empty());

    // when
    // then
    assertThatThrownBy(() -> userService.getUserByUsername(unknownUsername))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void given_userExists_getUserByUsername_willReturnUser() {
    // given
    String existingUsername = "johnwick";
    given(userRepository.findByUsername(any())).willReturn(Optional.of(new User()));
    given(userMapper.toDto(any())).willReturn(new UserDto());

    // when
    userService.getUserByUsername(existingUsername);

    // then
    InOrder inOrder = Mockito.inOrder(userRepository, userMapper);
    inOrder.verify(userRepository).findByUsername(existingUsername);
    inOrder.verify(userMapper).toDto(any());
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void given_validUser_createUser_willReturnCreatedUser() {
    // given
    UserDto userPayload =
        UserDto.builder().firstName("john").lastName("wick").email("john.wick@xyz.com").build();
    given(userMapper.toUser(any())).willReturn(new User());

    // when
    userService.createUser(userPayload);

    // then
    InOrder inOrder = Mockito.inOrder(userRepository, userMapper);
    inOrder.verify(userMapper).toUser(any());
    inOrder.verify(userRepository).save(any());
    inOrder.verify(userMapper).toDto(any());
    inOrder.verifyNoMoreInteractions();
  }

}
