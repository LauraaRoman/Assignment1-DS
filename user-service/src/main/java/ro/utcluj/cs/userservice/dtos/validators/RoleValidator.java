package ro.utcluj.cs.userservice.dtos.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RoleValidator implements ConstraintValidator<ValidRole, String> {

    @Override
    public void initialize(ValidRole constraintAnnotation) {
    }

    @Override
    public boolean isValid(String role, ConstraintValidatorContext context) {
        if (role == null) {
            return false;
        }
        return role.equalsIgnoreCase("ADMIN") || role.equalsIgnoreCase("CLIENT");
    }
}
