package qble2.cookbook.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import qble2.cookbook.user.UserRepository;

public class UniqueUserUsernameValidator
    implements ConstraintValidator<UniqueUserUsername, String> {

  @Autowired
  private UserRepository userRepository;

  public UniqueUserUsernameValidator(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public boolean isValid(String username, ConstraintValidatorContext context) {
    return username != null && !this.userRepository.existsByUsername(username);
  }

}
