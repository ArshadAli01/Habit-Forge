package com.habittracker.user.rest.controllers;

import com.habittracker.user.api.dto.request.CreateUserRequest;
import com.habittracker.user.api.dto.response.UserResponse;
import com.habittracker.user.api.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * User REST Controller.
 * Endpoints: register (public), me (auth).
 */
@RestController
@RequestMapping("/api/v1/users")
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        UserResponse response = userService.getCurrentUser();
        return ResponseEntity.ok(response);
    }
}