package com.habittracker.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Application Entry Point.
 * Scans all modules under com.habittracker.
 */
@SpringBootApplication(scanBasePackages = "com.habittracker")
public class HabitTrackerApplication {
    public static void main(String[] args) {
        SpringApplication.run(HabitTrackerApplication.class, args);
    }
}