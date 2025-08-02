package com.example.saas.repository.common;

import com.example.saas.entity.common.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for persisting {@link AuditLog} entries.  Audit logs are
 * written by the {@link com.example.saas.service.AuditLogService} when
 * entities are created, updated or deleted.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}