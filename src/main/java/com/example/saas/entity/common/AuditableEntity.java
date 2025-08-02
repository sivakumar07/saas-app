package com.example.saas.entity.common;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Base class for all entities that need tenant, auditing and soft deletion
 * fields.  It uses Spring Data's auditing annotations to populate the
 * {@code createdBy}, {@code updatedBy}, {@code createdAt} and
 * {@code updatedAt} fields automatically.  A custom entity listener
 * {@link com.example.saas.entity.common.TenantEntityListener} sets the
 * current tenant ID and handles soft deletion.  The {@link Where}
 * annotation ensures that soft‑deleted records are filtered out of all
 * queries automatically.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@EntityListeners({AuditingEntityListener.class, TenantEntityListener.class})
@Where(clause = "deleted_at IS NULL")
public abstract class AuditableEntity {

    /**
     * Identifier of the user who created this record.  Populated by
     * Spring Data auditing via {@link CreatedBy}.  May be null if no
     * authenticated user is available (e.g., during system processes).
     */
    @CreatedBy
    @Column(name = "created_by")
    private Long createdBy;

    /**
     * Identifier of the user who last updated this record.  Populated by
     * Spring Data auditing via {@link LastModifiedBy}.
     */
    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;

    /**
     * Timestamp of when this record was created.  Populated by Spring Data
     * auditing via {@link CreatedDate}.
     */
    @CreatedDate
    @Column(name = "created_at")
    private Instant createdAt;

    /**
     * Timestamp of when this record was last updated.  Populated by Spring
     * Data auditing via {@link LastModifiedDate}.
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    /**
     * Timestamp of when this record was soft‑deleted.  Null if not deleted.
     */
    @Column(name = "deleted_at")
    private Instant deletedAt;

    /**
     * Identifier of the user who soft‑deleted this record.  Null if not
     * deleted.
     */
    @Column(name = "deleted_by")
    private Long deletedBy;
}