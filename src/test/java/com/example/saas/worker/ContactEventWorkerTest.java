package com.example.saas.worker;

import com.example.saas.context.TenantContext;
import com.example.saas.entity.common.AuditLog;
import com.example.saas.model.ContactEventMessage;
import com.example.saas.repository.common.AuditLogRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactEventWorkerTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private ContactEventWorker contactEventWorker;

    @BeforeEach
    void setUp() {
        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testHandleEvent() {
        // Given
        ContactEventMessage message = new ContactEventMessage();
        message.setTenantId("test-tenant");
        message.setShard("test-shard");
        message.setUserId(1L);
        message.setEventName("CONTACT_CREATED");
        message.setRecordId(123L);
        message.setChanges("{\"field\": \"value\"}");

        // When
        contactEventWorker.handleEvent(message);

        // Then
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog savedLog = captor.getValue();
        assertEquals("Contact", savedLog.getEntityName());
        assertEquals("123", savedLog.getEntityId());
        assertEquals("CONTACT_CREATED", savedLog.getAction());
        assertEquals(1L, savedLog.getChangedBy());
        assertEquals("{\"field\": \"value\"}", savedLog.getData());

        assertNull(TenantContext.getTenantId(), "TenantContext should be cleared");
    }

    @Test
    void testHandleEventWithException() {
        // Given
        ContactEventMessage message = new ContactEventMessage();
        message.setTenantId("test-tenant");
        message.setShard("test-shard");

        when(auditLogRepository.save(any(AuditLog.class))).thenThrow(new RuntimeException("DB error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> contactEventWorker.handleEvent(message));
        assertNull(TenantContext.getTenantId(), "TenantContext should be cleared even on exception");
    }
}
