package com.example.saas.service;

import com.example.saas.entity.common.AuditLog;
import com.example.saas.repository.common.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void record_success() {
        AuditLogService.record("tenant1", "User", "123", "INSERT", 1L);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog savedLog = captor.getValue();
        assertEquals("tenant1", savedLog.getTenantId());
        assertEquals("User", savedLog.getEntityName());
        assertEquals("123", savedLog.getEntityId());
        assertEquals("INSERT", savedLog.getAction());
        assertEquals(1L, savedLog.getChangedBy());
    }

    @Test
    void record_nullRepository() {
        ReflectionTestUtils.setField(AuditLogService.class, "staticRepository", null);

        AuditLogService.record("tenant1", "User", "123", "INSERT", 1L);

        verify(auditLogRepository, never()).save(org.mockito.ArgumentMatchers.any(AuditLog.class));
    }
}
