package com.example.saas.service;

import com.example.saas.dto.LoginRequest;
import com.example.saas.dto.LoginResponse;
import com.example.saas.entity.global.TenantShardMapping;
import com.example.saas.entity.shard.User;
import com.example.saas.repository.global.TenantShardMappingRepository;
import com.example.saas.repository.shard.UserRepository;
import com.example.saas.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TenantShardMappingRepository tenantShardMappingRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void login_success() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setTenantId("tenant1");
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        TenantShardMapping tenantShardMapping = new TenantShardMapping();
        tenantShardMapping.setTenantId("tenant1");
        tenantShardMapping.setShard("shard1");
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("encodedPassword");

        when(tenantShardMappingRepository.findByTenantId("tenant1")).thenReturn(Optional.of(tenantShardMapping));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(jwtUtils.generateToken(any(), any())).thenReturn("test_token");

        LoginResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("test_token", response.getToken());
    }

    @Test
    void login_invalidTenant() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setTenantId("invalid_tenant");
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        when(tenantShardMappingRepository.findByTenantId("invalid_tenant")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.login(loginRequest));
    }

    @Test
    void login_invalidUsername() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setTenantId("tenant1");
        loginRequest.setUsername("invalid_user");
        loginRequest.setPassword("password");

        TenantShardMapping tenantShardMapping = new TenantShardMapping();
        tenantShardMapping.setTenantId("tenant1");
        tenantShardMapping.setShard("shard1");

        when(tenantShardMappingRepository.findByTenantId("tenant1")).thenReturn(Optional.of(tenantShardMapping));
        when(userRepository.findByUsername("invalid_user")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.login(loginRequest));
    }

    @Test
    void login_invalidPassword() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setTenantId("tenant1");
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrong_password");

        TenantShardMapping tenantShardMapping = new TenantShardMapping();
        tenantShardMapping.setTenantId("tenant1");
        tenantShardMapping.setShard("shard1");
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("encodedPassword");

        when(tenantShardMappingRepository.findByTenantId("tenant1")).thenReturn(Optional.of(tenantShardMapping));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong_password", "encodedPassword")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.login(loginRequest));
    }
}