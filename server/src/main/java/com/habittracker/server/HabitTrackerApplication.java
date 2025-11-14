package com.habittracker.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

/**
 * Main Application Entry Point.
 * Scans all modules under com.habittracker.
 */
@SpringBootApplication(scanBasePackages = "com.habittracker")
public class HabitTrackerApplication {
    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(HabitTrackerApplication.class, args);
    }
}