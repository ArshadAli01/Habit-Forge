package com.habittracker.user.core.dao;

import com.habittracker.user.api.dao.UserDao;
import com.habittracker.user.api.entity.User;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * User DAO Implementation.
 * Uses NamedParameterJdbcTemplate for consistency with Habit module.
 */
@Repository
public class UserDaoImpl implements UserDao {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    private static final RowMapper<User> USER_ROW_MAPPER = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setCreatedAt(rs.getTimestamp("created_at") != null ?
                rs.getTimestamp("created_at").toLocalDateTime() : null);
        user.setUpdatedAt(rs.getTimestamp("updated_at") != null ?
                rs.getTimestamp("updated_at").toLocalDateTime() : null);
        user.setCreatedBy(rs.getString("created_by"));
        user.setUpdatedBy(rs.getString("updated_by"));
        return user;
    };

    public UserDaoImpl(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedJdbcTemplate = namedJdbcTemplate;
    }

    @Override
    public User save(User user) {
        String sql = "INSERT INTO users (name, email, password, created_at, updated_at, created_by, updated_by) " +
                "VALUES (:name, :email, :password, :createdAt, :updatedAt, :createdBy, :updatedBy) RETURNING id";

        Map<String, Object> params = new HashMap<>();
        params.put("name", user.getName());
        params.put("email", user.getEmail());
        params.put("password", user.getPassword());
        params.put("createdAt", user.getCreatedAt());
        params.put("updatedAt", user.getUpdatedAt());
        params.put("createdBy", user.getCreatedBy());
        params.put("updatedBy", user.getUpdatedBy());

        Long id = namedJdbcTemplate.queryForObject(sql, params, Long.class);
        user.setId(id);
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = :id";

        Map<String, Object> params = new HashMap<>();
        params.put("id", id);

        try {
            User user = namedJdbcTemplate.queryForObject(sql, params, USER_ROW_MAPPER);
            return Optional.of(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = :email";

        Map<String, Object> params = new HashMap<>();
        params.put("email", email.trim().toLowerCase());

        try {
            User user = namedJdbcTemplate.queryForObject(sql, params, USER_ROW_MAPPER);
            return Optional.of(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = :email";

        Map<String, Object> params = new HashMap<>();
        params.put("email", email.trim().toLowerCase());

        Long count = namedJdbcTemplate.queryForObject(sql, params, Long.class);
        return count != null && count > 0;
    }
}