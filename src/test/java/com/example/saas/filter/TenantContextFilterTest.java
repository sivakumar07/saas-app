package com.example.saas.filter;

import com.example.saas.entity.global.TenantShardMapping;
import com.example.saas.repository.global.TenantShardMappingRepository;
import com.example.saas.security.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TenantContextFilterTest {

    @Mock
    private TenantShardMappingRepository tenantShardMappingRepository;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private TenantContextFilter tenantContextFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_publicPath() throws Exception {
        when(request.getRequestURI()).thenReturn("/signup");

        tenantContextFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_tenantPath_noJwt() throws Exception {
        when(request.getRequestURI()).thenReturn("/tenant1/some-resource");
        when(request.getHeader("Authorization")).thenReturn(null);
        TenantShardMapping mapping = new TenantShardMapping("tenant1", "shard1");
        when(tenantShardMappingRepository.findByTenantId("tenant1")).thenReturn(Optional.of(mapping));

        tenantContextFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_tenantPath_validJwt() throws Exception {
        when(request.getRequestURI()).thenReturn("/tenant1/some-resource");
        when(request.getHeader("Authorization")).thenReturn("Bearer test_token");
        Claims claims = Jwts.claims()
                .add("tenantId", "tenant1")
                .add("shard", "shard1")
                .add("userId", 1L)
                .build();
        when(jwtUtils.parseToken("test_token")).thenReturn(claims);

        tenantContextFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_tenantPath_invalidJwt() throws Exception {
        when(request.getRequestURI()).thenReturn("/tenant1/some-resource");
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid_token");
        when(jwtUtils.parseToken("invalid_token")).thenThrow(new RuntimeException("Invalid token"));
        TenantShardMapping mapping = new TenantShardMapping("tenant1", "shard1");
        when(tenantShardMappingRepository.findByTenantId("tenant1")).thenReturn(Optional.of(mapping));

        tenantContextFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_tenantMismatch() {
        when(request.getRequestURI()).thenReturn("/tenant2/some-resource");
        when(request.getHeader("Authorization")).thenReturn("Bearer test_token");
        Claims claims = Jwts.claims()
                .add("tenantId", "tenant1")
                .build();
        when(jwtUtils.parseToken("test_token")).thenReturn(claims);

        assertThrows(IllegalArgumentException.class, () -> tenantContextFilter.doFilterInternal(request, response, filterChain));
    }

    @Test
    void doFilterInternal_invalidTenant() {
        when(request.getRequestURI()).thenReturn("/invalid_tenant/some-resource");
        when(request.getHeader("Authorization")).thenReturn(null);
        when(tenantShardMappingRepository.findByTenantId("invalid_tenant")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> tenantContextFilter.doFilterInternal(request, response, filterChain));
    }
}
