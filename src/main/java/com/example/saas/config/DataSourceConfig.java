package com.example.saas.config;

import com.example.saas.context.TenantContext;
import jakarta.persistence.EntityManagerFactory;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Configuration of the application's data sources.  A master data source
 * holds tenant and user metadata, while a routing data source distributes
 * tenant data across multiple shard data sources.  The {@link TenantContext}
 * determines which shard to use at runtime.
 */
@Configuration
@EnableTransactionManagement
public class DataSourceConfig {

    /**
     * Configuration properties for the central (master) database.  This
     * database stores global entities such as tenants and users.
     */
    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.central")
    public DataSource centralDataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * A routing data source that selects a shard based on the current
     * {@link TenantContext}.  When the context does not specify a shard, the
     * default data source (shard1) is used.  The high‑level approach mirrors
     * the dynamic data source pattern described in the Spring Boot multi‑tenancy
     * guide.
     */
    @Bean
    public DataSource routingDataSource(@Qualifier("centralDataSource") DataSource centralDataSource) {
        Map<Object, Object> targetDataSources = new HashMap<>();
        try (Connection conn = centralDataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT shard_key, jdbcurl, username, password FROM shards")) {

            while (rs.next()) {
                String shardKey = rs.getString("shard_key");
                String jdbcUrl = rs.getString("jdbcurl");
                String username = rs.getString("username");
                String password = rs.getString("password");

                DataSource ds = DataSourceBuilder.create()
                        .url(jdbcUrl)
                        .username(username)
                        .password(password)
                        .driverClassName("org.postgresql.Driver")
                        .build();
                targetDataSources.put(shardKey, ds);

                // Apply shard-specific migrations
                SpringLiquibase liquibase = new SpringLiquibase();
                liquibase.setDataSource(ds);
                liquibase.setChangeLog("classpath:db/changelog/shard/db.changelog-shard.yaml");
                liquibase.afterPropertiesSet();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load shards or run Liquibase migrations", e);
        }

        AbstractRoutingDataSource dataSource = new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                return TenantContext.getShard();
            }
        };
        DataSource defaultDs = targetDataSources.isEmpty() ? null : (DataSource) targetDataSources.values().iterator().next();
        dataSource.setDefaultTargetDataSource(defaultDs);
        dataSource.setTargetDataSources(targetDataSources);
        dataSource.afterPropertiesSet();
        return dataSource;
    }

    /**
     * Entity manager factory for the central database.  It scans the master
     * entity package and uses the central data source.
     */
    @Bean(name = "centralEntityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean centralEntityManagerFactory(
            @Qualifier("centralDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("com.example.saas.entity.global");
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        emf.setEntityManagerFactoryInterface(EntityManagerFactory.class);
        Properties jpaProps = new Properties();
        jpaProps.put("hibernate.hbm2ddl.auto", "validate");
        emf.setJpaProperties(jpaProps);
        emf.setPersistenceUnitName("central");
        return emf;
    }

    /**
     * Transaction manager for the central entity manager factory.
     */
    @Bean
    public PlatformTransactionManager centralTransactionManager(
            @Qualifier("centralEntityManagerFactory") LocalContainerEntityManagerFactoryBean centralEntityManagerFactory) {
        return new JpaTransactionManager(centralEntityManagerFactory.getObject());
    }

    /**
     * Entity manager factory for tenant‑scoped entities.  It uses the routing
     * data source so that the correct shard is chosen based on the
     * {@link TenantContext}.  The entity package contains tenant entities such
     * as contacts and background jobs.  Automatic schema update is enabled.
     */
    @Bean(name = "tenantEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean tenantEntityManagerFactory(
            @Qualifier("routingDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("com.example.saas.entity.shard", "com.example.saas.entity.common");
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        emf.setEntityManagerFactoryInterface(EntityManagerFactory.class);
        Properties jpaProps = new Properties();
        jpaProps.put("hibernate.hbm2ddl.auto", "validate");
        emf.setJpaProperties(jpaProps);
        emf.setPersistenceUnitName("tenant");
        return emf;
    }

    /**
     * Transaction manager for tenant‑scoped operations.  By wiring this
     * transaction manager into tenant repositories we ensure that reads and
     * writes go through the appropriate shard.
     */
    @Bean
    public PlatformTransactionManager tenantTransactionManager(
            @Qualifier("tenantEntityManagerFactory") LocalContainerEntityManagerFactoryBean tenantEntityManagerFactory) {
        return new JpaTransactionManager(tenantEntityManagerFactory.getObject());
    }
}
