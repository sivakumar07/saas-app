package com.example.saas.config;

import com.example.saas.filter.TenantContextFilter;
import com.example.saas.service.AuthService;
import com.example.saas.service.ContactService;
import com.example.saas.service.TaskService;
import com.example.saas.service.TenantService;
import com.github.sonus21.rqueue.core.RqueueMessageEnqueuer;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TenantContextFilter tenantContextFilter;

    @MockBean
    private TenantService tenantService;

    @MockBean
    private AuthService authService;

    @MockBean
    private ContactService contactService;

    @MockBean
    private TaskService taskService;

    @MockBean
    private RqueueMessageEnqueuer rqueueMessageEnqueuer;

    @Test
    void testPublicEndpoints() throws Exception {
        mockMvc.perform(get("/signup")).andExpect(status().isMethodNotAllowed());
        mockMvc.perform(get("/token")).andExpect(status().isMethodNotAllowed());
        mockMvc.perform(get("/swagger-ui.html")).andExpect(status().isOk());
    }

    @Test
    void testProtectedEndpoints() throws Exception {
        mockMvc.perform(get("/some-protected-resource"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void testProtectedEndpointsWithMockUser() throws Exception {
        mockMvc.perform(get("/some-protected-resource"))
                .andExpect(status().isNotFound()); // isNotFound because we don't have a controller for it in this test slice
    }

    @Test
    void testPasswordEncoder() {
        SecurityConfig securityConfig = new SecurityConfig(tenantContextFilter);
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        assertNotNull(passwordEncoder);
        assertTrue(passwordEncoder instanceof BCryptPasswordEncoder);
    }

    @Test
    void testOpenAPI() {
        SecurityConfig securityConfig = new SecurityConfig(tenantContextFilter);
        OpenAPI openAPI = securityConfig.openAPI();
        assertNotNull(openAPI);
        SecurityRequirement securityRequirement = openAPI.getSecurity().get(0);
        assertTrue(securityRequirement.containsKey("bearerAuth"));
        SecurityScheme securityScheme = openAPI.getComponents().getSecuritySchemes().get("bearerAuth");
        assertEquals(SecurityScheme.Type.HTTP, securityScheme.getType());
        assertEquals("bearer", securityScheme.getScheme());
        assertEquals("JWT", securityScheme.getBearerFormat());
    }
}