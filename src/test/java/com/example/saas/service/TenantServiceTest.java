package com.example.saas.service;

import com.example.saas.dto.SignupRequest;
import com.example.saas.dto.SignupResponse;
import com.example.saas.entity.global.Shard;
import com.example.saas.entity.global.TenantShardMapping;
import com.example.saas.entity.shard.Tenant;
import com.example.saas.entity.shard.User;
import com.example.saas.repository.global.ShardRepository;
import com.example.saas.repository.global.TenantShardMappingRepository;
import com.example.saas.repository.shard.TenantRepository;
import com.example.saas.repository.shard.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class TenantServiceTest {

    @Mock
    private TenantShardMappingRepository tenantShardMappingRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ShardRepository shardRepository;

    @InjectMocks
    private TenantService tenantService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void signup_success() {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setTenantName("Test Tenant");
        signupRequest.setUsername("testuser");
        signupRequest.setPassword("password");

        Shard shard = new Shard();
        shard.setShardKey("shard1");

        when(shardRepository.findAll()).thenReturn(Collections.singletonList(shard));
        when(tenantShardMappingRepository.existsByTenantId(anyString())).thenReturn(false);
        when(tenantShardMappingRepository.save(any(TenantShardMapping.class))).thenAnswer(i -> i.getArguments()[0]);
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(i -> i.getArguments()[0]);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        SignupResponse response = tenantService.signup(signupRequest);

        assertNotNull(response);
        assertTrue(response.getTenantId().startsWith("test-tenant-"));
        assertEquals("testuser", response.getUsername());
    }

    @Test
    void signup_noShards() {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setTenantName("Test Tenant");
        signupRequest.setUsername("testuser");
        signupRequest.setPassword("password");

        when(shardRepository.findAll()).thenReturn(Collections.emptyList());

        assertThrows(IllegalStateException.class, () -> tenantService.signup(signupRequest));
    }

    @Test
    void signup_tenantIdCollision() {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setTenantName("Test Tenant");
        signupRequest.setUsername("testuser");
        signupRequest.setPassword("password");

        Shard shard = new Shard();
        shard.setShardKey("shard1");

        when(shardRepository.findAll()).thenReturn(Collections.singletonList(shard));
        when(tenantShardMappingRepository.existsByTenantId(anyString())).thenReturn(true).thenReturn(false);
        when(tenantShardMappingRepository.save(any(TenantShardMapping.class))).thenAnswer(i -> i.getArguments()[0]);
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(i -> i.getArguments()[0]);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        SignupResponse response = tenantService.signup(signupRequest);

        assertNotNull(response);
        assertTrue(response.getTenantId().startsWith("test-tenant-"));
        assertEquals("testuser", response.getUsername());
    }
}
