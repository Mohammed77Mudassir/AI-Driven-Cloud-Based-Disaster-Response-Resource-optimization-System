package com.disaster.service.routing;

import com.disaster.dto.routing.RouteOption;

import java.util.List;

/**
 * Abstraction over external/offline road routing engines. Implementations:
 * <ul>
 *   <li>{@link OpenRouteServiceProvider} - primary provider (free, no key for
 *       light usage; recommended with an API key for production).</li>
 *   <li>{@link GoogleMapsDirectionsProvider} / {@link MapboxDirectionsProvider}
 *       - drop-in stubs showing how a commercial provider plugs in later.</li>
 *   <li>{@link HaversineRouteProvider} - always-available offline fallback.</li>
 * </ul>
 */
public interface RouteProvider {

    /** Provider identifier surfaced in the API response, e.g. "openrouteservice". */
    String getName();

    /**
     * Whether this provider is configured and can be called. The orchestrator
     * skips providers that are not configured and falls back to Haversine.
     */
    boolean isConfigured();

    /** Whether this provider returns live/typical traffic-aware ETAs. */
    boolean supportsTraffic();

    /** Whether this provider can produce multiple route alternatives. */
    boolean supportsAlternatives();

    /**
     * Calculate candidate routes for the given context.
     *
     * @throws RoutingUnavailableException when the provider cannot produce a
     *         result (missing key, network failure, invalid response). The
     *         orchestrator catches this and falls back to Haversine.
     */
    List<RouteOption> calculateRoutes(RouteContext context);
}
