package com.habittracker.user.api.exception;

import com.habittracker.common.exception.EntityNotFoundException;

/**
 * User-specific NotFoundException.
 * Extends EntityNotFoundException so it's caught by the global handler.
 */
public class UserNotFoundException extends EntityNotFoundException {
    public UserNotFoundException(String message) {
        super(message);
    }
}