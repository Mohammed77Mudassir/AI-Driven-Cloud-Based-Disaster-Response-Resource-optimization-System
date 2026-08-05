package com.disaster.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory login-attempt tracking with automatic account lockout.
 *
 * After {@code maxAttempts} consecutive failed logins for a username the
 * account is locked for {@code lockoutMinutes}. Successful logins clear the
 * counter. State is intentionally in-memory (per instance) — a production
 * multi-node deployment should back this with Redis / a shared cache.
 */
@Service
public class LoginAttemptService {

    private static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);

    private final int maxAttempts;
    private final long lockoutMinutes;

    private final ConcurrentHashMap<String, Attempt> attempts = new ConcurrentHashMap<>();

    public LoginAttemptService(
            @Value("${app.security.login.max-attempts:5}") int maxAttempts,
            @Value("${app.security.login.lockout-minutes:15}") long lockoutMinutes) {
        this.maxAttempts = maxAttempts;
        this.lockoutMinutes = lockoutMinutes;
    }

    public boolean isLocked(String username) {
        if (username == null) return false;
        Attempt attempt = attempts.get(normalize(username));
        if (attempt == null) return false;
        if (attempt.lockedUntil != null && attempt.lockedUntil.isAfter(Instant.now())) {
            return true;
        }
        if (attempt.lockedUntil != null && !attempt.lockedUntil.isAfter(Instant.now())) {
            attempts.remove(normalize(username));
        }
        return false;
    }

    public void loginFailed(String username) {
        if (username == null) return;
        String key = normalize(username);
        Attempt attempt = attempts.compute(key, (k, existing) -> {
            Attempt current = existing != null ? existing : new Attempt();
            if (current.lockedUntil != null && !current.lockedUntil.isAfter(Instant.now())) {
                current = new Attempt();
            }
            current.failures++;
            if (current.failures >= maxAttempts) {
                current.lockedUntil = Instant.now().plusSeconds(lockoutMinutes * 60);
                current.failures = 0;
            }
            return current;
        });
        if (attempt.lockedUntil != null) {
            log.warn("Account locked for {} after repeated failed logins", username);
        }
    }

    public void loginSucceeded(String username) {
        if (username != null) {
            attempts.remove(normalize(username));
        }
    }

    public void clear(String username) {
        if (username != null) {
            attempts.remove(normalize(username));
        }
    }

    private String normalize(String username) {
        return username.trim().toLowerCase();
    }

    private static final class Attempt {
        int failures;
        Instant lockedUntil;
    }
}
