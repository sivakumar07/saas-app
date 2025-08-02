package com.example.saas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Enables JPA repositories for the master database.  These repositories
 * operate against the {@code centralEntityManagerFactory} and use
 * {@code centralTransactionManager} for transactions.
 */
@Configuration
@EnableJpaRepositories(
        // The central database stores only master data (tenants, users, shards).
        // Audit logs are now stored in each shard, so the 'common' repository
        // package is scanned by the tenant entity manager instead of the
        // central one.
        basePackages = "com.example.saas.repository.global",
        entityManagerFactoryRef = "centralEntityManagerFactory",
        transactionManagerRef = "centralTransactionManager"
)
public class CentralJpaConfig {
    // configuration marker
}