package com.disaster.service;

import com.disaster.dto.AirQualityDTO;

import java.util.List;

/**
 * Live OpenAQ air-quality monitoring service.
 *
 * <p>Fetches the latest OpenAQ measurements for stations inside India's
 * geographical boundaries, maps them into {@link AirQualityDTO}s, caches the
 * result in memory and refreshes automatically on a fixed schedule. When OpenAQ
 * is unreachable (or no API key is configured) the last successful cache is
 * returned (an empty list before the first successful fetch) so callers never
 * see a failure.</p>
 */
public interface AirQualityService {

    /**
     * Returns the most recently fetched air-quality readings for stations
     * inside India, worst AQI first. Never throws: on the first call (or after
     * the cache expires) a refresh is attempted internally; if the feed is
     * unreachable the last cached data is returned.
     */
    List<AirQualityDTO> getAirQuality();

    /**
     * Force a refresh of the OpenAQ feed. Logs and swallows any failure so
     * callers (including the scheduled job) can never crash the application.
     */
    void refresh();
}
