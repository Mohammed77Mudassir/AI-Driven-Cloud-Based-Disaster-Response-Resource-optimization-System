package com.disaster.service.routing;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Simple thread-safe, TTL-based in-memory cache for route results. Keys are
 * derived from the normalized request (coordinates, mode, emergency flag,
 * closure list and provider). Entries older than the TTL are evicted lazily on
 * read, keeping recent routes cheap while never serving stale results.
 */
public class RouteCache {

    private final long ttlMillis;
    private final Map<String, CacheEntry> store = new ConcurrentHashMap<>();

    private static final class CacheEntry {
        private final Object value;
        private final long expiresAt;

        private CacheEntry(Object value, long expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }
    }

    public RouteCache(long ttlMillis) {
        this.ttlMillis = Math.max(1, ttlMillis);
    }

    public void put(String key, Object value) {
        store.put(key, new CacheEntry(value, System.currentTimeMillis() + ttlMillis));
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        CacheEntry entry = store.get(key);
        if (entry == null) return null;
        if (entry.expiresAt < System.currentTimeMillis()) {
            store.remove(key);
            return null;
        }
        return (T) entry.value;
    }

    public void clear() {
        store.clear();
    }

    public int size() {
        return store.size();
    }
}
