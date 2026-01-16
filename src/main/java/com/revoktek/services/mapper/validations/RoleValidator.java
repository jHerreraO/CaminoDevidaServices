package com.revoktek.services.mapper.validations;

import com.revoktek.services.model.enums.Authority;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RoleValidator implements ConstraintValidator<ValidRole, String> {
    @Override
    public void initialize(ValidRole constraintAnnotation) {
    }
    @Override
    public boolean isValid(String role, ConstraintValidatorContext context) {
        if (role == null) {
            return false; // or true, depending on whether null is allowed
        }
        try {
            Authority.valueOf(role);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
