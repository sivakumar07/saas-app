package com.example.saas.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

/**
 * Utility class for generating and validating JSON Web Tokens (JWT).  Tokens
 * carry the username, user ID and tenant ID as claims.  A symmetric secret
 * key is used to sign and verify tokens.  The secret should be configured via
 * the {@code jwt.secret} property in {@code application.yml}.
 */
@Component
public class JwtUtils {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtUtils(@Value("${jwt.secret}") String secret,
                    @Value("${jwt.expiration-ms:3600000}") long expirationMs) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    /**
     * Generates a new JWT token with the given claims.  The token expires
     * after {@code expirationMs} milliseconds.
     *
     * @param claims the claims to include in the token
     * @param subject the subject (typically the username)
     * @return a signed JWT as a compact string
     */
    public String generateToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Parses the provided token and returns the claims.  If the token is
     * invalid or expired a runtime exception will be thrown.
     *
     * @param token the JWT token
     * @return the token claims
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extracts the username (subject) from the token.
     */
    public String getUsernameFromToken(String token) {
        return parseToken(token).getSubject();
    }
}