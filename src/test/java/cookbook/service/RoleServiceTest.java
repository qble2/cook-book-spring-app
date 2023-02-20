package cookbook.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import qble2.cookbook.exception.ResourceNotFoundException;
import qble2.cookbook.role.RoleMapper;
import qble2.cookbook.role.RoleRepository;
import qble2.cookbook.role.RoleService;
import qble2.cookbook.role.dto.RoleDto;
import qble2.cookbook.role.model.Role;
import qble2.cookbook.role.model.RoleEnum;
import qble2.cookbook.user.UserMapper;
import qble2.cookbook.user.UserRepository;
import qble2.cookbook.user.dto.UserDto;
import qble2.cookbook.user.model.User;

// unit testing
@ExtendWith(MockitoExtension.class) // allows to get rid of the autoCloseable code
class RoleServiceTest {

  @Mock
  private RoleRepository roleRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserMapper userMapper;

  @Mock
  private RoleMapper roleMapper;

  private RoleService roleService; // underTest

  @BeforeEach
  void setUp() {
    roleService = new RoleService(roleRepository, userRepository, userMapper, roleMapper);
  }

  @Test
  void can_getRoles() {
    // given

    // when
    roleService.getRoles();

    // then
    verify(roleRepository).findAll();
  }

  @Test
  void given_roleDoesNotExist_getRole_willThrowResourceNotFoundException() {
    // given
    UUID unknownRoleId = UUID.randomUUID();
    given(roleRepository.findById(any())).willReturn(Optional.empty());

    // when
    // then
    assertThatThrownBy(() -> roleService.getRole(unknownRoleId))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void given_roleExists_getRole_willReturnRole() {
    // given
    Role existingRole = Role.builder().id(UUID.randomUUID()).name(RoleEnum.ROLE_USER).build();
    RoleDto existingRoleMappedToDto =
        RoleDto.builder().id(existingRole.getId()).name(existingRole.getName()).build();
    given(roleRepository.findById(any())).willReturn(Optional.of(existingRole));
    given(roleMapper.toDto(any())).willReturn(existingRoleMappedToDto);

    // when
    RoleDto returnedRoleDto = roleService.getRole(existingRole.getId());

    // then
    verify(roleRepository).findById(existingRole.getId());
    assertThat(returnedRoleDto).isEqualTo(existingRoleMappedToDto);
  }

  @Test
  void given_validRole_createRole_willReturnCreatedRole() {
    // given
    RoleDto rolePayload = RoleDto.builder().name(RoleEnum.ROLE_USER).build();
    Role rolePayloadMappedToEntity = Role.builder().name(rolePayload.getName()).build();
    given(roleMapper.toRole(any())).willReturn(rolePayloadMappedToEntity);

    // when
    roleService.createRole(rolePayload);

    // then
    ArgumentCaptor<Role> roleArgumentCaptor = ArgumentCaptor.forClass(Role.class);
    verify(roleRepository).save(roleArgumentCaptor.capture());
    assertThat(roleArgumentCaptor.getValue()).isEqualTo(rolePayloadMappedToEntity);
  }

  @Test
  void given_roleAndUserExist_addRoleToUser_willReturnUpdatedUser() {
    // given
    UUID existingRoleId = UUID.randomUUID();
    UUID existingUserId = UUID.randomUUID();
    given(roleRepository.findById(any())).willReturn(Optional.of(new Role()));
    given(userRepository.findById(any())).willReturn(Optional.of(new User()));
    given(userMapper.toDto(any())).willReturn(new UserDto());

    // when
    roleService.addRoleToUser(existingUserId, existingRoleId);

    // then
    InOrder inOrder = Mockito.inOrder(roleRepository, userRepository, userMapper);
    inOrder.verify(roleRepository).findById(any());
    inOrder.verify(userRepository).findById(any());
    inOrder.verify(userMapper).toDto(any());
    inOrder.verifyNoMoreInteractions();
  }

}
