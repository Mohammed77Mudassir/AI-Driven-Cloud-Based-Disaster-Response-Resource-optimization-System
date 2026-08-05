package com.disaster.security;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the JWT utility: token issuance, signature validation,
 * subject extraction and tamper/expiry rejection.
 */
class JwtUtilsTest {

    // Matches the demo secret from application.properties (64 bytes when decoded).
    private static final String SECRET =
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970337336763979244226452948404D635166546A576E5A7234753778214125442A47";

    private static final long EXPIRATION_MS = 60_000L;

    private JwtUtils jwtUtils(long expirationMs) {
        return new JwtUtils(SECRET, expirationMs);
    }

    @Test
    void generatesDistinctTokensForDifferentSubjects() {
        JwtUtils utils = jwtUtils(EXPIRATION_MS);
        String a = utils.generateToken("alice");
        String b = utils.generateToken("bob");
        assertNotEquals(a, b);
        assertTrue(a.split("\\.").length == 3);
    }

    @Test
    void validatesGenuinelyIssuedToken() {
        JwtUtils utils = jwtUtils(EXPIRATION_MS);
        String token = utils.generateToken("alice");
        assertTrue(utils.validateToken(token));
        assertEquals("alice", utils.getUsernameFromToken(token));
    }

    @Test
    void rejectsTamperedToken() {
        JwtUtils utils = jwtUtils(EXPIRATION_MS);
        String token = utils.generateToken("alice");
        String tampered = token.substring(0, token.length() - 4) + "AAAA";
        assertFalse(utils.validateToken(tampered));
    }

    @Test
    void rejectsGarbageInput() {
        JwtUtils utils = jwtUtils(EXPIRATION_MS);
        assertFalse(utils.validateToken("not-a-jwt"));
        assertFalse(utils.validateToken(""));
        assertFalse(utils.validateToken(null));
    }

    @Test
    void rejectsExpiredToken() {
        JwtUtils utils = jwtUtils(-1000L);
        String token = utils.generateToken("alice");
        assertFalse(utils.validateToken(token));
    }

    @Test
    void issuedAtIsBeforeExpiration() {
        JwtUtils utils = jwtUtils(EXPIRATION_MS);
        String token = utils.generateToken("alice");
        var claims = io.jsonwebtoken.Jwts.parser()
                .verifyWith(new javax.crypto.spec.SecretKeySpec(
                        java.util.Base64.getDecoder().decode(SECRET), "HmacSHA256"))
                .build()
                .parseSignedClaims(token)
                .getPayload();
        Date issuedAt = claims.getIssuedAt();
        Date expiration = claims.getExpiration();
        assertNotNull(issuedAt);
        assertNotNull(expiration);
        assertTrue(expiration.after(issuedAt));
        assertEquals("ai-disaster-management", claims.getIssuer());
    }

    @Test
    void rejectsSecretShorterThan32Bytes() {
        String shortSecret = java.util.Base64.getEncoder().encodeToString("short".getBytes());
        assertThrows(IllegalStateException.class, () -> new JwtUtils(shortSecret, EXPIRATION_MS));
    }
}
