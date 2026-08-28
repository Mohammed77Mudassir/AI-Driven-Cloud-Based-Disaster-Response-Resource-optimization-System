package com.disaster.service.routing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Thin HTTP client for the OpenRouteService Directions API
 * ({@code https://api.openrouteservice.org/v2/directions}). Owns all transport
 * concerns: URL construction, the {@code Authorization} header, connect/read
 * timeouts, exponential-backoff retries and JSON (de)serialization.
 *
 * <p>Every transient failure (network error, timeout, HTTP error) is retried up
 * to the configured {@code retry-count}; when all attempts are exhausted the
 * call fails with a {@link RoutingUnavailableException} so the caller can fall
 * back to the offline Haversine engine. A missing API key makes the client
 * report {@link #isConfigured()} {@code false} and no request is ever sent.</p>
 *
 * <p>Configuration is bound from environment variables (never hardcoded):</p>
 * <pre>
 * app.routing.openrouteservice.api-key     = ${OPENROUTESERVICE_API_KEY:}
 * app.routing.openrouteservice.url         = ${OPENROUTESERVICE_API_URL:https://api.openrouteservice.org/v2/directions}
 * app.routing.openrouteservice.timeout-ms  = ${OPENROUTESERVICE_TIMEOUT_MS:8000}
 * app.routing.openrouteservice.retry-count = ${OPENROUTESERVICE_RETRY_COUNT:2}
 * </pre>
 */
@Service
public class OpenRouteServiceClient {

    private static final Logger log = LoggerFactory.getLogger(OpenRouteServiceClient.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String apiKey;
    private final String baseUrl;
    private final int timeoutMs;
    private final int retryCount;

    /** Base delay before the first retry; doubles after every failed attempt. */
    private long retryBackoffMs = 250;

    @Autowired
    public OpenRouteServiceClient(RestTemplateBuilder builder,
                                  @Value("${app.routing.openrouteservice.api-key:}") String apiKey,
                                  @Value("${app.routing.openrouteservice.url:https://api.openrouteservice.org/v2/directions}") String baseUrl,
                                  @Value("${app.routing.openrouteservice.timeout-ms:8000}") int timeoutMs,
                                  @Value("${app.routing.openrouteservice.retry-count:2}") int retryCount) {
        this(buildRestTemplate(builder, timeoutMs), apiKey, baseUrl, timeoutMs, retryCount);
    }

    /** Package-private for unit tests. */
    OpenRouteServiceClient(RestTemplate restTemplate, String apiKey, String baseUrl,
                           int timeoutMs, int retryCount) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey == null ? "" : apiKey;
        this.baseUrl = baseUrl == null || baseUrl.isBlank()
                ? "https://api.openrouteservice.org/v2/directions" : baseUrl;
        this.timeoutMs = Math.max(1, timeoutMs);
        this.retryCount = Math.max(0, retryCount);
    }

    private static RestTemplate buildRestTemplate(RestTemplateBuilder builder, int timeoutMs) {
        long timeout = Math.max(1, timeoutMs);
        return builder
                .setConnectTimeout(Duration.ofMillis(timeout))
                .setReadTimeout(Duration.ofMillis(timeout))
                .build();
    }

    /** Whether an API key is available. No request is sent when this is false. */
    public boolean isConfigured() {
        return !apiKey.isBlank();
    }

    /** Package-private for tests: speeds up backoff verification. */
    void setRetryBackoffMs(long retryBackoffMs) {
        this.retryBackoffMs = Math.max(0, retryBackoffMs);
    }

    /**
     * POST the given JSON body to the Directions endpoint for {@code profile}.
     * Retries transient failures up to {@code retryCount} times, then throws
     * {@link RoutingUnavailableException}.
     *
     * @param profile transport profile, e.g. "driving-car"
     * @param body    request body (coordinates, preference, instructions, ...)
     * @return parsed JSON response, never {@code null}
     */
    public Map<String, Object> requestDirections(String profile, Map<String, Object> body) {
        if (!isConfigured()) {
            throw new RoutingUnavailableException("OpenRouteService API key is not configured");
        }

        String url = directionsUrl(profile);
        HttpEntity<String> entity = new HttpEntity<>(toJson(body), headers());
        int attempts = retryCount + 1;
        RestClientException lastFailure = null;

        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                ResponseEntity<String> raw = restTemplate.postForEntity(url, entity, String.class);
                if (!raw.getStatusCode().is2xxSuccessful()) {
                    throw new RoutingUnavailableException(
                            "OpenRouteService returned HTTP " + raw.getStatusCode());
                }
                return parse(raw.getBody());
            } catch (RoutingUnavailableException ex) {
                if (attempt == attempts) {
                    throw ex;
                }
                log.warn("OpenRouteService attempt {}/{} failed: {}", attempt, attempts, ex.getMessage());
                backoff(attempt);
            } catch (RestClientException ex) {
                lastFailure = ex;
                if (attempt == attempts) {
                    log.error("OpenRouteService request failed after {} attempts: {}",
                            attempts, ex.getMessage());
                    throw new RoutingUnavailableException(
                            "OpenRouteService request failed after " + attempts + " attempts: "
                                    + ex.getMessage(), ex);
                }
                log.warn("OpenRouteService attempt {}/{} failed: {}", attempt, attempts, ex.getMessage());
                backoff(attempt);
            }
        }
        throw new RoutingUnavailableException("OpenRouteService request failed", lastFailure);
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
        headers.set("Authorization", apiKey);
        return headers;
    }

    /** ORS v2 expects the transport profile in the URL path. */
    private String directionsUrl(String profile) {
        String url = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return url + "/" + profile;
    }

    private String toJson(Map<String, Object> body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (Exception ex) {
            throw new RoutingUnavailableException("Failed to build route request body", ex);
        }
    }

    private Map<String, Object> parse(String json) {
        try {
            Map<String, Object> parsed = objectMapper.readValue(json, Map.class);
            return parsed == null ? new LinkedHashMap<>() : parsed;
        } catch (Exception ex) {
            throw new RoutingUnavailableException("Failed to parse OpenRouteService response", ex);
        }
    }

    private void backoff(int attempt) {
        long delay = Math.min(retryBackoffMs * (1L << (attempt - 1)), 2000);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RoutingUnavailableException("Retry interrupted", ie);
        }
    }
}
