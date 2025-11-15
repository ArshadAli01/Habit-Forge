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
import com.habittracker.user.core.security.CustomUserDetails;
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
        // This method is fine, no new logging needed as it already logs errors/debug info.
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            log.error("SERVICE: Attempted to get user ID without valid authentication");
            throw new BusinessException("User not authenticated");
        }

        // Get CustomUserDetails from authentication
        Object principal = auth.getPrincipal();

        if (!(principal instanceof CustomUserDetails)) {
            log.error("SERVICE: Principal is not CustomUserDetails: {}", principal.getClass().getName());
            throw new BusinessException("Invalid authentication principal");
        }

        CustomUserDetails userDetails = (CustomUserDetails) principal;
        Long userId = userDetails.getId();

        log.debug("SERVICE: Successfully retrieved current user ID: {}", userId);
        return userId;
    }

    @Override
    @Transactional
    public HabitResponse create(CreateHabitRequest request, Long currentUserId) {
        log.info("SERVICE: Attempting to create habit '{}' for user {}", request.getName(), currentUserId);

        // Check for duplicate name (case-insensitive)
        Optional<Habit> existing = habitDao.findByUserIdAndName(currentUserId, request.getName().trim());
        if (existing.isPresent() && existing.get().getIsActive()) {
            log.warn("SERVICE: Duplicate active habit found: '{}' for user {}", request.getName(), currentUserId);
            throw new DuplicateResourceException(
                    String.format("Active habit with name '%s' already exists", request.getName())
            );
        }

        // Max 50 active habits check
        long activeCount = habitDao.countByUserId(currentUserId, true, null);
        log.debug("SERVICE: User {} currently has {} active habits (Max allowed: {})", currentUserId, activeCount, HabitConstants.MAX_ACTIVE_HABITS);

        if (activeCount >= HabitConstants.MAX_ACTIVE_HABITS) {
            log.error("SERVICE: User {} exceeded max active habits limit of {}", currentUserId, HabitConstants.MAX_ACTIVE_HABITS);
            throw new BusinessException(
                    String.format("Maximum %d active habits allowed", HabitConstants.MAX_ACTIVE_HABITS)
            );
        }

        Habit habit = converter.toEntity(request, currentUserId);
        AuditFieldUtility.initialize(habit, "system");
        log.debug("SERVICE: Habit entity converted and audit fields initialized for user {}", currentUserId);

        Habit saved = habitDao.save(habit);
        log.info("SERVICE: Habit created successfully with ID {}", saved.getId());

        return converter.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedHabitResponse getAll(Long currentUserId, int page, int size, Boolean isActive,
                                     String frequency, String sortBy, String sortDirection) {
        log.debug("SERVICE: Preparing to fetch habits for user {}. Input params: page={}, size={}, isActive={}, frequency={}, sortBy={}, sortDirection={}",
                currentUserId, page, size, isActive, frequency, sortBy, sortDirection);

        // Validate pagination parameters
        if (page < 0) {
            log.debug("SERVICE: Correcting invalid page number from {} to 0", page);
            page = 0;
        }
        if (size < 1) {
            log.debug("SERVICE: Correcting invalid size from {} to default {}", size, HabitConstants.DEFAULT_PAGE_SIZE);
            size = HabitConstants.DEFAULT_PAGE_SIZE;
        }
        if (size > HabitConstants.MAX_PAGE_SIZE) {
            log.debug("SERVICE: Correcting excessive size from {} to max {}", size, HabitConstants.MAX_PAGE_SIZE);
            size = HabitConstants.MAX_PAGE_SIZE;
        }

        int offset = page * size;
        log.debug("SERVICE: Calculated offset: {} (page * size: {} * {})", offset, page, size);

        List<Habit> habits = habitDao.findByUserId(currentUserId, isActive, frequency, offset, size, sortBy, sortDirection);
        long total = habitDao.countByUserId(currentUserId, isActive, frequency);

        log.debug("SERVICE: DAO returned {} habits from current page and total count is {}", habits.size(), total);

        List<HabitResponse> responses = habits.stream()
                .map(converter::toResponse)
                .collect(Collectors.toList());

        log.info("SERVICE: Fetched and converted {} habits for user {} (Total: {})", responses.size(), currentUserId, total);

        return PagedHabitResponse.of(responses, page, size, total);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<HabitResponse> getById(Long id, Long currentUserId) {
        log.info("SERVICE: Fetching habit {} for user {}", id, currentUserId);

        Optional<Habit> habitOpt = habitDao.findById(id);

        if (habitOpt.isEmpty()) {
            log.warn("SERVICE: Habit not found in DAO: {}", id);
            return Optional.empty();
        }

        Habit habit = habitOpt.get();
        if (!habit.getUserId().equals(currentUserId)) {
            log.error("SERVICE: Ownership check failed! User {} attempted to access habit {} owned by user {}",
                    currentUserId, id, habit.getUserId());
            throw new HabitNotFoundException("Habit not found or unauthorized: " + id);
        }

        log.info("SERVICE: Habit {} retrieved successfully for user {}", id, currentUserId);

        return Optional.of(converter.toResponse(habit));
    }

    @Override
    @Transactional
    public HabitResponse update(Long id, UpdateHabitRequest request, Long currentUserId) {
        log.info("SERVICE: Attempting to update habit {} for user {}", id, currentUserId);

        Optional<Habit> habitOpt = habitDao.findById(id);
        Habit habit = habitOpt.orElseThrow(() -> {
            log.error("SERVICE: Habit not found for update: {}", id);
            return new HabitNotFoundException("Habit not found: " + id);
        });

        if (!habit.getUserId().equals(currentUserId)) {
            log.error("SERVICE: Ownership check failed for update! User {} attempted to update habit {} owned by user {}",
                    currentUserId, id, habit.getUserId());
            throw new HabitNotFoundException("Unauthorized access to habit: " + id);
        }
        log.debug("SERVICE: Habit {} retrieved and ownership confirmed for user {}", id, currentUserId);

        // Check for duplicate name if name is being changed
        if (request.getName() != null && !request.getName().trim().equals(habit.getName())) {
            log.debug("SERVICE: Name change detected. Checking for duplicate name: '{}'", request.getName());
            Optional<Habit> existing = habitDao.findByUserIdAndName(currentUserId, request.getName().trim());
            if (existing.isPresent() && !existing.get().getId().equals(id) && existing.get().getIsActive()) {
                log.warn("SERVICE: Duplicate active habit name found during update for name: '{}'", request.getName());
                throw new DuplicateResourceException(
                        String.format("Active habit with name '%s' already exists", request.getName())
                );
            }
        }

        converter.toEntityForUpdate(habit, request);
        AuditFieldUtility.update(habit, "system");
        log.debug("SERVICE: Habit entity {} updated and audit fields refreshed", id);

        Habit updated = habitDao.update(habit);
        log.info("SERVICE: Habit updated successfully: ID {}", id);

        return converter.toResponse(updated);
    }

    @Override
    @Transactional
    public void softDelete(Long id, Long currentUserId) {
        log.info("SERVICE: Attempting to soft-delete habit {} for user {}", id, currentUserId);

        Optional<Habit> habitOpt = habitDao.findById(id);
        Habit habit = habitOpt.orElseThrow(() -> {
            log.error("SERVICE: Habit not found for soft-delete: {}", id);
            return new HabitNotFoundException("Habit not found: " + id);
        });

        if (!habit.getUserId().equals(currentUserId)) {
            log.error("SERVICE: Ownership check failed for soft-delete! User {} attempted to delete habit {} owned by user {}",
                    currentUserId, id, habit.getUserId());
            throw new HabitNotFoundException("Unauthorized access to habit: " + id);
        }
        log.debug("SERVICE: Habit {} retrieved and ownership confirmed for user {}", id, currentUserId);

        if (!habit.getIsActive()) {
            log.warn("SERVICE: Habit {} is already inactive/deleted. Skipping deletion.", id);
            throw new BusinessException("Habit is already deleted: " + id);
        }

        habit.setIsActive(false);
        AuditFieldUtility.update(habit, "system");
        log.debug("SERVICE: Habit {} set to inactive and audit fields refreshed", id);

        habitDao.update(habit);
        log.info("SERVICE: Habit {} soft-deleted successfully", id);
    }
}