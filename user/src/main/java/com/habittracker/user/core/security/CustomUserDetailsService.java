package com.habittracker.user.core.security;

import com.habittracker.user.api.dao.UserDao;
import com.habittracker.user.api.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom UserDetailsService implementation.
 * Loads user from database for Spring Security authentication.
 */
@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserDao userDao;

    public CustomUserDetailsService(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);

        // Email is already normalized (lowercase) in the DAO
        User user = userDao.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", email);
                    return new UsernameNotFoundException("User not found: " + email);
                });

        log.debug("User found: ID={}, Email={}", user.getId(), user.getEmail());
        return new CustomUserDetails(user);
    }
}
