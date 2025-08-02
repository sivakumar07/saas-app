package com.example.saas.controller;

import com.example.saas.dto.TaskRequest;
import com.example.saas.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for scheduling asynchronous tasks.  All endpoints are
 * scoped by tenant via the path variable.  The {@link TaskService}
 * enqueues tasks for processing.
 */
@RestController
@RequestMapping("/{tenantId}/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Schedules a new asynchronous task for the current tenant.  The task
     * name and payload are provided in the request body.  Returns HTTP
     * 202 Accepted to indicate that the task has been enqueued.
     */
    @PostMapping
    public ResponseEntity<Void> scheduleTask(@PathVariable String tenantId, @Valid @RequestBody TaskRequest request) {
        taskService.enqueue(request);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}