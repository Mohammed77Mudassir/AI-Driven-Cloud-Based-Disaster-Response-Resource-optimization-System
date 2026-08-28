package com.disaster.service;

import com.disaster.dto.EonetDTO;

import java.util.List;

/**
 * Live NASA EONET natural-disaster monitoring service.
 *
 * <p>Fetches the NASA EONET v3 events feed, keeps only <em>active</em> events
 * inside India's geographical boundaries, maps them into {@link EonetDTO}s,
 * caches the result and refreshes automatically on a fixed schedule. When the
 * NASA API is unavailable the last successful cache is returned (or an empty
 * list before the first successful fetch) so callers never see a failure.</p>
 */
public interface EonetService {

    /**
     * Returns the most recently fetched active EONET events inside India.
     * Never throws: on the first call (or after the cache expires) a refresh is
     * attempted internally; if the feed is unreachable the last cached data is
     * returned.
     */
    List<EonetDTO> getEvents();

    /**
     * Force a refresh of the EONET feed. Logs and swallows any failure so
     * callers (including the scheduled job) can never crash the application.
     */
    void refresh();
}
