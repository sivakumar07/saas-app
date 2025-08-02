package com.example.saas.controller;

import com.example.saas.dto.LoginRequest;
import com.example.saas.dto.LoginResponse;
import com.example.saas.dto.SignupRequest;
import com.example.saas.dto.SignupResponse;
import com.example.saas.service.AuthService;
import com.example.saas.service.TenantService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private TenantService tenantService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    public void testSignup_Success() {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setTenantName("test-tenant");
        signupRequest.setUsername("test-user");
        signupRequest.setPassword("password");
        
        SignupResponse signupResponse = new SignupResponse("test-tenant", "test-user");
        when(tenantService.signup(signupRequest)).thenReturn(signupResponse);

        ResponseEntity<SignupResponse> response = authController.signup(signupRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(signupResponse, response.getBody());
    }

    @Test
    public void testLogin_Success() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setTenantId("test-tenant");
        loginRequest.setUsername("test-user");
        loginRequest.setPassword("password");
        
        LoginResponse loginResponse = new LoginResponse("accessToken");
        when(authService.login(loginRequest)).thenReturn(loginResponse);

        ResponseEntity<LoginResponse> response = authController.login(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(loginResponse, response.getBody());
    }

    @Test
    public void testLogin_InvalidCredentials() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setTenantId("test-tenant");
        loginRequest.setUsername("test-user");
        loginRequest.setPassword("wrongpassword");

        when(authService.login(loginRequest)).thenThrow(new BadCredentialsException("Invalid credentials"));

        try {
            authController.login(loginRequest);
        } catch (BadCredentialsException e) {
            assertEquals("Invalid credentials", e.getMessage());
        }
    }
}