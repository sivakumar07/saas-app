package com.example.saas.service;

import com.example.saas.context.TenantContext;
import com.example.saas.dto.TaskRequest;
import com.example.saas.model.TaskMessage;
import com.github.sonus21.rqueue.core.RqueueMessageEnqueuer;
import org.springframework.stereotype.Service;

/**
 * Service that schedules tasks for asynchronous processing via Rqueue.  The
 * tasks are not persisted in a database; instead, they are enqueued to
 * Redis and processed by {@link com.example.saas.worker.TaskWorker}.  The
 * current tenant and user are obtained from {@link TenantContext} so that
 * the worker can reconstruct the context when processing the message.
 */
@Service
public class TaskService {

    private static final String TASK_QUEUE = "taskQueue";

    private final RqueueMessageEnqueuer rqueueMessageSender;

    public TaskService(RqueueMessageEnqueuer rqueueMessageSender) {
        this.rqueueMessageSender = rqueueMessageSender;
    }

    /**
     * Enqueues a new task.  The payload includes the name and payload as
     * provided by the client, along with the tenant, shard and user from
     * {@link TenantContext} so that the worker can process the task within
     * the correct database context.
     *
     * @param request the task details
     */
    public void enqueue(TaskRequest request) {
        String tenantId = TenantContext.getTenantId();
        String shard = TenantContext.getShard();
        Long userId = TenantContext.getUserId();
        TaskMessage message = new TaskMessage(
                request.getName(),
                request.getRecordId(),
                request.getPayload(),
                tenantId,
                shard,
                userId);
        rqueueMessageSender.enqueue(TASK_QUEUE, message);
    }
}