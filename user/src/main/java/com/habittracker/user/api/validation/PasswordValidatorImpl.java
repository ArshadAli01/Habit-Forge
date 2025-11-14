package com.habittracker.user.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for password: 8+ chars, 1 uppercase, 1 digit.
 * Provides detailed error messages.
 */
public class PasswordValidatorImpl implements ConstraintValidator<PasswordValidator, String> {

    private static final int MIN_LENGTH = 8;
    private static final String UPPERCASE_PATTERN = ".*[A-Z].*";
    private static final String DIGIT_PATTERN = ".*\\d.*";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return false;
        }

        boolean valid = true;
        context.disableDefaultConstraintViolation();

        if (value.length() < MIN_LENGTH) {
            context.buildConstraintViolationWithTemplate(
                    "Password must be at least " + MIN_LENGTH + " characters"
            ).addConstraintViolation();
            valid = false;
        }

        if (!value.matches(UPPERCASE_PATTERN)) {
            context.buildConstraintViolationWithTemplate(
                    "Password must contain at least one uppercase letter"
            ).addConstraintViolation();
            valid = false;
        }

        if (!value.matches(DIGIT_PATTERN)) {
            context.buildConstraintViolationWithTemplate(
                    "Password must contain at least one digit"
            ).addConstraintViolation();
            valid = false;
        }

        return valid;
    }
}