package com.habittracker.common.utility;

import com.habittracker.common.api.BaseEntity;

/**
 * CENTRAL AUDIT HELPER
 *
 * Use in services to avoid duplication.
 */
public class AuditFieldUtility {

    public static <T extends BaseEntity> void initialize(T entity, String actor) {
        entity.initializeAuditFields(actor);
    }

    public static <T extends BaseEntity> void update(T entity, String actor) {
        entity.updateAuditFields(actor);
    }
}