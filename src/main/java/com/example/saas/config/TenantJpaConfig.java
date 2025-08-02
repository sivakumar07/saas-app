package com.example.saas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Enables JPA repositories for tenant‑scoped entities.  These repositories
 * operate against the routing data source and use the tenant entity manager.
 */
@Configuration
@EnableJpaRepositories(
        // Scan tenant repositories (contacts) and common repositories (audit logs)
        // using the tenant entity manager so that these entities are stored
        // in the shard databases instead of the central database.
        basePackages = {"com.example.saas.repository.shard", "com.example.saas.repository.common"},
        entityManagerFactoryRef = "tenantEntityManagerFactory",
        transactionManagerRef = "tenantTransactionManager"
)
public class TenantJpaConfig {
    // configuration marker
}