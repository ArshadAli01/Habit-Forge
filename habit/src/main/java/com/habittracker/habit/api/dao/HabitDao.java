package com.habittracker.habit.api.dao;

import com.habittracker.habit.api.entity.Habit;
import java.util.List;
import java.util.Optional;

/**
 * Habit DAO Interface.
 * CRUD + user-specific queries (filters, count active).
 */
public interface HabitDao {
    Habit save(Habit habit);
    Habit update(Habit habit);
    Optional<Habit> findById(Long id);
    Optional<Habit> findByUserIdAndName(Long userId, String name);
    List<Habit> findByUserId(Long userId, Boolean isActive, String frequency, int offset, int limit, String sortBy, String sortDirection);
    long countByUserId(Long userId, Boolean isActive, String frequency);
}