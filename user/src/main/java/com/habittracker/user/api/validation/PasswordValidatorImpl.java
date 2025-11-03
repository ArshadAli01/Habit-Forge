package com.habittracker.user.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for password: 8+ chars, 1 uppercase, 1 digit.
 */
public class PasswordValidatorImpl implements ConstraintValidator<PasswordValidator, String> {
    private static final String PATTERN = "^(?=.*[A-Z])(?=.*\\d).{8,}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return false;
        return value.matches(PATTERN);
    }
}