package ro.utcluj.cs.userservice.dtos.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {RoleValidator.class})
public @interface ValidRole {
    String message() default "Role must be either ADMIN or CLIENT";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
