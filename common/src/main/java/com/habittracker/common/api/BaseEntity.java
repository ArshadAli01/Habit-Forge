package com.habittracker.common.api;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.time.Instant;
import java.time.ZoneOffset;

/**
 * UNIVERSAL BASE ENTITY
 *
 * All entities (User, Habit, etc.) extend this.
 * Provides:
 * - Auto-generated Long ID
 * - UTC audit fields (createdAt, updatedAt)
 * - createdBy / updatedBy (default: "system")
 *
 * All timestamps are stored in UTC.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public abstract class BaseEntity {

    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    /**
     * Initialize audit fields on creation.
     * Called in service before save().
     */
    public void initializeAuditFields(String actor) {
        LocalDateTime now = Instant.now().atOffset(ZoneOffset.UTC).toLocalDateTime();
        this.createdAt = now;
        this.updatedAt = now;
        this.createdBy = actor != null ? actor : "system";
        this.updatedBy = actor != null ? actor : "system";
    }

    /**
     * Update timestamp and updatedBy on modification.
     */
    public void updateAuditFields(String actor) {
        this.updatedAt = Instant.now().atOffset(ZoneOffset.UTC).toLocalDateTime();
        this.updatedBy = actor != null ? actor : "system";
    }

    /**
     * Update only timestamp (for domain actions like activate/deactivate).
     */
    public void updateTimestamp() {
        this.updatedAt = Instant.now().atOffset(ZoneOffset.UTC).toLocalDateTime();
    }
}