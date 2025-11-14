package com.habittracker.user.core.service;

import com.habittracker.user.api.dto.request.CreateUserRequest;
import com.habittracker.user.api.dto.response.UserResponse;
import com.habittracker.user.api.entity.User;
import com.habittracker.user.api.exception.UserNotFoundException;
import com.habittracker.user.api.service.UserService;
import com.habittracker.user.api.dao.UserDao;

import com.habittracker.user.core.converter.UserConverter;
import com.habittracker.common.exception.BusinessException;
import com.habittracker.common.exception.DuplicateResourceException;
import com.habittracker.common.utility.AuditFieldUtility;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * User Service Implementation.
 * Business logic: registration (hash, duplicate check), profile retrieval.
 */
@Service
@Slf4j
@Transactional
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
    @Transactional
    public UserResponse register(CreateUserRequest request) {
        log.info("Registering user with email: {}", request.getEmail());

        // Normalize email for duplicate check
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        // Check duplicate
        if (userDao.existsByEmail(normalizedEmail)) {
            log.warn("Registration failed: Email already exists: {}", normalizedEmail);
            throw new DuplicateResourceException("Email already exists: " + normalizedEmail);
        }

        // Convert, hash password, set audits
        User user = converter.toEntity(request);
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(encodedPassword);

        // Verify BCrypt hash is at least 60 characters
        if (encodedPassword.length() < 60) {
            log.error("Password encoding failed: hash too short");
            throw new BusinessException("Password encryption failed");
        }

        AuditFieldUtility.initialize(user, "system");

        User saved = userDao.save(user);
        log.info("User registered successfully: ID {}, Email: {}", saved.getId(), saved.getEmail());

        return converter.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            log.error("Attempted to get current user without authentication");
            throw new BusinessException("User not authenticated");
        }

        // For now, principal is just the email string until JWT is implemented
        String email = auth.getName();
        log.debug("Fetching profile for email: {}", email);

        Optional<User> userOpt = userDao.findByEmail(email);
        User user = userOpt.orElseThrow(() -> {
            log.error("User not found for email: {}", email);
            return new UserNotFoundException("User not found: " + email);
        });

        return converter.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userDao.findByEmail(email);
    }
}