package com.example.saas.entity.shard;

import com.example.saas.entity.common.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;

/**
 * A simple contact entity.  Contacts are scoped to a tenant and stored in
 * the tenant‑sharded database.  Each contact row contains the tenant ID
 * explicitly to prevent data leakage across tenants.
 */
@Entity
@Table(name = "contacts")
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.NoArgsConstructor
@AllArgsConstructor
public class Contact extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;
}