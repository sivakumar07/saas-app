package com.example.saas.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for creating a JWT token.  The user supplies their username,
 * password, and tenant ID.
 */
@Data
public class LoginRequest {
    @NotBlank
    private String tenantId;
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}