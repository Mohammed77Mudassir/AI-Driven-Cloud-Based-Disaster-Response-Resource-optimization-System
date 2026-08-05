package com.disaster.geo;

/**
 * Offline geospatial math used by the Real-Time Monitoring module.
 * Provides great-circle distance, bearing, destination projection,
 * route length and ETA estimation without any external API.
 */
public final class GeoUtils {

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double DEFAULT_SPEED_KMPH = 40.0;

    private GeoUtils() {}

    public static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    /** Initial bearing in degrees (0 = North, clockwise). */
    public static double bearing(double lat1, double lon1, double lat2, double lon2) {
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double dLon = Math.toRadians(lon2 - lon1);
        double y = Math.sin(dLon) * Math.cos(phi2);
        double x = Math.cos(phi1) * Math.sin(phi2)
                - Math.sin(phi1) * Math.cos(phi2) * Math.cos(dLon);
        double brng = Math.toDegrees(Math.atan2(y, x));
        return (brng + 360.0) % 360.0;
    }

    /** Destination point given start, bearing (deg) and distance (km). */
    public static double[] destination(double lat1, double lon1, double bearingDeg, double distanceKm) {
        double angular = distanceKm / EARTH_RADIUS_KM;
        double brng = Math.toRadians(bearingDeg);
        double phi1 = Math.toRadians(lat1);
        double lam1 = Math.toRadians(lon1);
        double phi2 = Math.asin(Math.sin(phi1) * Math.cos(angular)
                + Math.cos(phi1) * Math.sin(angular) * Math.cos(brng));
        double lam2 = lam1 + Math.atan2(
                Math.sin(brng) * Math.sin(angular) * Math.cos(phi1),
                Math.cos(angular) - Math.sin(phi1) * Math.sin(phi2));
        return new double[]{Math.toDegrees(phi2), Math.toDegrees(lam2)};
    }

    /** Point at fraction t (0..1) of the straight line between two coordinates. */
    public static double[] interpolate(double lat1, double lon1, double lat2, double lon2, double t) {
        return new double[]{lat1 + (lat2 - lat1) * t, lon1 + (lon2 - lon1) * t};
    }

    /** Total length of a polyline of [lat, lon] points in km. */
    public static double routeDistanceKm(double[][] points) {
        double total = 0;
        for (int i = 1; i < points.length; i++) {
            total += distanceKm(points[i - 1][0], points[i - 1][1],
                    points[i][0], points[i][1]);
        }
        return total;
    }

    /** ETA in minutes for a given distance at an assumed speed (km/h). */
    public static double etaMinutes(double distanceKm, double speedKmph) {
        double speed = speedKmph > 0 ? speedKmph : DEFAULT_SPEED_KMPH;
        return (distanceKm / speed) * 60.0;
    }
}
