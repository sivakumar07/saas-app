package com.example.saas.worker;

import com.example.saas.context.TenantContext;
import com.example.saas.model.TaskMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class TaskWorkerTest {

    @InjectMocks
    private TaskWorker taskWorker;

    @BeforeEach
    void setUp() {
        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testHandleTask() {
        // Given
        TaskMessage message = new TaskMessage();
        message.setTenantId("test-tenant");
        message.setShard("test-shard");
        message.setUserId(1L);
        message.setName("test-task");
        message.setRecordId(123L);
        message.setPayload("test-payload");

        // When
        taskWorker.handleTask(message);

        // Then
        assertNull(TenantContext.getTenantId(), "TenantContext should be cleared after task handling");
        assertNull(TenantContext.getShard(), "TenantContext should be cleared after task handling");
        assertNull(TenantContext.getUserId(), "TenantContext should be cleared after task handling");
    }
}
