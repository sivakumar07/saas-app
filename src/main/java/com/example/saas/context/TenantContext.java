package com.example.saas.context;

/**
 * Maintains tenant and user information per request using {@link ThreadLocal}.
 *
 * <p>Spring Boot does not provide built‑in support for sharding, so this class
 * stores the current tenant identifier, the shard that tenant belongs to, and
 * the authenticated user identifier for the duration of a request or a
 * background job.  The {@link com.example.saas.filter.TenantContextFilter}
 * populates this context for web requests, while job dispatchers set the
 * context before processing a background job.  The DataSource router
 * consults the current shard to determine which database to use for a given
 * operation.</p>
 */
public final class TenantContext {

    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();
    private static final ThreadLocal<String> currentShard = new ThreadLocal<>();
    private static final ThreadLocal<Long> currentUser = new ThreadLocal<>();

    private TenantContext() {
        // prevent instantiation
    }

    /**
     * Sets the current tenant identifier and associated shard.  The user
     * identifier is optional and may be {@code null} if the user is not
     * authenticated (e.g. during signup or login).
     *
     * @param tenantId the tenant identifier (non‑null)
     * @param shardId  the shard key for the tenant (non‑null)
     * @param userId   the authenticated user identifier, may be {@code null}
     */
    public static void setContext(String tenantId, String shardId, Long userId) {
        currentTenant.set(tenantId);
        currentShard.set(shardId);
        currentUser.set(userId);
    }

    /**
     * @return the tenant identifier for the current thread or {@code null} if
     *         none is set
     */
    public static String getTenantId() {
        return currentTenant.get();
    }

    /**
     * @return the shard key associated with the current tenant or {@code null}
     */
    public static String getShard() {
        return currentShard.get();
    }

    /**
     * @return the authenticated user identifier or {@code null} if not set
     */
    public static Long getUserId() {
        return currentUser.get();
    }

    /**
     * Clears all thread‑local values.  This should be called at the end of
     * each request or background job to avoid leaking context across
     * unrelated requests.
     */
    public static void clear() {
        currentTenant.remove();
        currentShard.remove();
        currentUser.remove();
    }
}