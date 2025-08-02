package com.example.saas.worker;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class LoadTestWorkerTest {

    @Test
    void onMessage() {
        LoadTestWorker worker = new LoadTestWorker();
        assertDoesNotThrow(() -> worker.onMessage("Test message"));
    }
}
