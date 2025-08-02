package com.example.saas.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;
    // Use a 256-bit key for HS256
    private final String secret = "c2VjdXJlLXNlY3JldC1rZXktZm9yLXRlc3RpbmctcHVycG9zZS1vbmx5LTEyMzQ1"; // secure-secret-key-for-testing-purpose-only-12345

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils(secret, 3600000);
    }

    @Test
    void testGenerateAndParseToken() {
        // Given
        Map<String, Object> claims = Collections.singletonMap("tenantId", "test-tenant");
        String subject = "test-user";

        // When
        String token = jwtUtils.generateToken(claims, subject);
        Claims parsedClaims = jwtUtils.parseToken(token);

        // Then
        assertEquals(subject, parsedClaims.getSubject());
        assertEquals("test-tenant", parsedClaims.get("tenantId"));
    }

    @Test
    void testTokenExpiration() throws InterruptedException {
        // Given
        JwtUtils expiredJwtUtils = new JwtUtils(secret, 1);
        String token = expiredJwtUtils.generateToken(Collections.emptyMap(), "test-user");

        // When
        Thread.sleep(10); // Wait for the token to expire

        // Then
        assertThrows(ExpiredJwtException.class, () -> expiredJwtUtils.parseToken(token));
    }

    @Test
    void testInvalidSignature() {
        // Given
        String otherSecret = "YW5vdGhlci1zZWN1cmUtc2VjcmV0LWtleS1mb3ItdGVzdGluZy1wdXJwb3NlLTU0MzIx"; // another-secure-secret-key-for-testing-purpose-54321
        JwtUtils otherJwtUtils = new JwtUtils(otherSecret, 3600000);
        String token = jwtUtils.generateToken(Collections.emptyMap(), "test-user");

        // When & Then
        assertThrows(SignatureException.class, () -> otherJwtUtils.parseToken(token));
    }

    @Test
    void testGetUsernameFromToken() {
        // Given
        String token = jwtUtils.generateToken(Collections.emptyMap(), "test-user");

        // When
        String username = jwtUtils.getUsernameFromToken(token);

        // Then
        assertEquals("test-user", username);
    }
}
