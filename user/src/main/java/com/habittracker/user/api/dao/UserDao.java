package com.habittracker.user.api.dao;

import com.habittracker.user.api.entity.User;
import java.util.Optional;

/**
 * User DAO Interface.
 * Defines CRUD + uniqueness checks.
 */
public interface UserDao {
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}