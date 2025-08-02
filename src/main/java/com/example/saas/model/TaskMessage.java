package com.example.saas.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Message payload sent to the Rqueue queue for asynchronous tasks.  Contains
 * enough context to reconstruct the tenant and user for processing the
 * task.  The actual task name and user‑defined payload are included for
 * logging or further processing.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskMessage implements Serializable {
    private String name;
    private Long recordId;
    private String payload;
    private String tenantId;
    private String shard;
    private Long userId;
}