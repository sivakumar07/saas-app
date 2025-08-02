package com.example.saas.filter;

import com.example.saas.context.TenantContext;
import com.example.saas.entity.global.TenantShardMapping;
import com.example.saas.repository.global.TenantShardMappingRepository;
import com.example.saas.security.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class TenantContextFilter extends OncePerRequestFilter {

    private final TenantShardMappingRepository tenantShardMappingRepository;
    private final JwtUtils jwtUtils;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final List<String> publicPaths = List.of(
            "/signup",
            "/token",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/rqueue/**",
            "/load-test/**"
    );

    public TenantContextFilter(TenantShardMappingRepository tenantShardMappingRepository, JwtUtils jwtUtils) {
        this.tenantShardMappingRepository = tenantShardMappingRepository;
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Check if the request is for a public path
        if (publicPaths.stream().anyMatch(p -> pathMatcher.match(p, request.getRequestURI()))) {
            filterChain.doFilter(request, response);
            return;
        }

        // If not a public path, it's a tenant-specific request.
        try {
            String path = request.getRequestURI();
            String[] segments = path.split("/");
            if (segments.length < 2) {
                throw new IllegalArgumentException("Invalid tenant path");
            }
            String tenantIdFromPath = segments[1];
            Long userId = null;
            String shard = null;

            // Resolve shard and user from JWT if present
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                try {
                    Claims claims = jwtUtils.parseToken(token);
                    String tokenTenantId = (String) claims.get("tenantId");
                    String tokenShard = (String) claims.get("shard");
                    Number uid = (Number) claims.get("userId");

                    if (!tenantIdFromPath.equals(tokenTenantId)) {
                        throw new IllegalArgumentException("Tenant in URL does not match tenant in token");
                    }

                    userId = uid != null ? uid.longValue() : null;
                    shard = tokenShard;

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userId, null, Collections.emptyList());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (Exception ex) {
                    // Invalid token, clear context
                    SecurityContextHolder.clearContext();
                }
            }

            // If shard is not resolved from token, look it up
            if (shard == null) {
                TenantShardMapping mapping = tenantShardMappingRepository.findByTenantId(tenantIdFromPath)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid tenant"));
                shard = mapping.getShard();
            }

            TenantContext.setContext(tenantIdFromPath, shard, userId);
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
