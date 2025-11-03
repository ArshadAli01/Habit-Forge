package com.habittracker.user.api.service;

import com.habittracker.user.api.dto.request.CreateUserRequest;
import com.habittracker.user.api.dto.response.UserResponse;
import com.habittracker.user.api.entity.User;

import java.util.Optional;

/**
 * User Service Interface.
 * Business operations: register, get profile.
 */
public interface UserService {
    UserResponse register(CreateUserRequest request);
    UserResponse getCurrentUser();
    Optional<User> findByEmail(String email);
}