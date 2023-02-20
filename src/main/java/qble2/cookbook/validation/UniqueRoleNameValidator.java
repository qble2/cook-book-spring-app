package qble2.cookbook.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import qble2.cookbook.role.RoleRepository;
import qble2.cookbook.role.model.RoleEnum;

public class UniqueRoleNameValidator implements ConstraintValidator<UniqueRoleName, RoleEnum> {

  @Autowired
  private RoleRepository roleRepository;

  public UniqueRoleNameValidator(RoleRepository roleRepository) {
    this.roleRepository = roleRepository;
  }

  @Override
  public boolean isValid(RoleEnum name, ConstraintValidatorContext context) {
    return name != null && !this.roleRepository.existsByName(name);
  }

}
