package com.example.saas.entity.common;

import com.example.saas.context.TenantContext;
import com.example.saas.service.AuditLogService;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;

import java.time.Instant;

/**
 * JPA entity listener that populates tenant and audit fields based on the
 * current {@link TenantContext}.  This listener complements Spring Data
 * auditing by setting the tenant ID and handling soft deletion.  It is
 * registered via {@link jakarta.persistence.EntityListeners} on the
 * {@link AuditableEntity} superclass.
 */
public class TenantEntityListener {

    @PrePersist
    public void onPrePersist(Object target) {
        if (target instanceof AuditableEntity entity) {
            // createdBy and updatedBy fields are handled by Spring Data
            // auditing via @CreatedBy and @LastModifiedBy.  We still set
            // updatedBy here in case auditing is not applied.
            Long userId = TenantContext.getUserId();
            if (entity.getCreatedBy() == null) {
                entity.setCreatedBy(userId);
            }
            entity.setUpdatedBy(userId);
        }
    }

    @PreUpdate
    public void onPreUpdate(Object target) {
        if (target instanceof AuditableEntity entity) {
            // Update the updatedBy field from context.  The updatedAt field
            // is handled by Spring Data auditing (@LastModifiedDate).
            Long userId = TenantContext.getUserId();
            entity.setUpdatedBy(userId);
        }
    }

    @PreRemove
    public void onPreRemove(Object target) {
        if (target instanceof AuditableEntity entity) {
            // Instead of deleting the record, mark it as deleted.  The
            // repository/service should avoid calling EntityManager.remove.
            Long userId = TenantContext.getUserId();
            entity.setDeletedBy(userId);
            entity.setDeletedAt(Instant.now());
        }
    }

    @PostPersist
    public void onPostPersist(Object target) {
        if (target instanceof AuditableEntity) {
            // After insert, record an audit log entry.  Use the entity's class name
            // and its ID (if available) as the identifier.  The tenant ID is
            // already set on the entity during pre‑persist.
            String tenantId = TenantContext.getTenantId();
            Long userId = TenantContext.getUserId();
            String entityName = target.getClass().getSimpleName();
            String entityId = extractId(target);
            AuditLogService.record(tenantId, entityName, entityId, "INSERT", userId);
        }
    }

    @PostUpdate
    public void onPostUpdate(Object target) {
        if (target instanceof AuditableEntity) {
            String tenantId = TenantContext.getTenantId();
            Long userId = TenantContext.getUserId();
            String entityName = target.getClass().getSimpleName();
            String entityId = extractId(target);
            AuditLogService.record(tenantId, entityName, entityId, "UPDATE", userId);
        }
    }

    @PostRemove
    public void onPostRemove(Object target) {
        if (target instanceof AuditableEntity) {
            String tenantId = TenantContext.getTenantId();
            Long userId = TenantContext.getUserId();
            String entityName = target.getClass().getSimpleName();
            String entityId = extractId(target);
            AuditLogService.record(tenantId, entityName, entityId, "DELETE", userId);
        }
    }

    /**
     * Extracts the identifier from an audited entity via reflection.  JPA
     * entities typically define a field named "id".  If the field is not
     * accessible or missing, returns null.
     */
    private String extractId(Object entity) {
        try {
            var field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            Object value = field.get(entity);
            return value != null ? value.toString() : null;
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            return null;
        }
    }
}