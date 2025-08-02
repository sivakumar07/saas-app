package com.example.saas.entity.shard;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Represents an application user. Users are stored in the tenant-specific
 * databases.
 */
@Entity
@Table(name = "users")
@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique username used for authentication.
     */
    @Column(unique = true, nullable = false)
    private String username;

    /**
     * Encoded password.  The password is hashed using a {@link
     * org.springframework.security.crypto.password.PasswordEncoder}.
     */
    @Column(nullable = false)
    private String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}