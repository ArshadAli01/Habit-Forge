package com.habittracker.habit.api.exception;

import com.habittracker.common.exception.EntityNotFoundException;

/**
 * Habit-specificNotFoundException.
 * Extends EntityNotFoundException so it's caught by the global handler.
 */
public class HabitNotFoundException extends EntityNotFoundException {
    public HabitNotFoundException(String message) {
        super(message);
    }
}