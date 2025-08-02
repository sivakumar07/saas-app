package com.example.saas.service;

import com.example.saas.context.TenantContext;
import com.example.saas.model.ContactEventMessage;
import com.github.sonus21.rqueue.core.RqueueMessageEnqueuer;
import org.springframework.stereotype.Service;

/**
 * Publishes contact lifecycle events to a dedicated Rqueue queue.  Each
 * event contains the event type, the contact identifier and an optional
 * JSON description of the changes.  The tenant, shard and user
 * identifiers are taken from the {@link TenantContext} so that the
 * worker can operate in the correct database context.
 */
@Service
public class ContactEventPublisher {

    /**
     * Name of the queue used for contact events.  This queue is
     * processed by {@link com.example.saas.worker.ContactEventWorker}.
     */
    public static final String CONTACT_EVENT_QUEUE = "contactEventQueue";

    private final RqueueMessageEnqueuer messageSender;

    public ContactEventPublisher(RqueueMessageEnqueuer messageSender) {
        this.messageSender = messageSender;
    }

    /**
     * Publish a contact lifecycle event.  The current tenant, shard and
     * user identifiers are automatically included in the message.  The
     * message is enqueued on the contact event queue for asynchronous
     * processing.
     *
     * @param eventName the event type (e.g. CONTACT_CREATED, CONTACT_UPDATED, CONTACT_DELETED)
     * @param recordId  the identifier of the affected contact
     * @param changes   a JSON representation of the state change
     */
    public void publish(String eventName, Long recordId, String changes) {
        String tenantId = TenantContext.getTenantId();
        String shard = TenantContext.getShard();
        Long userId = TenantContext.getUserId();
        ContactEventMessage message = new ContactEventMessage(eventName, recordId, changes,
                tenantId, shard, userId);
        messageSender.enqueue(CONTACT_EVENT_QUEUE, message);
    }
}