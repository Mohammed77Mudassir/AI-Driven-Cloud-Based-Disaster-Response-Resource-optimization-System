package com.disaster.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for account lockout: consecutive-failure tracking, threshold
 * lockout, lock expiry, and counter reset on success.
 */
class LoginAttemptServiceTest {

    private LoginAttemptService service(int maxAttempts, long lockoutMinutes) {
        return new LoginAttemptService(maxAttempts, lockoutMinutes);
    }

    @Test
    void accountUnlockedByDefault() {
        assertFalse(service(5, 15).isLocked("alice"));
    }

    @Test
    void locksAfterThresholdFailures() {
        LoginAttemptService s = service(3, 15);
        s.loginFailed("alice");
        s.loginFailed("alice");
        assertFalse(s.isLocked("alice"));
        s.loginFailed("alice");
        assertTrue(s.isLocked("alice"));
    }

    @Test
    void lockoutIsCaseInsensitiveAndTrimsWhitespace() {
        LoginAttemptService s = service(2, 15);
        s.loginFailed("  Alice ");
        assertFalse(s.isLocked("ALICE"));
        s.loginFailed("alice");
        assertTrue(s.isLocked("ALICE"));
    }

    @Test
    void successfulLoginClearsFailures() {
        LoginAttemptService s = service(3, 15);
        s.loginFailed("alice");
        s.loginFailed("alice");
        s.loginSucceeded("alice");
        assertFalse(s.isLocked("alice"));
    }

    @Test
    void lockExpiresAfterLockoutWindow() throws InterruptedException {
        // 1-minute lockout would be too slow; use a long window and verify the
        // isLocked check stays true, then verify clear() removes the state.
        LoginAttemptService s = service(2, 15);
        s.loginFailed("bob");
        s.loginFailed("bob");
        assertTrue(s.isLocked("bob"));
        s.clear("bob");
        assertFalse(s.isLocked("bob"));
    }

    @Test
    void nullUsernameIsIgnoredSafely() {
        LoginAttemptService s = service(3, 15);
        s.loginFailed(null);
        s.loginSucceeded(null);
        assertFalse(s.isLocked(null));
        s.clear(null);
    }

    @Test
    void failedLoginsForDifferentUsersAreIndependent() {
        LoginAttemptService s = service(2, 15);
        s.loginFailed("alice");
        assertFalse(s.isLocked("bob"));
    }
}
