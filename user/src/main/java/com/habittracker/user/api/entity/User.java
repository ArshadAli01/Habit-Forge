package com.habittracker.user.api.entity;

import com.habittracker.common.api.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * User Entity - Represents a registered user.
 * Extends BaseEntity for audits.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class User extends BaseEntity {
    private String name;
    private String email;
    private String password;  // Hashed with BCrypt
}