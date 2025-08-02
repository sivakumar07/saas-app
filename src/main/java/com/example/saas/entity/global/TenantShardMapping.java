package com.example.saas.entity.global;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Represents the mapping of a tenant to a database shard.
 */
@Entity
@Table(name = "tenant_shard_mappings")
@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class TenantShardMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * A globally unique identifier for this tenant.
     */
    @Column(name = "tenant_id", unique = true, nullable = false)
    private String tenantId;

    /**
     * The shard key that identifies which database holds this tenant's data.
     */
    @Column(nullable = false)
    private String shard;

    public TenantShardMapping(String tenantId, String shard) {
        this.tenantId = tenantId;
        this.shard = shard;
    }
}