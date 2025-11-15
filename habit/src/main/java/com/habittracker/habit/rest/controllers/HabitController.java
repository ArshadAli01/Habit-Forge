package com.habittracker.habit.rest.controllers;

import com.habittracker.habit.api.dto.request.CreateHabitRequest;
import com.habittracker.habit.api.dto.request.UpdateHabitRequest;
import com.habittracker.habit.api.dto.response.HabitResponse;
import com.habittracker.habit.api.dto.response.PagedHabitResponse;
import com.habittracker.habit.api.service.HabitService;
import com.habittracker.habit.core.constants.HabitConstants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j; // Added Slf4j import

import java.util.Optional;

/**
 * Habit REST Controller.
 * All endpoints require auth; userId from SecurityContext via service.
 */
@RestController
@RequestMapping("/api/v1/habits")
@Validated
@Slf4j // Added Slf4j annotation
public class HabitController {

    private final HabitService habitService;

    public HabitController(HabitService habitService) {
        this.habitService = habitService;
    }

    @PostMapping
    public ResponseEntity<HabitResponse> create(@Valid @RequestBody CreateHabitRequest request) {
        log.info("REST: POST /api/v1/habits - Request to create habit with name: {}", request.getName());
        Long userId = habitService.getCurrentUserId();
        log.debug("REST: Identified userId: {} for create habit request", userId);

        HabitResponse response = habitService.create(request, userId);

        log.info("REST: Habit created successfully. ID: {} (Status: 201 CREATED)", response.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<PagedHabitResponse> getAll(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "created_at") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String frequency) {

        log.info("REST: GET /api/v1/habits - Fetching habits with params: page={}, size={}, sortBy={}, sortDirection={}, isActive={}, frequency={}",
                page, size, sortBy, sortDirection, isActive, frequency);

        Long userId = habitService.getCurrentUserId();
        log.debug("REST: Identified userId: {} for getAll request", userId);

        PagedHabitResponse response = habitService.getAll(userId, page, size, isActive, frequency, sortBy, sortDirection);

        log.info("REST: Fetched {} habits (total: {}) for user {} (Status: 200 OK)", response.getHabits().size(), response.getTotalElements(), userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HabitResponse> getById(@PathVariable Long id) {
        log.info("REST: GET /api/v1/habits/{} - Request to fetch habit", id);
        Long userId = habitService.getCurrentUserId();
        log.debug("REST: Identified userId: {} for getById({}) request", userId, id);

        Optional<HabitResponse> responseOpt = habitService.getById(id, userId);

        if (responseOpt.isPresent()) {
            log.info("REST: Habit {} found (Status: 200 OK)", id);
            return ResponseEntity.ok(responseOpt.get());
        } else {
            log.warn("REST: Habit {} not found (Status: 404 NOT FOUND)", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<HabitResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateHabitRequest request) {
        log.info("REST: PUT /api/v1/habits/{} - Request to update habit. New name: {}", id, request.getName());
        Long userId = habitService.getCurrentUserId();
        log.debug("REST: Identified userId: {} for update({}) request", userId, id);

        HabitResponse response = habitService.update(id, request, userId);

        log.info("REST: Habit {} updated successfully (Status: 200 OK)", id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        log.info("REST: DELETE /api/v1/habits/{} - Request to soft-delete habit", id);
        Long userId = habitService.getCurrentUserId();
        log.debug("REST: Identified userId: {} for softDelete({}) request", userId, id);

        habitService.softDelete(id, userId);

        log.info("REST: Habit {} soft-deleted successfully (Status: 204 NO CONTENT)", id);
        return ResponseEntity.noContent().build();
    }
}