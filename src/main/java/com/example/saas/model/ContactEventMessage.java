package com.example.saas.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Message payload used to communicate contact lifecycle events to the Rqueue
 * queue.  Each message contains the type of event (e.g. CONTACT_CREATED,
 * CONTACT_UPDATED, CONTACT_DELETED), the identifier of the affected contact,
 * an optional JSON blob describing the changes, and the tenant, shard and
 * user identifiers.  The tenant and shard allow the worker to resolve the
 * correct database connection, while the user identifier is included for
 * auditing purposes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactEventMessage implements Serializable {

    private String eventName;
    private Long recordId;
    private String changes;
    private String tenantId;
    private String shard;
    private Long userId;
}