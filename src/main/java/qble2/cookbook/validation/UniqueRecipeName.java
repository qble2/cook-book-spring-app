package qble2.cookbook.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Target(ElementType.TYPE) // class level
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {UniqueRecipeNameValidator.class})
public @interface UniqueRecipeName {

  String message() default "{recipe.name.Taken.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

}
