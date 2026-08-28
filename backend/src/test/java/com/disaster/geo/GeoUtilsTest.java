package com.disaster.geo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the offline geospatial math: great-circle distance, bearing,
 * destination projection, interpolation, polyline length and ETA estimation.
 */
class GeoUtilsTest {

    private static final double DELTA = 0.5;

    @Test
    void distanceBetweenIdenticalPointsIsZero() {
        assertEquals(0.0, GeoUtils.distanceKm(19.0760, 72.8777, 19.0760, 72.8777), 1e-9);
    }

    @Test
    void mumbaiToDelhiDistanceIsApproximately1120Km() {
        double d = GeoUtils.distanceKm(19.0760, 72.8777, 28.6139, 77.2090);
        assertEquals(1120.0, d, 30.0);
    }

    @Test
    void distanceIsSymmetric() {
        double ab = GeoUtils.distanceKm(10, 20, 30, 40);
        double ba = GeoUtils.distanceKm(30, 40, 10, 20);
        assertEquals(ab, ba, 1e-9);
    }

    @Test
    void distanceIsNotNegative() {
        assertTrue(GeoUtils.distanceKm(-33.86, 151.21, 52.52, 13.40) > 0);
    }

    @Test
    void bearingNorthIsZero() {
        assertEquals(0.0, GeoUtils.bearing(10.0, 0.0, 11.0, 0.0), DELTA);
    }

    @Test
    void bearingEastIsNinety() {
        assertEquals(90.0, GeoUtils.bearing(0.0, 10.0, 0.0, 11.0), DELTA);
    }

    @Test
    void bearingIsNormalizedToZeroToThreeSixty() {
        double bearing = GeoUtils.bearing(0.0, 10.0, 0.0, 9.0); // west
        assertTrue(bearing >= 0 && bearing < 360);
    }

    @Test
    void destinationReturnsApproximateStartForZeroDistance() {
        double[] p = GeoUtils.destination(19.0760, 72.8777, 90.0, 0.0);
        assertEquals(19.0760, p[0], 1e-6);
        assertEquals(72.8777, p[1], 1e-6);
    }

    @Test
    void destinationEastIncreasesLongitude() {
        double[] p = GeoUtils.destination(0.0, 0.0, 90.0, 111.0); // ~1 degree at equator
        assertEquals(0.0, p[0], DELTA);
        assertEquals(1.0, p[1], 0.2);
    }

    @Test
    void interpolateMidpointIsAverage() {
        double[] mid = GeoUtils.interpolate(10.0, 20.0, 20.0, 40.0, 0.5);
        assertEquals(15.0, mid[0], 1e-9);
        assertEquals(30.0, mid[1], 1e-9);
    }

    @Test
    void routeDistanceIsSumOfSegments() {
        double[][] route = {{0, 0}, {0, 1}, {0, 2}};
        double oneDegree = GeoUtils.distanceKm(0, 0, 0, 1);
        assertEquals(2 * oneDegree, GeoUtils.routeDistanceKm(route), 1e-6);
    }

    @Test
    void routeDistanceOfSinglePointIsZero() {
        double[][] route = {{1, 1}};
        assertEquals(0.0, GeoUtils.routeDistanceKm(route), 1e-9);
    }

    @Test
    void etaScalesWithSpeed() {
        double fast = GeoUtils.etaMinutes(100.0, 100.0); // 60 min
        double slow = GeoUtils.etaMinutes(100.0, 50.0);  // 120 min
        assertEquals(60.0, fast, 1e-9);
        assertEquals(120.0, slow, 1e-9);
    }

    @Test
    void etaFallsBackToDefaultSpeedForNonPositiveSpeed() {
        double eta = GeoUtils.etaMinutes(40.0, 0.0); // default 40 km/h
        assertEquals(60.0, eta, 1e-9);
    }

    @Test
    void validIndianCitiesAreInsideIndia() {
        assertTrue(GeoUtils.isInsideIndia(12.9716, 77.5946)); // Bengaluru
        assertTrue(GeoUtils.isInsideIndia(19.0760, 72.8777)); // Mumbai
        assertTrue(GeoUtils.isInsideIndia(28.6139, 77.2090)); // Delhi
        assertTrue(GeoUtils.isInsideIndia(13.0827, 80.2707)); // Chennai
    }

    @Test
    void outOfBoundsPointsInNeighboringCountriesAreNotInsideIndia() {
        assertFalse(GeoUtils.isInsideIndia(24.86, 67.01)); // Karachi, Pakistan (west of box)
        assertFalse(GeoUtils.isInsideIndia(16.69, 98.51)); // eastern Myanmar (east of box)
        assertFalse(GeoUtils.isInsideIndia(6.03, 80.21));  // Galle, Sri Lanka (south of box)
        assertFalse(GeoUtils.isInsideIndia(37.2, 82.0));   // Tibet, north of Nepal (north of box)
    }

    @Test
    void northernBoundaryIsInsideIndia() {
        assertTrue(GeoUtils.isInsideIndia(37.1, 80.0));
        assertFalse(GeoUtils.isInsideIndia(37.1001, 80.0));
    }

    @Test
    void southernBoundaryIsInsideIndia() {
        assertTrue(GeoUtils.isInsideIndia(6.7, 80.0));
        assertFalse(GeoUtils.isInsideIndia(6.6999, 80.0));
    }

    @Test
    void easternBoundaryIsInsideIndia() {
        assertTrue(GeoUtils.isInsideIndia(20.0, 97.4));
        assertFalse(GeoUtils.isInsideIndia(20.0, 97.4001));
    }

    @Test
    void westernBoundaryIsInsideIndia() {
        assertTrue(GeoUtils.isInsideIndia(20.0, 68.1));
        assertFalse(GeoUtils.isInsideIndia(20.0, 68.0999));
    }

    @Test
    void invalidLatitudeIsNotInsideIndia() {
        assertFalse(GeoUtils.isInsideIndia(91.0, 80.0));
        assertFalse(GeoUtils.isInsideIndia(-91.0, 80.0));
    }

    @Test
    void invalidLongitudeIsNotInsideIndia() {
        assertFalse(GeoUtils.isInsideIndia(20.0, 181.0));
        assertFalse(GeoUtils.isInsideIndia(20.0, -181.0));
    }

    @Test
    void originIsNotInsideIndia() {
        assertFalse(GeoUtils.isInsideIndia(0.0, 0.0));
    }
}
