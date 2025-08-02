package com.example.saas.service;

import com.example.saas.entity.common.AuditLog;
import com.example.saas.repository.common.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Service for recording audit log entries.  This service holds a
 * static reference to the repository so that it can be invoked from
 * entity listeners where dependency injection is not available.
 */
@Service
public class AuditLogService {

    private static AuditLogRepository staticRepository;

    public AuditLogService(AuditLogRepository repository) {
        staticRepository = repository;
    }

    /**
     * Records an audit event.  Called by entity listeners to log
     * persistence operations.
     *
     * @param tenantId   the tenant identifier
     * @param entityName the entity class simple name
     * @param entityId   the entity identifier (may be null)
     * @param action     one of INSERT, UPDATE, DELETE
     * @param userId     the ID of the user performing the action (may be null)
     */
    public static void record(String tenantId, String entityName, String entityId, String action, Long userId) {
        if (staticRepository == null) {
            // Should never happen; the repository is injected via constructor
            return;
        }
        AuditLog log = AuditLog.builder()
                .tenantId(tenantId)
                .entityName(entityName)
                .entityId(entityId)
                .action(action)
                .changedBy(userId)
                .changedAt(Instant.now())
                .build();
        staticRepository.save(log);
    }
}