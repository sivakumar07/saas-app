package com.example.saas.config;

import com.example.saas.context.TenantContext;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuditingConfigTest {

    @Test
    void testAuditorAware() {
        AuditingConfig auditingConfig = new AuditingConfig();
        AuditorAware<Long> auditorAware = auditingConfig.auditorAware();

        TenantContext.setContext("test-tenant", "test-shard", 1L);
        Optional<Long> currentAuditor = auditorAware.getCurrentAuditor();
        assertEquals(Optional.of(1L), currentAuditor);

        TenantContext.clear();
        currentAuditor = auditorAware.getCurrentAuditor();
        assertEquals(Optional.empty(), currentAuditor);
    }
}