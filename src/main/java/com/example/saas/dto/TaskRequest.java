package com.example.saas.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for scheduling an asynchronous task.  The task name is an
 * arbitrary label and the payload contains user‑defined data.  All tasks
 * scheduled through this API are enqueued to Rqueue and processed
 * asynchronously.
 */
@Data
public class TaskRequest {

    /**
     * Name of the task or job type.  For example, "SendWelcomeEmail" or
     * "RecalculateContactScore".  This value should be used by the worker
     * to determine how to process the record referenced by {@link #recordId}.
     */
    @NotBlank
    private String name;

    /**
     * Identifier of the domain record associated with this task.  For
     * example, when scheduling a task to send a welcome email to a contact
     * this would contain the contact’s ID.  A null value may be used for
     * tasks that are not associated with a specific record.
     */
    private Long recordId;

    /**
     * Arbitrary payload containing additional data for the task.  This
     * field is optional and may be left blank if not needed.
     */
    private String payload;
}