package com.disaster.config;

import com.disaster.dto.LiveUpdateDTO;
import com.disaster.service.WebSocketSessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.*;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.CopyOnWriteArrayList;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);

    private final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebSocketSessionService sessionService;

    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    public WebSocketConfig(WebSocketSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new LiveWebSocketHandler(), "/ws/live")
                .setAllowedOrigins(allowedOrigins.split(","));
    }

    public class LiveWebSocketHandler extends TextWebSocketHandler {
        @Override
        public void afterConnectionEstablished(WebSocketSession session) {
            sessions.add(session);
            sessionService.addSession(session.getId());
            log.debug("WebSocket connected. Active sessions: {}", sessions.size());
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
            sessions.remove(session);
            sessionService.removeSession(session.getId());
            log.debug("WebSocket disconnected. Active sessions: {}", sessions.size());
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
            String payload = message.getPayload();
            if (payload != null) {
                sessionService.recordHeartbeat(session.getId());
            }
            if ("PING".equalsIgnoreCase(payload)) {
                // Application-level keepalive: reply so the client can measure liveness.
                session.sendMessage(new TextMessage("PONG"));
            }
        }
    }

    /**
     * Closes sessions that stopped sending any traffic (heartbeats or events)
     * for longer than the configured stale timeout.
     */
    public void closeStaleSessions(long staleTimeoutMs) {
        for (String sessionId : sessionService.getStaleSessions(staleTimeoutMs)) {
            sessions.stream()
                    .filter(s -> s.getId().equals(sessionId))
                    .forEach(s -> {
                        try {
                            s.close(CloseStatus.POLICY_VIOLATION.withReason("Heartbeat timeout"));
                        } catch (IOException e) {
                            log.debug("Failed to close stale session {}", sessionId);
                        } finally {
                            sessions.remove(s);
                            sessionService.removeSession(sessionId);
                        }
                    });
        }
    }

    public void broadcastUpdate(String type, Object data) {
        if (sessions.isEmpty()) return;
        try {
            LiveUpdateDTO dto = new LiveUpdateDTO();
            dto.setType(type);
            dto.setData(data);
            dto.setTimestamp(LocalDateTime.now().toString());
            String json = objectMapper.writeValueAsString(dto);
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage(json));
                    } catch (IOException e) {
                        sessions.remove(session);
                        sessionService.removeSession(session.getId());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Broadcast error: {}", e.getMessage(), e);
        }
    }
}
