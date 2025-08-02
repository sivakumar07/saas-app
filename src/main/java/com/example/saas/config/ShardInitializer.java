package com.example.saas.config;

import com.example.saas.entity.global.Shard;
import com.example.saas.repository.global.ShardRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Initializes shard metadata in the central database based on the
 * {@code spring.datasource.shards} configuration properties.  When the
 * application starts it checks whether the {@link Shard} table contains
 * entries.  If not, it reads the shard definitions from the environment
 * (e.g., application.yml) and persists them.  This enables dynamic
 * routing of tenant data to the correct shard.
 */
@Component
public class ShardInitializer {

    private final ShardRepository shardRepository;
    private final ConfigurableEnvironment env;

    @Autowired
    public ShardInitializer(ShardRepository shardRepository, ConfigurableEnvironment env) {
        this.shardRepository = shardRepository;
        this.env = env;
    }

    @PostConstruct
    public void initializeShards() {
        if (!shardRepository.findAll().isEmpty()) {
            return; // shards already initialized
        }
        // Extract shard definitions from environment.  We expect properties
        // like spring.datasource.shards.shard1.url, .username, .password.
        Map<String, Object> props = env.getPropertySources().stream()
                .filter(ps -> ps instanceof org.springframework.core.env.MapPropertySource)
                .map(ps -> (org.springframework.core.env.MapPropertySource) ps)
                .flatMap(ps -> ps.getSource().entrySet().stream())
                .filter(e -> e.getKey().startsWith("spring.datasource.shards"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        // Determine shard names based on keys such as spring.datasource.shards.shard1.url
        props.keySet().stream()
                .map(key -> key.split("\\.")[3]) // extract "shard1" from the property key
                .distinct()
                .forEach(shardKey -> {
                    String prefix = "spring.datasource.shards." + shardKey;
                    String url = env.getProperty(prefix + ".jdbcUrl");
                    String username = env.getProperty(prefix + ".username");
                    String password = env.getProperty(prefix + ".password");
                    if (url != null && username != null && password != null) {
                        Shard shard = new Shard(shardKey, url, username, password);
                        shardRepository.save(shard);
                    }
                });
    }
}