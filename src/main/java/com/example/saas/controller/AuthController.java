package com.example.saas.controller;

import com.example.saas.dto.LoginRequest;
import com.example.saas.dto.LoginResponse;
import com.example.saas.dto.SignupRequest;
import com.example.saas.dto.SignupResponse;
import com.example.saas.service.AuthService;
import com.example.saas.service.TenantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication and tenant registration endpoints.
 */
@RestController
public class AuthController {

    private final TenantService tenantService;
    private final AuthService authService;

    public AuthController(TenantService tenantService, AuthService authService) {
        this.tenantService = tenantService;
        this.authService = authService;
    }

    /**
     * Registers a new tenant and admin user.  This endpoint is public and
     * requires no authentication.
     */
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = tenantService.signup(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Authenticates a user and returns a JWT token.  This endpoint is public.
     */
    @PostMapping("/token")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}