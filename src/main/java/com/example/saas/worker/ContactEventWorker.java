package com.example.saas.worker;

import com.example.saas.context.TenantContext;
import com.example.saas.entity.common.AuditLog;
import com.example.saas.model.ContactEventMessage;
import com.example.saas.repository.common.AuditLogRepository;
import com.example.saas.service.ContactEventPublisher;
import com.github.sonus21.rqueue.annotation.RqueueListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Rqueue listener that processes contact lifecycle events.  When a message
 * arrives the listener sets the {@link TenantContext}, writes an audit
 * log entry to the tenant's shard and then clears the context.  The
 * {@link ContactEventMessage} provides all necessary context to
 * reconstruct the database routing information.
 */
@Service
public class ContactEventWorker {

    private static final Logger logger = LoggerFactory.getLogger(ContactEventWorker.class);

    private final AuditLogRepository auditLogRepository;

    public ContactEventWorker(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @RqueueListener(value = ContactEventPublisher.CONTACT_EVENT_QUEUE)
    public void handleEvent(ContactEventMessage message) {
        // Set the tenant context so that any JPA operations use the correct
        // datasource and user information.
        TenantContext.setContext(message.getTenantId(), message.getShard(), message.getUserId());
        try {
            // Log the event for debugging purposes
            logger.info("Processing contact event type={} recordId={} tenant={} user={} changes={}",
                    message.getEventName(), message.getRecordId(), message.getTenantId(), message.getUserId(), message.getChanges());

            // Persist an audit log entry into the shard.  This records the event
            // type, entity and change details.  Using the AuditLog entity
            // directly ensures the entry is written to the correct shard via
            // the routing datasource.
            AuditLog log = AuditLog.builder()
                    .entityName("Contact")
                    .entityId(message.getRecordId() != null ? message.getRecordId().toString() : null)
                    .action(message.getEventName())
                    .changedBy(message.getUserId())
                    .changedAt(Instant.now())
                    .data(message.getChanges())
                    .build();
            auditLogRepository.save(log);
        } catch (Exception e) {
            logger.error("Error processing contact event", e);
            // Rqueue will handle retries based on its configuration
            throw e;
        } finally {
            TenantContext.clear();
        }
    }
}