package com.habittracker.habit.core.converter;

import com.habittracker.habit.api.dto.request.CreateHabitRequest;
import com.habittracker.habit.api.dto.request.UpdateHabitRequest;
import com.habittracker.habit.api.dto.response.HabitResponse;
import com.habittracker.habit.api.entity.Habit;
import org.springframework.stereotype.Component;

/**
 * Converter: DTO ↔ Entity.
 * Includes input sanitization and null safety.
 */
@Component
public class HabitConverter {

    public Habit toEntity(CreateHabitRequest request, Long userId) {
        return Habit.builder()
                .userId(userId)
                .name(sanitize(request.getName()))
                .description(sanitize(request.getDescription()))
                .frequency(request.getFrequency())
                .isActive(true)
                .build();
    }

    public Habit toEntityForUpdate(Habit existing, UpdateHabitRequest request) {
        if (request.getName() != null && !request.getName().isBlank()) {
            existing.setName(sanitize(request.getName()));
        }
        if (request.getDescription() != null) {
            // Allow setting description to empty/null
            existing.setDescription(request.getDescription().isBlank() ? null : sanitize(request.getDescription()));
        }
        if (request.getFrequency() != null) {
            existing.setFrequency(request.getFrequency());
        }
        return existing;
    }

    public HabitResponse toResponse(Habit habit) {
        return HabitResponse.builder()
                .id(habit.getId())
                .name(habit.getName())
                .description(habit.getDescription())
                .frequency(habit.getFrequency())
                .isActive(habit.getIsActive())
                .createdAt(habit.getCreatedAt())
                .build();
    }

    /**
     * Sanitize input by trimming whitespace.
     * Returns null for null/blank inputs.
     */
    private String sanitize(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        return input.trim();
    }
}