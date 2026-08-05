package com.disaster.service.routing;

/**
 * Thrown by a {@link RouteProvider} when it cannot produce a route (not
 * configured, network failure, malformed response, API rejection). The
 * {@code RouteOptimizationService} catches it and transparently falls back to
 * the Haversine engine so routing always keeps working.
 */
public class RoutingUnavailableException extends RuntimeException {

    public RoutingUnavailableException(String message) {
        super(message);
    }

    public RoutingUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
