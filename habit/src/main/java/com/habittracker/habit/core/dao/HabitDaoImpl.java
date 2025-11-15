package com.habittracker.habit.core.dao;

import com.habittracker.habit.api.dao.HabitDao;
import com.habittracker.habit.api.entity.Habit;
import com.habittracker.habit.api.enums.Frequency;
import com.habittracker.common.exception.EntityNotFoundException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * Habit DAO Implementation.
 * Uses NamedParameterJdbcTemplate for all queries.
 * Includes SQL injection protection via whitelisting.
 */
@Repository
public class HabitDaoImpl implements HabitDao {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    // Whitelist for SQL injection protection
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "name", "created_at", "updated_at", "frequency"
    );
    private static final Set<String> ALLOWED_DIRECTIONS = Set.of("ASC", "DESC");

    private static final RowMapper<Habit> HABIT_ROW_MAPPER = (rs, rowNum) -> {
        Habit habit = new Habit();
        habit.setId(rs.getLong("id"));
        habit.setUserId(rs.getLong("user_id"));
        habit.setName(rs.getString("name"));
        habit.setDescription(rs.getString("description"));

        // FIX 1: Add defensive code to prevent 500 on bad/null database data for Frequency enum.
        String freqString = rs.getString("frequency");
        if (freqString != null) {
            try {
                habit.setFrequency(Frequency.valueOf(freqString));
            } catch (IllegalArgumentException e) {
                // If the frequency value in the database is corrupt/invalid,
                // setting it to null prevents the 500 Internal Server Error.
                habit.setFrequency(null);
            }
        } else {
            habit.setFrequency(null); // Explicitly handle null from DB
        }
        // END FIX 1

        habit.setIsActive(rs.getBoolean("is_active"));
        habit.setCreatedAt(rs.getTimestamp("created_at") != null ?
                rs.getTimestamp("created_at").toLocalDateTime() : null);
        habit.setUpdatedAt(rs.getTimestamp("updated_at") != null ?
                rs.getTimestamp("updated_at").toLocalDateTime() : null);
        habit.setCreatedBy(rs.getString("created_by"));
        habit.setUpdatedBy(rs.getString("updated_by"));
        return habit;
    };

    public HabitDaoImpl(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedJdbcTemplate = namedJdbcTemplate;
    }

    @Override
    public Habit save(Habit habit) {
        String sql = "INSERT INTO habits (user_id, name, description, frequency, is_active, " +
                "created_at, updated_at, created_by, updated_by) " +
                "VALUES (:userId, :name, :description, :frequency, :isActive, " +
                ":createdAt, :updatedAt, :createdBy, :updatedBy) RETURNING id";

        Map<String, Object> params = new HashMap<>();
        params.put("userId", habit.getUserId());
        params.put("name", habit.getName());
        params.put("description", habit.getDescription());
        params.put("frequency", habit.getFrequency().getValue());
        params.put("isActive", habit.getIsActive());
        params.put("createdAt", habit.getCreatedAt());
        params.put("updatedAt", habit.getUpdatedAt());
        params.put("createdBy", habit.getCreatedBy());
        params.put("updatedBy", habit.getUpdatedBy());

        Long id = namedJdbcTemplate.queryForObject(sql, params, Long.class);
        habit.setId(id);
        return habit;
    }

    @Override
    public Habit update(Habit habit) {
        String sql = "UPDATE habits SET " +
                "name = :name, " +
                "description = :description, " +
                "frequency = :frequency, " +
                "is_active = :isActive, " +
                "updated_at = :updatedAt, " +
                "updated_by = :updatedBy " +
                "WHERE id = :id";

        Map<String, Object> params = new HashMap<>();
        params.put("id", habit.getId());
        params.put("name", habit.getName());
        params.put("description", habit.getDescription());
        params.put("frequency", habit.getFrequency().getValue());
        params.put("isActive", habit.getIsActive());
        params.put("updatedAt", habit.getUpdatedAt());
        params.put("updatedBy", habit.getUpdatedBy());

        int updated = namedJdbcTemplate.update(sql, params);
        if (updated == 0) {
            throw new EntityNotFoundException("Habit not found: " + habit.getId());
        }

        return habit;
    }

    @Override
    public Optional<Habit> findById(Long id) {
        String sql = "SELECT * FROM habits WHERE id = :id";

        Map<String, Object> params = new HashMap<>();
        params.put("id", id);

        try {
            Habit habit = namedJdbcTemplate.queryForObject(sql, params, HABIT_ROW_MAPPER);
            return Optional.of(habit);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Habit> findByUserIdAndName(Long userId, String name) {
        String sql = "SELECT * FROM habits WHERE user_id = :userId AND name = :name";

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("name", name);

        try {
            Habit habit = namedJdbcTemplate.queryForObject(sql, params, HABIT_ROW_MAPPER);
            return Optional.of(habit);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Habit> findByUserId(Long userId, Boolean isActive, String frequency,
                                    int offset, int limit, String sortBy, String sortDirection) {
        // FIX 2: Handle null sort parameters defensively to prevent NullPointerException

        // Safely resolve sortDirection to avoid NullPointerException if no parameter is passed.
        String resolvedSortDirection = (sortDirection == null) ? "DESC" : sortDirection;

        // SQL Injection Protection - whitelist validation
        String safeSortBy = (sortBy != null && ALLOWED_SORT_FIELDS.contains(sortBy)) ? sortBy : "created_at";

        // Use the resolvedSortDirection to prevent NPE when calling toUpperCase() on a null input.
        String safeSortDirection = ALLOWED_DIRECTIONS.contains(resolvedSortDirection.toUpperCase())
                ? resolvedSortDirection.toUpperCase()
                : "DESC";

        // END FIX 2

        StringBuilder sql = new StringBuilder("SELECT * FROM habits WHERE user_id = :userId");
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);

        if (isActive != null) {
            sql.append(" AND is_active = :isActive");
            params.put("isActive", isActive);
        }
        if (frequency != null) {
            sql.append(" AND frequency = :frequency");
            params.put("frequency", frequency);
        }

        sql.append(" ORDER BY ").append(safeSortBy).append(" ").append(safeSortDirection);
        sql.append(" LIMIT :limit OFFSET :offset");
        params.put("limit", limit);
        params.put("offset", offset);

        return namedJdbcTemplate.query(sql.toString(), params, HABIT_ROW_MAPPER);
    }

    @Override
    public long countByUserId(Long userId, Boolean isActive, String frequency) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM habits WHERE user_id = :userId");
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);

        if (isActive != null) {
            sql.append(" AND is_active = :isActive");
            params.put("isActive", isActive);
        }
        if (frequency != null) {
            sql.append(" AND frequency = :frequency");
            params.put("frequency", frequency);
        }

        Long count = namedJdbcTemplate.queryForObject(sql.toString(), params, Long.class);
        return count != null ? count : 0L;
    }
}