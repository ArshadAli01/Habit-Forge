package com.habittracker.habit.api.service;

import com.habittracker.habit.api.dto.request.CreateHabitRequest;
import com.habittracker.habit.api.dto.request.UpdateHabitRequest;
import com.habittracker.habit.api.dto.response.HabitResponse;
import com.habittracker.habit.api.dto.response.PagedHabitResponse;
import java.util.Optional;

/**
 * Habit Service Interface.
 * Business ops: CRUD with ownership, max 50 active.
 */
public interface HabitService {
    Long getCurrentUserId();
    HabitResponse create(CreateHabitRequest request, Long currentUserId);
    PagedHabitResponse getAll(Long currentUserId, int page, int size, Boolean isActive, String frequency, String sortBy, String sortDirection);
    Optional<HabitResponse> getById(Long id, Long currentUserId);
    HabitResponse update(Long id, UpdateHabitRequest request, Long currentUserId);
    void softDelete(Long id, Long currentUserId);
}