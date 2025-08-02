package com.example.saas.service;

import com.example.saas.context.TenantContext;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class TenantService {

    private final TenantShardMappingRepository tenantShardMappingRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ShardRepository shardRepository;

    private static final AtomicInteger shardCounter = new AtomicInteger(0);

    public TenantService(TenantShardMappingRepository tenantShardMappingRepository,
                         TenantRepository tenantRepository,
                         UserRepository userRepository,
                         ShardRepository shardRepository,
                         PasswordEncoder passwordEncoder) {
        this.tenantShardMappingRepository = tenantShardMappingRepository;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.shardRepository = shardRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public SignupResponse signup(SignupRequest request) {
        var shards = shardRepository.findAll();
        if (shards.isEmpty()) {
            throw new IllegalStateException("No shards configured in the system");
        }
        int index = shardCounter.getAndIncrement();
        Shard shard = shards.get(index % shards.size());

        TenantShardMapping tenantShardMapping = createTenantShardMapping(request, shard);

        createTenantAndUser(request, tenantShardMapping, shard);

        return new SignupResponse(tenantShardMapping.getTenantId(), request.getUsername());
    }

    @Transactional("centralTransactionManager")
    protected TenantShardMapping createTenantShardMapping(SignupRequest request, Shard shard) {
        String baseSlug = request.getTenantName().trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
        String tenantIdCandidate;
        do {
            String suffix = UUID.randomUUID().toString().substring(0, 8);
            tenantIdCandidate = baseSlug + "-" + suffix;
        } while (tenantShardMappingRepository.existsByTenantId(tenantIdCandidate));

        TenantShardMapping tenantShardMapping = new TenantShardMapping(tenantIdCandidate, shard.getShardKey());
        return tenantShardMappingRepository.save(tenantShardMapping);
    }

    @Transactional("tenantTransactionManager")
    protected void createTenantAndUser(SignupRequest request, TenantShardMapping tenantShardMapping, Shard shard) {
        try {
            TenantContext.setContext(tenantShardMapping.getTenantId(), shard.getShardKey(), null);

            Tenant tenant = new Tenant();
            tenant.setId(tenantShardMapping.getTenantId());
            tenant.setName(request.getTenantName());
            tenantRepository.save(tenant);

            String encodedPassword = passwordEncoder.encode(request.getPassword());
            User user = new User(request.getUsername(), encodedPassword);
            userRepository.save(user);
        } finally {
            TenantContext.clear();
        }
    }
}