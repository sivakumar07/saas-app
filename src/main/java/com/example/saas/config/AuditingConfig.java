package com.example.saas.config;

import com.example.saas.context.TenantContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * Enables JPA auditing and provides an {@link AuditorAware} implementation
 * that resolves the current user from {@link TenantContext}.  The auditing
 * infrastructure will call {@code getCurrentAuditor()} to populate the
 * {@code createdBy} and {@code updatedBy} fields of auditable entities.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class AuditingConfig {

    @Bean
    public AuditorAware<Long> auditorAware() {
        return () -> Optional.ofNullable(TenantContext.getUserId());
    }
}