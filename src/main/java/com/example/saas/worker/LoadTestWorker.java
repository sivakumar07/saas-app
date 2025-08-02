package com.example.saas.worker;

import com.github.sonus21.rqueue.annotation.RqueueListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LoadTestWorker {

    @RqueueListener(value = "testQueue")
    public void onMessage(String message) {
        log.info("Received message: {}", message);
    }
}
