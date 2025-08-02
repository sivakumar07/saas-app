package com.example.saas.service;

import com.example.saas.context.TenantContext;
import com.example.saas.dto.TaskRequest;
import com.example.saas.model.TaskMessage;
import com.github.sonus21.rqueue.core.RqueueMessageEnqueuer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

class TaskServiceTest {

    @Mock
    private RqueueMessageEnqueuer rqueueMessageEnqueuer;

    @InjectMocks
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        TenantContext.clear();
    }

    @Test
    void enqueue() {
        TenantContext.setContext("test-tenant", "test-shard", 1L);
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setName("test-task");
        taskRequest.setRecordId(123L);
        taskRequest.setPayload("{}");

        taskService.enqueue(taskRequest);

        ArgumentCaptor<TaskMessage> messageCaptor = ArgumentCaptor.forClass(TaskMessage.class);
        verify(rqueueMessageEnqueuer).enqueue(ArgumentCaptor.forClass(String.class).capture(), messageCaptor.capture());

        TaskMessage capturedMessage = messageCaptor.getValue();
        assertEquals("test-task", capturedMessage.getName());
        assertEquals(123L, capturedMessage.getRecordId());
        assertEquals("{}", capturedMessage.getPayload());
        assertEquals("test-tenant", capturedMessage.getTenantId());
        assertEquals("test-shard", capturedMessage.getShard());
        assertEquals(1L, capturedMessage.getUserId());
    }
}
