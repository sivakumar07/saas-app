package com.example.saas.controller;

import com.github.sonus21.rqueue.core.RqueueMessageEnqueuer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/load-test")
@AllArgsConstructor
@Slf4j
public class LoadTestController {

    private final RqueueMessageEnqueuer rqueueMessageEnqueuer;
    private final Random random = new Random();

    @PostMapping("/enqueue")
    public ResponseEntity<String> enqueueJobs(@RequestBody Map<String, Integer> request) {
        int numJobs = request.getOrDefault("numJobs", 1);
        log.info("Enqueuing {} jobs to testQueue", numJobs);
        for (int i = 0; i < numJobs; i++) {
            int tenantId = random.nextInt(100) + 1;
            TestMessage testMessage = new TestMessage("Test message " + (i + 1), tenantId);
            rqueueMessageEnqueuer.enqueue("testQueue", testMessage);
        }
        return ResponseEntity.ok("Enqueued " + numJobs + " jobs to testQueue");
    }

    public record TestMessage(String message, int tenantId) {}
}
