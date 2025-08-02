package com.example.saas;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import redis.embedded.RedisServer;

/**
 * Entry point for the multi‑tenant SaaS application.
 *
 * <p>This application uses several technologies:
 * <ul>
 *   <li><strong>Spring Boot 3.3.1</strong> – a recent stable release.</li>
 *   <li><strong>Java 21</strong> – The application is compiled and runs on Java 21.</li>
 *   <li><strong>Rqueue</strong> for asynchronous task processing.  Rqueue
 *       requires a running Redis instance.  To simplify local deployment,
 *       the application starts an embedded Redis server on port 6379 at
 *       startup using the {@code embedded‑redis} library.</li>
 *   <li><strong>PostgreSQL</strong> for persistence.  A central database and
 *       multiple shard databases are used to isolate tenant data.</li>
 * </ul>
 *
 * <p>When the application starts, an embedded Redis server is started.  At
 * shutdown the server is stopped to free resources.</p>
 */
@SpringBootApplication
public class SaasApplication {

    private RedisServer redisServer;

    public static void main(String[] args) {
        SpringApplication.run(SaasApplication.class, args);
    }

    /**
     * Starts an embedded Redis server on the default port (6379) if no
     * external Redis instance is available.  Rqueue uses Redis under
     * the hood, so having a local instance ensures that background jobs
     * are processed without requiring additional infrastructure.
     */
    @PostConstruct
    public void startRedis() {
        try {
            redisServer = RedisServer.builder().port(6379).build();
            redisServer.start();
        } catch (Exception ex) {
            // If starting Redis fails (e.g. because a server is already
            // running), log the exception but allow the application to
            // continue.  Rqueue will connect to the existing server.
            System.err.println("Failed to start embedded Redis: " + ex.getMessage());
        }
    }

    /**
     * Stops the embedded Redis server when the application shuts down.  This
     * method is invoked automatically by the Spring container.
     */
    @PreDestroy
    public void stopRedis() {
        if (redisServer != null) {
            redisServer.stop();
        }
    }
}