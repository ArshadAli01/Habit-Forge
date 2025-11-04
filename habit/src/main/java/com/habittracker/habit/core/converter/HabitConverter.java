package com.habittracker.habit.core.converter;

import com.habittracker.habit.api.dto.request.CreateHabitRequest;
import com.habittracker.habit.api.dto.request.UpdateHabitRequest;
import com.habittracker.habit.api.dto.response.HabitResponse;
import com.habittracker.habit.api.entity.Habit;
import org.springframework.stereotype.Component;

/**
 * Converter: DTO ↔ Entity (enum handling).
 */
@Component
public class HabitConverter {

    public Habit toEntity(CreateHabitRequest request, Long userId) {
        return Habit.builder()
                .userId(userId)
                .name(request.getName().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .frequency(request.getFrequency())
                .isActive(true)
                .build();
    }

    public Habit toEntityForUpdate(Habit existing, UpdateHabitRequest request) {
        if (request.getName() != null) existing.setName(request.getName().trim());
        if (request.getDescription() != null) existing.setDescription(request.getDescription().trim());
        if (request.getFrequency() != null) existing.setFrequency(request.getFrequency());
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
}