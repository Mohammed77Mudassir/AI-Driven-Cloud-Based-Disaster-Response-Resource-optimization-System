package com.disaster.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the in-memory sliding-window rate limiter: scope of
 * application, per-IP bucketing and HTTP 429 enforcement.
 */
class RateLimitingFilterTest {

    private RateLimitingFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RateLimitingFilter(2, 1);
    }

    private MockHttpServletRequest request(String uri) {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", uri);
        req.setRequestURI(uri);
        req.setRemoteAddr("127.0.0.1");
        return req;
    }

    @Test
    void appliesOnlyToAuthAndPublicPaths() {
        assertFalse(filter.shouldNotFilter(request("/api/auth/login")));
        assertFalse(filter.shouldNotFilter(request("/api/public/disasters")));
        assertTrue(filter.shouldNotFilter(request("/api/disasters")));
        assertTrue(filter.shouldNotFilter(request("/api/ai/analyze")));
    }

    @Test
    void rejectsRequestsOverTheLimit() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        // maxRequestsPerMinute = 2
        filter.doFilterInternal(request("/api/auth/login"), response, new MockFilterChain());
        assertEquals(200, response.getStatus());
        filter.doFilterInternal(request("/api/auth/login"), response, new MockFilterChain());
        assertEquals(200, response.getStatus());
        filter.doFilterInternal(request("/api/auth/login"), response, new MockFilterChain());
        assertEquals(429, response.getStatus());
    }

    @Test
    void publicEndpointsUseTheirOwnStricterLimit() throws Exception {
        // publicMaxRequestsPerMinute = 1
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilterInternal(request("/api/public/disasters"), response, new MockFilterChain());
        assertEquals(200, response.getStatus());
        filter.doFilterInternal(request("/api/public/disasters"), response, new MockFilterChain());
        assertEquals(429, response.getStatus());
    }

    @Test
    void differentClientsHaveIndependentBuckets() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpServletRequest clientA = request("/api/auth/login");
        clientA.setRemoteAddr("10.0.0.1");
        MockHttpServletRequest clientB = request("/api/auth/login");
        clientB.setRemoteAddr("10.0.0.2");

        filter.doFilterInternal(clientA, response, new MockFilterChain());
        filter.doFilterInternal(clientA, response, new MockFilterChain());
        filter.doFilterInternal(clientA, response, new MockFilterChain());
        assertEquals(429, response.getStatus());

        MockHttpServletResponse responseB = new MockHttpServletResponse();
        filter.doFilterInternal(clientB, responseB, new MockFilterChain());
        assertEquals(200, responseB.getStatus());
    }

    @Test
    void missingRemoteAddressFallsBackToGlobalKey() throws Exception {
        MockHttpServletRequest req = request("/api/auth/login");
        req.setRemoteAddr(null);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilterInternal(req, response, new MockFilterChain());
        assertEquals(200, response.getStatus());
    }
}
