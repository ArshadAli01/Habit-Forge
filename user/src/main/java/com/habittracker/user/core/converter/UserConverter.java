package com.habittracker.user.core.converter;

import com.habittracker.user.api.dto.request.CreateUserRequest;
import com.habittracker.user.api.dto.response.UserResponse;
import com.habittracker.user.api.entity.User;
import org.springframework.stereotype.Component;

/**
 * Converter: DTO ↔ Entity.
 * Includes input sanitization and normalization.
 */
@Component
public class UserConverter {

    public User toEntity(CreateUserRequest request) {
        return User.builder()
                .name(sanitize(request.getName()))
                .email(normalizeEmail(request.getEmail()))
                .build();
    }

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Sanitize input by trimming whitespace.
     * Returns null for null/blank inputs.
     */
    private String sanitize(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        return input.trim();
    }

    /**
     * Normalize email: trim and lowercase.
     */
    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase();
    }
}