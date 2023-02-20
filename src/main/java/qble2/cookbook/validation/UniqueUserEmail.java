package qble2.cookbook.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {UniqueUserEmailValidator.class})
public @interface UniqueUserEmail {

  String message() default "{user.email.Taken.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

}
