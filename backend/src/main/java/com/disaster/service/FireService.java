package com.disaster.service;

import com.disaster.dto.FireDTO;

import java.util.List;

/**
 * Live NASA FIRMS fire monitoring service.
 *
 * <p>Fetches NASA FIRMS active-fire (thermal anomaly) CSV feeds, keeps only
 * detections inside India's geographical boundaries, maps them into
 * {@link FireDTO}s, caches the result in memory and refreshes automatically on
 * a fixed schedule. When FIRMS is unreachable (or no API key is configured) the
 * last successful cache is returned (an empty list before the first successful
 * fetch) so callers never see a failure.</p>
 */
public interface FireService {

    /**
     * Returns the most recently fetched active fires inside India, newest
     * first. Never throws: on the first call (or after the cache expires) a
     * refresh is attempted internally; if the feed is unreachable the last
     * cached data is returned.
     */
    List<FireDTO> getFires();

    /**
     * Force a refresh of the NASA FIRMS feed. Logs and swallows any failure so
     * callers (including the scheduled job) can never crash the application.
     */
    void refresh();
}
