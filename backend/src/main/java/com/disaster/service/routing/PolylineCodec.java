package com.disaster.service.routing;

import java.util.ArrayList;
import java.util.List;

/**
 * Decoder for Google-style encoded polylines as returned by OpenRouteService
 * and most commercial routing providers. Default precision is 5 decimal places
 * (used by ORS); the constructor allows the Mapbox precision of 6.
 */
public final class PolylineCodec {

    private final double precision;

    public PolylineCodec() {
        this(1e-5);
    }

    public PolylineCodec(double precision) {
        this.precision = precision;
    }

    /**
     * Decode an encoded polyline into a list of [latitude, longitude] pairs.
     */
    public List<double[]> decode(String encoded) {
        List<double[]> points = new ArrayList<>();
        if (encoded == null || encoded.isEmpty()) return points;

        int index = 0;
        int len = encoded.length();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int result = 1;
            int shift = 0;
            int b;
            do {
                if (index >= len) break;
                b = encoded.charAt(index++) - 63;
                result += (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            lat += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

            result = 1;
            shift = 0;
            do {
                if (index >= len) break;
                b = encoded.charAt(index++) - 63;
                result += (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            lng += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

            points.add(new double[]{lat * precision, lng * precision});
        }
        return points;
    }
}
