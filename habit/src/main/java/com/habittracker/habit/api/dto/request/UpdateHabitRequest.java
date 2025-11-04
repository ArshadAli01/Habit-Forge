package com.habittracker.habit.api.dto.request;

import com.habittracker.habit.api.enums.Frequency;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO for partial habit update.
 * Optional fields; name uniqueness re-checked.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class UpdateHabitRequest {
    @Size(min = 3, max = 100, message = "Habit name must be between 3 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    private Frequency frequency;
}