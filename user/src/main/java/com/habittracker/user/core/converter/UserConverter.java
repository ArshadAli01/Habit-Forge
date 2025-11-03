package com.habittracker.user.core.converter;

import com.habittracker.user.api.dto.request.CreateUserRequest;
import com.habittracker.user.api.dto.response.UserResponse;
import com.habittracker.user.api.entity.User;
import org.springframework.stereotype.Component;

/**
 * Converter: DTO ↔ Entity.
 */
@Component
public class UserConverter {

    public User toEntity(CreateUserRequest request) {
        return User.builder()
                .name(request.getName().trim())
                .email(request.getEmail().trim().toLowerCase())
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
}