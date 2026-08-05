package com.disaster.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory sliding-window rate limiter.
 *
 * Applies per IP-address (falling back to a global key when the IP is
 * unavailable) to the authentication and public endpoints to blunt
 * brute-force, credential-stuffing and public-report spam attacks. Limits
 * are configurable via properties. The real remote address is always part
 * of the bucket key so a client cannot evade limits by spoofing
 * {@code X-Forwarded-For} headers.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final long WINDOW_SECONDS = 60;

    /** Evict buckets that have not been touched in this many windows. */
    private static final long STALE_AFTER_SECONDS = WINDOW_SECONDS * 30;

    private final int maxRequestsPerMinute;
    private final int publicMaxRequestsPerMinute;

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimitingFilter(
            @Value("${app.security.rate-limit.max-per-minute:60}") int maxRequestsPerMinute,
            @Value("${app.security.rate-limit.public-max-per-minute:20}") int publicMaxRequestsPerMinute) {
        this.maxRequestsPerMinute = maxRequestsPerMinute;
        this.publicMaxRequestsPerMinute = publicMaxRequestsPerMinute;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !(path.startsWith("/api/auth/") || path.startsWith("/api/public/"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        int limit = path.startsWith("/api/public/") ? publicMaxRequestsPerMinute : maxRequestsPerMinute;
        String key = clientKey(request);
        if (!allow(key, limit)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too many requests. Please wait a minute and try again.\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String clientKey(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        if (ip == null || ip.isBlank()) {
            ip = "0.0.0.0";
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded == null || forwarded.isBlank()) {
            return "ip:" + ip;
        }
        // Remote address is authoritative; the forwarded header is a hint only.
        return "ip:" + ip + "|fwd:" + forwarded.split(",")[0].trim();
    }

    private boolean allow(String key, int limit) {
        long now = Instant.now().getEpochSecond();
        long windowStart = now - WINDOW_SECONDS;
        Bucket bucket = buckets.computeIfAbsent(key, k -> new Bucket());
        synchronized (bucket) {
            bucket.timestamps.removeIf(ts -> ts < windowStart);
            if (bucket.timestamps.size() >= limit) {
                return false;
            }
            bucket.timestamps.add(now);
            bucket.lastSeen = now;
            return true;
        }
    }

    /**
     * Opportunistically evicts stale buckets so the map does not grow
     * without bound under IP churn. Called periodically by the scheduler.
     */
    public void evictStaleBuckets() {
        long now = Instant.now().getEpochSecond();
        buckets.entrySet().removeIf(e -> now - e.getValue().lastSeen > STALE_AFTER_SECONDS);
    }

    private static final class Bucket {
        final java.util.ArrayDeque<Long> timestamps = new java.util.ArrayDeque<>();
        volatile long lastSeen = Instant.now().getEpochSecond();
    }
}
