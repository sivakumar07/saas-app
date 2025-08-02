package com.example.saas.service;

import com.example.saas.context.TenantContext;
import com.example.saas.dto.LoginRequest;
import com.example.saas.dto.LoginResponse;
import com.example.saas.entity.global.TenantShardMapping;
import com.example.saas.entity.shard.User;
import com.example.saas.repository.global.TenantShardMappingRepository;
import com.example.saas.repository.shard.UserRepository;
import com.example.saas.security.JwtUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TenantShardMappingRepository tenantShardMappingRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthService(UserRepository userRepository,
                       TenantShardMappingRepository tenantShardMappingRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.tenantShardMappingRepository = tenantShardMappingRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    public LoginResponse login(LoginRequest request) {
        TenantShardMapping tenantShardMapping = getTenantShardMapping(request.getTenantId());

        try {
            TenantContext.setContext(tenantShardMapping.getTenantId(), tenantShardMapping.getShard(), null);
            return authenticateUserAndGenerateToken(request, tenantShardMapping);
        } finally {
            TenantContext.clear();
        }
    }

    @Transactional(transactionManager = "centralTransactionManager", readOnly = true)
    protected TenantShardMapping getTenantShardMapping(String tenantId) {
        return tenantShardMappingRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid tenantId"));
    }

    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    protected LoginResponse authenticateUserAndGenerateToken(LoginRequest request, TenantShardMapping tenantShardMapping) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("tenantId", request.getTenantId());
        claims.put("shard", tenantShardMapping.getShard());
        String token = jwtUtils.generateToken(claims, user.getUsername());
        return new LoginResponse(token);
    }
}