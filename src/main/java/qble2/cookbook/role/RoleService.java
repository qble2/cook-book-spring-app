package qble2.cookbook.role;

import java.util.List;
import java.util.UUID;
import javax.transaction.Transactional;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import lombok.AllArgsConstructor;
import qble2.cookbook.exception.ResourceNotFoundException;
import qble2.cookbook.role.dto.RoleDto;
import qble2.cookbook.role.model.Role;
import qble2.cookbook.user.UserMapper;
import qble2.cookbook.user.UserRepository;
import qble2.cookbook.user.dto.UserDto;
import qble2.cookbook.user.model.User;

@Service
@Transactional
@Validated
// @AllArgsConstructor needed to be able to inject mocked dependencies for unit testing
@AllArgsConstructor
public class RoleService {

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserMapper userMapper;

  @Autowired
  private RoleMapper roleMapper;

  public List<RoleDto> getRoles() {
    List<Role> listOfRoleEntity = this.roleRepository.findAll();

    return this.roleMapper.toDtoList(listOfRoleEntity);
  }

  public RoleDto getRole(UUID roleId) {
    return this.roleRepository.findById(roleId).map(roleMapper::toDto)
        .orElseThrow(ResourceNotFoundException::new);
  }

  @Validated(RoleDto.OnCreateValidationGroup.class)
  public RoleDto createRole(@Valid RoleDto roleDto) {
    Role role = this.roleMapper.toRole(roleDto);
    role = this.roleRepository.save(role);

    return this.roleMapper.toDto(role);
  }

  public UserDto addRoleToUser(UUID userId, UUID roleId) {
    User user = this.userRepository.findById(userId).orElseThrow(ResourceNotFoundException::new);

    Role role = this.roleRepository.findById(roleId).orElseThrow(ResourceNotFoundException::new);

    user.addRole(role);
    user = this.userRepository.save(user);

    return userMapper.toDto(user);
  }

}
