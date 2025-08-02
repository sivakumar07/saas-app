package com.example.saas.repository.global;

import com.example.saas.entity.global.TenantShardMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA repository for {@link TenantShardMapping} entities.
 */
@Repository
public interface TenantShardMappingRepository extends JpaRepository<TenantShardMapping, Long> {
    Optional<TenantShardMapping> findByTenantId(String tenantId);
    boolean existsByTenantId(String tenantId);
}