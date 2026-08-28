package com.disaster.service;

import com.disaster.dto.EarthquakeDTO;

import java.util.List;

/**
 * Live earthquake monitoring service.
 *
 * <p>Fetches the USGS "all day" GeoJSON feed, filters events to India's
 * geographical boundaries, maps them into {@link EarthquakeDTO}s, caches the
 * result and refreshes automatically on a fixed schedule. When the USGS API
 * is unavailable the last successful cache is returned so callers never see
 * a failure.</p>
 */
public interface EarthquakeService {

    /**
     * Returns the most recently fetched earthquakes inside India. Never throws:
     * on the first call (or after the cache expires) a refresh is attempted
     * internally; if the feed is unreachable the last cached data is returned.
     */
    List<EarthquakeDTO> getEarthquakes();

    /**
     * Force a refresh of the earthquake feed. Logs and swallows any failure so
     * callers (including the scheduled job) can never crash the application.
     */
    void refresh();
}
