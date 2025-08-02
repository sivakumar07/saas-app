package com.example.saas.config;

import com.example.saas.entity.global.Shard;
import com.example.saas.repository.global.ShardRepository;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * Applies Liquibase migrations to each shard database at application startup.
 * Liquibase does not natively support running against multiple dynamic
 * data sources, so this runner iterates over each shard defined in the
 * central database, creates a temporary DataSource and executes the shard
 * change log.  If no shards are defined the runner performs no action.
 */
@Component
public class ShardLiquibaseRunner implements CommandLineRunner {

    private final ShardRepository shardRepository;

    public ShardLiquibaseRunner(ShardRepository shardRepository) {
        this.shardRepository = shardRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        for (Shard shard : shardRepository.findAll()) {
            // Build a DataSource for this shard.  Use the PostgreSQL driver
            // explicitly because DataSourceBuilder cannot infer it from the URL.
            DataSource dataSource = DataSourceBuilder.create()
                    .url(shard.getJdbcUrl())
                    .username(shard.getUsername())
                    .password(shard.getPassword())
                    .driverClassName("org.postgresql.Driver")
                    .build();
            SpringLiquibase liquibase = new SpringLiquibase();
            liquibase.setDataSource(dataSource);
            liquibase.setChangeLog("classpath:db/changelog/shard/db.changelog-shard.yaml");
            // Disable liquibase bean definition name prefix to avoid collisions
            liquibase.setShouldRun(true);
            liquibase.afterPropertiesSet();
        }
    }
}