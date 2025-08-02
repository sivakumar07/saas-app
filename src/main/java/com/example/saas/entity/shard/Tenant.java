package com.example.saas.entity.shard;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;

/**
 * Represents a tenant in the system, stored in the tenant's own database shard.
 */
@Entity
@Table(name = "tenants")
@lombok.Data
@lombok.NoArgsConstructor
@AllArgsConstructor
public class Tenant {

    @Id
    private String id;

    private String name;
}