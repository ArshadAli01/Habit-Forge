package com.habittracker.habit.api.entity;

import com.habittracker.common.api.BaseEntity;
import com.habittracker.habit.api.enums.Frequency;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Habit Entity - User-specific habit.
 * Extends BaseEntity for audits; soft-delete via isActive.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Habit extends BaseEntity {
    private Long userId;  // FK to users.id
    private String name;
    private String description;
    private Frequency frequency;
    private Boolean isActive = true;  // Default true for soft-delete
}