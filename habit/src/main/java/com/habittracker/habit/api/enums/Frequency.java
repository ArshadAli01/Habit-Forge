package com.habittracker.habit.api.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Habit Frequency Enum.
 * Maps to VARCHAR in DB; JSON serialization via @JsonValue.
 */
public enum Frequency {
    DAILY("DAILY"),
    WEEKLY("WEEKLY"),
    MONTHLY("MONTHLY");

    private final String value;

    Frequency(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Frequency fromValue(String value) {
        for (Frequency f : Frequency.values()) {
            if (f.value.equalsIgnoreCase(value)) {
                return f;
            }
        }
        throw new IllegalArgumentException("Unknown frequency: " + value);
    }
}