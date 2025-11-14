package com.habittracker.user.core.constants;

/**
 * Constants for User module.
 * Centralized configuration values.
 */
public final class UserConstants {

    // Validation
    public static final int MIN_NAME_LENGTH = 2;
    public static final int MAX_NAME_LENGTH = 100;
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_EMAIL_LENGTH = 255;
    public static final int BCRYPT_HASH_LENGTH = 60;

    private UserConstants() {
        throw new AssertionError("Cannot instantiate constants class");
    }
}