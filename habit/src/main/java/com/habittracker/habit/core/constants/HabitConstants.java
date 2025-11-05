package com.habittracker.habit.core.constants;

/**
 * Constants for Habit module.
 * Centralized configuration values.
 */
public final class HabitConstants {

    // Business Rules
    public static final int MAX_ACTIVE_HABITS = 50;

    // Validation
    public static final int MIN_NAME_LENGTH = 3;
    public static final int MAX_NAME_LENGTH = 100;
    public static final int MAX_DESCRIPTION_LENGTH = 500;

    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    // Default Sort
    public static final String DEFAULT_SORT_BY = "created_at";
    public static final String DEFAULT_SORT_DIRECTION = "DESC";

    private HabitConstants() {
        // Prevent instantiation
        throw new AssertionError("Cannot instantiate constants class");
    }
}