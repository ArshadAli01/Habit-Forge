package com.habittracker.habit.core.service;

import com.habittracker.habit.api.dto.request.CreateHabitRequest;
import com.habittracker.habit.api.dto.request.UpdateHabitRequest;
import com.habittracker.habit.api.dto.response.HabitResponse;
import com.habittracker.habit.api.dto.response.PagedHabitResponse;
import com.habittracker.habit.api.entity.Habit;
import com.habittracker.habit.api.exception.HabitNotFoundException;
import com.habittracker.habit.api.service.HabitService;
import com.habittracker.habit.api.dao.HabitDao;
import com.habittracker.habit.core.converter.HabitConverter;
import com.habittracker.habit.core.constants.HabitConstants;
import com.habittracker.common.exception.BusinessException;
import com.habittracker.common.exception.DuplicateResourceException;
import com.habittracker.common.utility.AuditFieldUtility;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Habit Service Implementation.
 * Business: ownership (userId from auth), max 50 active, soft-delete, audits.
 */
@Service
@Slf4j
@Transactional
public class HabitServiceImpl implements HabitService {

    private final HabitDao habitDao;
    private final HabitConverter converter;

    public HabitServiceImpl(HabitDao habitDao, HabitConverter converter) {
        this.habitDao = habitDao;
        this.converter = converter;
    }

    @Override
    public Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new BusinessException("User not authenticated");
        }

        // TODO: Replace with actual implementation when user module is integrated
        // Example: return ((CustomUserDetails) auth.getPrincipal()).getId();
        // For now, returning placeholder
        log.warn("Using placeholder user ID - implement proper authentication integration");
        return 1L;
    }

    @Override
    @Transactional
    public HabitResponse create(CreateHabitRequest request, Long currentUserId) {
        log.info("Creating habit '{}' for user {}", request.getName(), currentUserId);

        // Check for duplicate name (case-insensitive)
        Optional<Habit> existing = habitDao.findByUserIdAndName(currentUserId, request.getName().trim());
        if (existing.isPresent() && existing.get().getIsActive()) {
            throw new DuplicateResourceException(
                    String.format("Active habit with name '%s' already exists", request.getName())
            );
        }

        // Max 50 active habits check
        long activeCount = habitDao.countByUserId(currentUserId, true, null);
        if (activeCount >= HabitConstants.MAX_ACTIVE_HABITS) {
            throw new BusinessException(
                    String.format("Maximum %d active habits allowed", HabitConstants.MAX_ACTIVE_HABITS)
            );
        }

        Habit habit = converter.toEntity(request, currentUserId);
        AuditFieldUtility.initialize(habit, "system");

        Habit saved = habitDao.save(habit);
        log.info("Habit created successfully: ID {}", saved.getId());

        return converter.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedHabitResponse getAll(Long currentUserId, int page, int size, Boolean isActive,
                                     String frequency, String sortBy, String sortDirection) {
        log.debug("Fetching habits for user {}: page={}, size={}, isActive={}, frequency={}, sortBy={}, sortDirection={}",
                currentUserId, page, size, isActive, frequency, sortBy, sortDirection);

        // Validate pagination parameters
        if (page < 0) {
            page = 0;
        }
        if (size < 1) {
            size = HabitConstants.DEFAULT_PAGE_SIZE;
        }
        if (size > HabitConstants.MAX_PAGE_SIZE) {
            size = HabitConstants.MAX_PAGE_SIZE;
        }

        int offset = page * size;
        List<Habit> habits = habitDao.findByUserId(currentUserId, isActive, frequency, offset, size, sortBy, sortDirection);
        long total = habitDao.countByUserId(currentUserId, isActive, frequency);

        List<HabitResponse> responses = habits.stream()
                .map(converter::toResponse)
                .collect(Collectors.toList());

        log.debug("Found {} habits for user {} (total: {})", habits.size(), currentUserId, total);

        return PagedHabitResponse.of(responses, page, size, total);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<HabitResponse> getById(Long id, Long currentUserId) {
        log.debug("Fetching habit {} for user {}", id, currentUserId);

        Optional<Habit> habitOpt = habitDao.findById(id);

        if (habitOpt.isEmpty()) {
            log.warn("Habit not found: {}", id);
            return Optional.empty();
        }

        Habit habit = habitOpt.get();
        if (!habit.getUserId().equals(currentUserId)) {
            log.warn("User {} attempted to access habit {} owned by user {}",
                    currentUserId, id, habit.getUserId());
            throw new HabitNotFoundException("Habit not found or unauthorized: " + id);
        }

        return Optional.of(converter.toResponse(habit));
    }

    @Override
    @Transactional
    public HabitResponse update(Long id, UpdateHabitRequest request, Long currentUserId) {
        log.info("Updating habit {} for user {}", id, currentUserId);

        Optional<Habit> habitOpt = habitDao.findById(id);
        Habit habit = habitOpt.orElseThrow(() -> {
            log.error("Habit not found: {}", id);
            return new HabitNotFoundException("Habit not found: " + id);
        });

        if (!habit.getUserId().equals(currentUserId)) {
            log.warn("User {} attempted to update habit {} owned by user {}",
                    currentUserId, id, habit.getUserId());
            throw new HabitNotFoundException("Unauthorized access to habit: " + id);
        }

        // Check for duplicate name if name is being changed
        if (request.getName() != null && !request.getName().trim().equals(habit.getName())) {
            Optional<Habit> existing = habitDao.findByUserIdAndName(currentUserId, request.getName().trim());
            if (existing.isPresent() && !existing.get().getId().equals(id) && existing.get().getIsActive()) {
                throw new DuplicateResourceException(
                        String.format("Active habit with name '%s' already exists", request.getName())
                );
            }
        }

        converter.toEntityForUpdate(habit, request);
        AuditFieldUtility.update(habit, "system");

        Habit updated = habitDao.update(habit);
        log.info("Habit updated successfully: ID {}", id);

        return converter.toResponse(updated);
    }

    @Override
    @Transactional
    public void softDelete(Long id, Long currentUserId) {
        log.info("Soft-deleting habit {} for user {}", id, currentUserId);

        Optional<Habit> habitOpt = habitDao.findById(id);
        Habit habit = habitOpt.orElseThrow(() -> {
            log.error("Habit not found: {}", id);
            return new HabitNotFoundException("Habit not found: " + id);
        });

        if (!habit.getUserId().equals(currentUserId)) {
            log.warn("User {} attempted to delete habit {} owned by user {}",
                    currentUserId, id, habit.getUserId());
            throw new HabitNotFoundException("Unauthorized access to habit: " + id);
        }

        if (!habit.getIsActive()) {
            log.warn("Habit {} is already inactive", id);
            throw new BusinessException("Habit is already deleted: " + id);
        }

        habit.setIsActive(false);
        AuditFieldUtility.update(habit, "system");

        habitDao.update(habit);
        log.info("Habit soft-deleted successfully: ID {}", id);
    }
}