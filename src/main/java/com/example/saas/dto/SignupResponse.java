package com.example.saas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response returned after a successful signup.
 */
@Data
@AllArgsConstructor
public class SignupResponse {
    private String tenantId;
    private String username;
}