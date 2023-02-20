package qble2.cookbook.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * @deprecated is not sufficient to cover updating a recipe without changing its name use
 *             {@link qble2.cookbook.validation.UniqueRecipeName} instead
 */
@Deprecated
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {UniqueRecipeNameValidator_OLD.class})
public @interface UniqueRecipeName_OLD {

  String message() default "{recipe.name.Taken.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

}
