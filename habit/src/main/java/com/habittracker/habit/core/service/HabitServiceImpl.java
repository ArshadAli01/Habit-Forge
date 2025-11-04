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
import com.habittracker.common.exception.BusinessException;
import com.habittracker.common.utility.AuditFieldUtility;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
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
public class HabitServiceImpl implements HabitService {

    private final HabitDao habitDao;
    private final HabitConverter converter;

    public HabitServiceImpl(HabitDao habitDao, HabitConverter converter) {
        this.habitDao = habitDao;
        this.converter = converter;
    }

    private Long getCurrentUserId() {
        // From SecurityContext (email principal → User ID via UserService if needed; simplified to Long ID for now)
        // In full integration, inject UserService or custom principal with ID
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return 1L;  // Placeholder; replace with ((CustomUserDetails) auth.getPrincipal()).getId();
    }

    @Override
    public HabitResponse create(CreateHabitRequest request, Long currentUserId) {
        log.info("Creating habit '{}' for user {}", request.getName(), currentUserId);

        // Max 50 active check
        long activeCount = habitDao.countByUserId(currentUserId, true, null);
        if (activeCount >= 50) {
            throw new BusinessException("Maximum 50 active habits allowed");
        }

        Habit habit = converter.toEntity(request, currentUserId);
        AuditFieldUtility.initialize(habit, "system");

        Habit saved = habitDao.save(habit);
        log.info("Habit created: ID {}", saved.getId());

        return converter.toResponse(saved);
    }

    @Override
    public PagedHabitResponse getAll(Long currentUserId, int page, int size, Boolean isActive, String frequency, String sortBy, String sortDirection) {
        log.debug("Fetching habits for user {}: page={}, size={}, isActive={}, frequency={}",
                currentUserId, page, size, isActive, frequency);

        int offset = page * size;
        List<Habit> habits = habitDao.findByUserId(currentUserId, isActive, frequency, offset, size, sortBy, sortDirection);
        long total = habitDao.countByUserId(currentUserId, isActive, frequency);

        List<HabitResponse> responses = habits.stream()
                .map(converter::toResponse)
                .collect(Collectors.toList());

        return PagedHabitResponse.of(responses, page, size, total);
    }

    @Override
    public Optional<HabitResponse> getById(Long id, Long currentUserId) {
        Optional<Habit> habitOpt = habitDao.findById(id);
        if (habitOpt.isPresent() && !habitOpt.get().getUserId().equals(currentUserId)) {
            throw new HabitNotFoundException("Habit not found or unauthorized: " + id);
        }
        return habitOpt.map(converter::toResponse);
    }

    @Override
    public HabitResponse update(Long id, UpdateHabitRequest request, Long currentUserId) {
        log.info("Updating habit {} for user {}", id, currentUserId);

        Optional<Habit> habitOpt = habitDao.findById(id);
        Habit habit = habitOpt.orElseThrow(() -> new HabitNotFoundException("Habit not found: " + id));

        if (!habit.getUserId().equals(currentUserId)) {
            throw new HabitNotFoundException("Unauthorized access to habit: " + id);
        }

        converter.toEntityForUpdate(habit, request);
        AuditFieldUtility.update(habit, "system");

        Habit updated = habitDao.save(habit);  // Re-save for ID/audits
        log.info("Habit updated: ID {}", id);

        return converter.toResponse(updated);
    }

    @Override
    public void softDelete(Long id, Long currentUserId) {
        log.info("Soft-deleting habit {} for user {}", id, currentUserId);

        Optional<Habit> habitOpt = habitDao.findById(id);
        Habit habit = habitOpt.orElseThrow(() -> new HabitNotFoundException("Habit not found: " + id));

        if (!habit.getUserId().equals(currentUserId)) {
            throw new HabitNotFoundException("Unauthorized access to habit: " + id);
        }

        habit.setIsActive(false);
        AuditFieldUtility.update(habit, "system");

        int rows = habitDao.softDeleteById(id);
        if (rows == 0) {
            throw new BusinessException("Failed to delete habit: " + id);
        }
        log.info("Habit soft-deleted: ID {}", id);
    }
}