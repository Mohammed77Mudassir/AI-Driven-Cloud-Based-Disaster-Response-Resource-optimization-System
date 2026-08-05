package com.disaster.ai.core;

/**
 * Great-circle distance utilities used by every geospatial recommendation engine.
 */
public final class GeoDistance {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private GeoDistance() {}

    public static double km(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    /** Distance decay in 0..1: closer -> higher proximity score. */
    public static double proximity(double distanceKm, double radiusKm) {
        if (radiusKm <= 0) return 0;
        return Math.max(0, 1 - distanceKm / radiusKm);
    }
}
