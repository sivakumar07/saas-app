package com.example.saas.context;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TenantContextTest {

    @Test
    void testContextMethods() {
        TenantContext.setContext("test-tenant", "test-shard", 1L);
        assertEquals("test-tenant", TenantContext.getTenantId());
        assertEquals("test-shard", TenantContext.getShard());
        assertEquals(1L, TenantContext.getUserId());

        TenantContext.clear();
        assertNull(TenantContext.getTenantId());
        assertNull(TenantContext.getShard());
        assertNull(TenantContext.getUserId());
    }

    @Test
    void testMultiThreadedContext() throws InterruptedException {
        Thread thread1 = new Thread(() -> {
            TenantContext.setContext("tenant1", "shard1", 1L);
            assertEquals("tenant1", TenantContext.getTenantId());
            assertEquals("shard1", TenantContext.getShard());
            assertEquals(1L, TenantContext.getUserId());
        });

        Thread thread2 = new Thread(() -> {
            TenantContext.setContext("tenant2", "shard2", 2L);
            assertEquals("tenant2", TenantContext.getTenantId());
            assertEquals("shard2", TenantContext.getShard());
            assertEquals(2L, TenantContext.getUserId());
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();
    }
}