package com.example.saas.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for tenant signup.  Contains the tenant name and the admin
 * user's credentials.
 */
@Data
public class SignupRequest {
    @NotBlank
    private String tenantName;
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}