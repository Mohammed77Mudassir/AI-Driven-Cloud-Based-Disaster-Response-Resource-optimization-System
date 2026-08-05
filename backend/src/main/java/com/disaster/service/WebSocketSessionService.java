package com.disaster.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class WebSocketSessionService {
    private final CopyOnWriteArrayList<String> activeSessions = new CopyOnWriteArrayList<>();
    private final ConcurrentHashMap<String, Long> lastHeartbeatAt = new ConcurrentHashMap<>();

    public void addSession(String sessionId) {
        activeSessions.add(sessionId);
        lastHeartbeatAt.put(sessionId, System.currentTimeMillis());
    }

    public void removeSession(String sessionId) {
        activeSessions.remove(sessionId);
        lastHeartbeatAt.remove(sessionId);
    }

    public void recordHeartbeat(String sessionId) {
        if (activeSessions.contains(sessionId)) {
            lastHeartbeatAt.put(sessionId, System.currentTimeMillis());
        }
    }

    /** Session ids that have not sent any traffic within the given timeout (ms). */
    public List<String> getStaleSessions(long timeoutMs) {
        long now = System.currentTimeMillis();
        return activeSessions.stream()
                .filter(id -> now - lastHeartbeatAt.getOrDefault(id, 0L) > timeoutMs)
                .toList();
    }

    public int getActiveSessionCount() {
        return activeSessions.size();
    }

    public boolean hasActiveSessions() {
        return !activeSessions.isEmpty();
    }
}
