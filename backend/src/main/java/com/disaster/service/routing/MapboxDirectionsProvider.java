package com.disaster.service.routing;

import com.disaster.dto.routing.RouteOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Drop-in stub for the Mapbox Directions API (traffic v1 / live traffic).
 * Activated by setting {@code app.routing.provider=mapbox} and
 * {@code app.routing.mapbox.access-token}. Returns Mapbox polyline6 encoding,
 * which the {@link PolylineCodec} already supports.
 */
@Service
public class MapboxDirectionsProvider implements RouteProvider {

    @Value("${app.routing.mapbox.access-token:}")
    private String accessToken;

    @Override
    public String getName() {
        return "mapbox";
    }

    @Override
    public boolean isConfigured() {
        return accessToken != null && !accessToken.isBlank();
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
                "Mapbox Directions integration is a plug-in stub: set app.routing.mapbox.access-token and "
                        + "implement MapboxDirectionsProvider.calculateRoutes() to use it");
    }
}
