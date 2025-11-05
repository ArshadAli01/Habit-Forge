package com.habittracker.habit.api.dto.request;

import com.habittracker.habit.api.enums.Frequency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO for creating a habit.
 * Validates: name unique per user (DB), description max 500.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CreateHabitRequest {

    @NotBlank(message = "Habit name is required")
    @Size(min = 3, max = 100, message = "Habit name must be between 3 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Frequency is required")
    private Frequency frequency;
}