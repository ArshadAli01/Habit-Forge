package com.habittracker.habit.core.dao;

import com.habittracker.habit.api.dao.HabitDao;
import com.habittracker.habit.api.entity.Habit;
import com.habittracker.common.utility.AuditFieldUtility;
import com.habittracker.habit.api.enums.Frequency;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap; // Imported HashMap
import java.util.List;
import java.util.Map;
import java.util.Optional;
// Removed Unused Import if any was causing a warning.

/**
 * Habit DAO Implementation.
 * Uses NamedParameterJdbcTemplate for filtered queries; SqlDaoHelper pattern for save/update.
 * RowMapper for Habit.
 */
@Repository
public class HabitDaoImpl implements HabitDao {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    // RowMapper for Habit (No change needed here)
    private static final RowMapper<Habit> HABIT_ROW_MAPPER = (rs, rowNum) -> {
        Habit habit = new Habit();
        habit.setId(rs.getLong("id"));
        habit.setUserId(rs.getLong("user_id"));
        habit.setName(rs.getString("name"));
        habit.setDescription(rs.getString("description"));
        habit.setFrequency(Frequency.valueOf(rs.getString("frequency")));
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
        AuditFieldUtility.initialize(habit, "system");
        String sql = "INSERT INTO habits (user_id, name, description, frequency, is_active, created_at, updated_at, created_by, updated_by) " +
                "VALUES (:userId, :name, :description, :frequency, :isActive, :createdAt, :updatedAt, :createdBy, :updatedBy) RETURNING id";

        // FIX: Replaced Map.of() (Java 9+) with new HashMap()
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
    public Optional<Habit> findById(Long id) {
        String sql = "SELECT * FROM habits WHERE id = :id";

        // FIX: Replaced Map.of() (Java 9+) with new HashMap()
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);

        try {
            Habit habit = namedJdbcTemplate.queryForObject(sql, params, HABIT_ROW_MAPPER);
            return Optional.of(habit);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // findByUserId and countByUserId already fixed in previous response.

    @Override
    public List<Habit> findByUserId(Long userId, Boolean isActive, String frequency, int offset, int limit, String sortBy, String sortDirection) {
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

        sql.append(" ORDER BY ").append(sortBy).append(" ").append(sortDirection).append(" LIMIT :limit OFFSET :offset");
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

        return namedJdbcTemplate.queryForObject(sql.toString(), params, Long.class);
    }

    @Override
    public int softDeleteById(Long id) {
        String sql = "UPDATE habits SET is_active = false, updated_at = CURRENT_TIMESTAMP, updated_by = 'system' WHERE id = :id AND is_active = true";

        // FIX: Replaced Map.of() (Java 9+) with new HashMap()
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);

        return namedJdbcTemplate.update(sql, params);
    }
}