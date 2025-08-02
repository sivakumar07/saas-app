package com.example.saas.repository.shard;

import com.example.saas.entity.shard.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for {@link Tenant} entities.
 */
@Repository
public interface TenantRepository extends JpaRepository<Tenant, String> {
}
