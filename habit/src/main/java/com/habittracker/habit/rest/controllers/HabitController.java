package com.habittracker.habit.rest.controllers;

import com.habittracker.habit.api.dto.request.CreateHabitRequest;
import com.habittracker.habit.api.dto.request.UpdateHabitRequest;
import com.habittracker.habit.api.dto.response.HabitResponse;
import com.habittracker.habit.api.dto.response.PagedHabitResponse;
import com.habittracker.habit.api.service.HabitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Habit REST Controller.
 * All endpoints require auth; userId from SecurityContext.
 */
@RestController
@RequestMapping("/api/v1/habits")
public class HabitController {

    private final HabitService habitService;

    public HabitController(HabitService habitService) {
        this.habitService = habitService;
    }

    @PostMapping
    public ResponseEntity<HabitResponse> create(@Valid @RequestBody CreateHabitRequest request) {
        Long userId = habitService.getCurrentUserId();  // From auth
        HabitResponse response = habitService.create(request, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<PagedHabitResponse> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String frequency) {
        Long userId = habitService.getCurrentUserId();
        PagedHabitResponse response = habitService.getAll(userId, page, size, isActive, frequency, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HabitResponse> getById(@PathVariable Long id) {
        Long userId = habitService.getCurrentUserId();
        Optional<HabitResponse> responseOpt = habitService.getById(id, userId);
        return responseOpt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<HabitResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateHabitRequest request) {
        Long userId = habitService.getCurrentUserId();
        HabitResponse response = habitService.update(id, request, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        Long userId = habitService.getCurrentUserId();
        habitService.softDelete(id, userId);
        return ResponseEntity.noContent().build();
    }
}