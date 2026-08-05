package com.disaster.service.routing;

import com.disaster.dto.routing.RouteOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Drop-in stub for the Google Maps Directions API. Intended to show how a
 * commercial traffic-aware provider plugs into the same {@link RouteProvider}
 * contract. To activate: set {@code app.routing.provider=google} and
 * {@code app.routing.google.api-key}. Until then it is never called.
 *
 * <p>Google Directions supports live traffic, so this provider advertises
 * {@code supportsTraffic() = true} (the ETA already reflects traffic) and
 * multiple alternatives via {@code alternatives=true} in the request.</p>
 */
@Service
public class GoogleMapsDirectionsProvider implements RouteProvider {

    @Value("${app.routing.google.api-key:}")
    private String apiKey;

    @Override
    public String getName() {
        return "google-maps";
    }

    @Override
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    @Override
    public boolean supportsTraffic() {
        return true;
    }

    @Override
    public boolean supportsAlternatives() {
        return true;
    }

    @Override
    public List<RouteOption> calculateRoutes(RouteContext context) {
        throw new RoutingUnavailableException(
                "Google Maps Directions integration is a plug-in stub: set app.routing.google.api-key and "
                        + "implement GoogleMapsDirectionsProvider.calculateRoutes() to use it");
    }
}
