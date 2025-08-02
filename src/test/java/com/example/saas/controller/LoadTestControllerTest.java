package com.example.saas.controller;

import com.github.sonus21.rqueue.core.RqueueMessageEnqueuer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class LoadTestControllerTest {

    @Mock
    private RqueueMessageEnqueuer rqueueMessageEnqueuer;

    @InjectMocks
    private LoadTestController loadTestController;

    @Test
    public void testEnqueueJobs() {
        int numJobs = 5;
        Map<String, Integer> request = Collections.singletonMap("numJobs", numJobs);

        ResponseEntity<String> response = loadTestController.enqueueJobs(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Enqueued " + numJobs + " jobs to testQueue", response.getBody());
        verify(rqueueMessageEnqueuer, times(numJobs)).enqueue(eq("testQueue"), any(LoadTestController.TestMessage.class));
    }
}
