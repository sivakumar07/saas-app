package com.example.saas.worker;

import com.example.saas.context.TenantContext;
import com.example.saas.model.TaskMessage;
import com.github.sonus21.rqueue.annotation.RqueueListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Listener for asynchronous tasks scheduled via Rqueue.  When a task
 * message arrives it sets the {@link TenantContext}, logs the task
 * details and then clears the context.  In a more sophisticated
 * implementation this class could trigger domain logic or call external
 * services.
 */
@Service
public class TaskWorker {

    private static final Logger logger = LoggerFactory.getLogger(TaskWorker.class);

    @RqueueListener(value = "taskQueue")
    public void handleTask(TaskMessage message) {
        // Set the tenant context so that any database operations occur on
        // the correct shard.  Even though this example only logs the
        // message, a real implementation might perform business logic.
        TenantContext.setContext(message.getTenantId(), message.getShard(), message.getUserId());
        try {
            logger.info("Processing task name={} recordId={} tenant={} user={} payload={}",
                    message.getName(), message.getRecordId(), message.getTenantId(), message.getUserId(), message.getPayload());
        } finally {
            TenantContext.clear();
        }
    }
}