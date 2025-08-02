package com.example.saas.controller;

import com.example.saas.dto.TaskRequest;
import com.example.saas.service.TaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
public class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    @Test
    public void testScheduleTask() {
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setName("testTask");
        taskRequest.setPayload("testPayload");
        doNothing().when(taskService).enqueue(taskRequest);

        ResponseEntity<Void> response = taskController.scheduleTask("tenant1", taskRequest);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
    }
}