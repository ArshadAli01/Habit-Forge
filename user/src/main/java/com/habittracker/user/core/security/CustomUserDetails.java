package com.habittracker.user.core.security;

import com.habittracker.user.api.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom UserDetails implementation that wraps our User entity.
 * Provides user ID for easy access in services.
 */
public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    /**
     * Get the user ID - used throughout the application for ownership validation.
     */
    public Long getId() {
        return user.getId();
    }

    /**
     * Get the full User entity.
     */
    public User getUser() {
        return user;
    }

    @Override
    public String getUsername() {
        return user.getEmail();  // Email is our username
    }

    @Override
    public String getPassword() {
        return user.getPassword();  // BCrypt hash
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // No roles yet - can add later
        return Collections.emptyList();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;  // Can use user.isActive if you add that field
    }
}