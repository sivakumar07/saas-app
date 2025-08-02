package com.example.saas.config;

import com.example.saas.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataSourceConfigTest {

    private DataSourceConfig dataSourceConfig;

    @Mock
    private DataSource centralDataSource;

    @BeforeEach
    void setUp() {
        dataSourceConfig = new DataSourceConfig();
        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testRoutingDataSourceLogic() throws Exception {
        // Given
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(centralDataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false); // Simulate one shard
        when(resultSet.getString("shard_key")).thenReturn("shard1");
        when(resultSet.getString("jdbcurl")).thenReturn("jdbc:h2:mem:shard1"); // Use H2 for in-memory test
        when(resultSet.getString("username")).thenReturn("sa");
        when(resultSet.getString("password")).thenReturn("");

        // When
        DataSource routingDataSource = dataSourceConfig.routingDataSource(centralDataSource);
        TenantContext.setContext(null, "shard1", null);

        // Then
        assertNotNull(routingDataSource);
        Method method = AbstractRoutingDataSource.class.getDeclaredMethod("determineCurrentLookupKey");
        method.setAccessible(true);
        Object lookupKey = method.invoke(routingDataSource);
        assertEquals("shard1", lookupKey);
    }

    @Test
    void testCentralEntityManagerFactoryConfiguration() {
        // When
        LocalContainerEntityManagerFactoryBean factoryBean = dataSourceConfig.centralEntityManagerFactory(centralDataSource);

        // Then
        assertEquals("central", factoryBean.getPersistenceUnitName());
        assertNotNull(factoryBean.getDataSource());
    }

    @Test
    void testTenantEntityManagerFactoryConfiguration() {
        // Given
        DataSource routingDataSource = mock(DataSource.class);

        // When
        LocalContainerEntityManagerFactoryBean factoryBean = dataSourceConfig.tenantEntityManagerFactory(routingDataSource);

        // Then
        assertEquals("tenant", factoryBean.getPersistenceUnitName());
        assertNotNull(factoryBean.getDataSource());
    }
}
