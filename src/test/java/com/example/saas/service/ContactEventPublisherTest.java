package com.example.saas.service;

import com.example.saas.context.TenantContext;
import com.example.saas.model.ContactEventMessage;
import com.github.sonus21.rqueue.core.RqueueMessageEnqueuer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

class ContactEventPublisherTest {

    @Mock
    private RqueueMessageEnqueuer rqueueMessageEnqueuer;

    @InjectMocks
    private ContactEventPublisher contactEventPublisher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        TenantContext.clear();
    }

    @Test
    void publish() {
        TenantContext.setContext("test-tenant", "test-shard", 1L);

        contactEventPublisher.publish("CONTACT_CREATED", 123L, "{}");

        ArgumentCaptor<ContactEventMessage> messageCaptor = ArgumentCaptor.forClass(ContactEventMessage.class);
        verify(rqueueMessageEnqueuer).enqueue(ArgumentCaptor.forClass(String.class).capture(), messageCaptor.capture());

        assertEquals(ContactEventPublisher.CONTACT_EVENT_QUEUE, "contactEventQueue");
        ContactEventMessage capturedMessage = messageCaptor.getValue();
        assertEquals("CONTACT_CREATED", capturedMessage.getEventName());
        assertEquals(123L, capturedMessage.getRecordId());
        assertEquals("{}", capturedMessage.getChanges());
        assertEquals("test-tenant", capturedMessage.getTenantId());
        assertEquals("test-shard", capturedMessage.getShard());
        assertEquals(1L, capturedMessage.getUserId());
    }
}
