package com.habittracker.user.core.service;

import com.habittracker.user.api.dto.request.CreateUserRequest;
import com.habittracker.user.api.dto.response.UserResponse;
import com.habittracker.user.api.entity.User;
import com.habittracker.user.api.exception.UserNotFoundException;
import com.habittracker.user.api.service.UserService;
import com.habittracker.user.api.dao.UserDao;

import com.habittracker.user.core.converter.UserConverter;

import com.habittracker.common.exception.DuplicateResourceException;
import com.habittracker.common.utility.AuditFieldUtility;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * User Service Implementation.
 * Business logic: registration (hash, duplicate check), profile retrieval.
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final UserConverter converter;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserDao userDao, UserConverter converter, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.converter = converter;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponse register(CreateUserRequest request) {
        log.info("Registering user with email: {}", request.getEmail());

        // Check duplicate
        if (userDao.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }

        // Convert, hash password, set audits
        User user = converter.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        AuditFieldUtility.initialize(user, "system");

        User saved = userDao.save(user);
        log.info("User registered successfully: ID {}", saved.getId());

        return converter.toResponse(saved);
    }

    @Override
    public UserResponse getCurrentUser() {
        // Extract from SecurityContext (custom principal with ID)
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.debug("Fetching profile for email: {}", email);

        Optional<User> userOpt = userDao.findByEmail(email);
        User user = userOpt.orElseThrow(() -> new UserNotFoundException("User not found: " + email));

        return converter.toResponse(user);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userDao.findByEmail(email);
    }
}