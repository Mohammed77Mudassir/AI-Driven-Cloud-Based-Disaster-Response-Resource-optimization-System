package com.disaster;

import com.disaster.service.WebSocketSessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end WebSocket tests for the Real-Time Monitoring module.
 * Verifies the application-level keepalive (PING/PONG), that server
 * broadcasts (LOCATION_SNAPSHOT) reach connected clients, and that the
 * session service tracks lifecycle, heartbeats and stale connections.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MonitoringWebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private WebSocketSessionService sessionService;

    private String adminToken() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        ResponseEntity<String> response = restTemplate.exchange("/api/auth/login", HttpMethod.POST,
                new HttpEntity<>(Map.of("username", "admin", "password", "admin123"), headers), String.class);
        return objectMapper.readTree(response.getBody()).get("token").asText();
    }

    private String wsUri() {
        return "ws://localhost:" + port + "/ws/live";
    }

    private WebSocketSession connect(String uri, BlockingQueue<String> inbox) throws Exception {
        CompletableFuture<WebSocketSession> future = new CompletableFuture<>();
        StandardWebSocketClient client = new StandardWebSocketClient();
        client.execute(new TextWebSocketHandler() {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) {
                future.complete(session);
            }

            @Override
            protected void handleTextMessage(WebSocketSession session, TextMessage message) {
                inbox.offer(message.getPayload());
            }
        }, uri).get(5, TimeUnit.SECONDS);
        return future.get(5, TimeUnit.SECONDS);
    }

    @Test
    void pingReceivesPong() throws Exception {
        BlockingQueue<String> inbox = new LinkedBlockingQueue<>();
        WebSocketSession session = connect(wsUri(), inbox);

        session.sendMessage(new TextMessage("PING"));
        String reply = inbox.poll(5, TimeUnit.SECONDS);
        session.close();

        assertThat(reply).isEqualTo("PONG");
    }

    @Test
    void broadcastsReachConnectedClients() throws Exception {
        BlockingQueue<String> inbox = new LinkedBlockingQueue<>();
        WebSocketSession session = connect(wsUri(), inbox);

        String token = adminToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        // Triggers LOCATION_SNAPSHOT + DRONE_LOCATION broadcasts on the server.
        restTemplate.exchange("/api/locations/live", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        String snapshot = null;
        long deadline = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < deadline && snapshot == null) {
            String message = inbox.poll(2, TimeUnit.SECONDS);
            if (message == null) break;
            if (message.contains("\"type\":\"LOCATION_SNAPSHOT\"")) snapshot = message;
        }
        session.close();

        assertThat(snapshot).isNotNull();
    }

    @Test
    void sessionServiceTracksLifecycleAndStaleSessions() throws Exception {
        int before = sessionService.getActiveSessionCount();

        sessionService.addSession("test-session-1");
        sessionService.addSession("test-session-2");
        assertThat(sessionService.getActiveSessionCount()).isEqualTo(before + 2);

        // Fresh sessions are not stale with a generous timeout.
        assertThat(sessionService.getStaleSessions(90_000)).doesNotContain("test-session-1");

        // Heartbeat resets the clock; with a 1ms timeout and a small wait both are stale.
        sessionService.recordHeartbeat("test-session-1");
        Thread.sleep(15);
        assertThat(sessionService.getStaleSessions(1)).contains("test-session-1", "test-session-2");

        sessionService.removeSession("test-session-1");
        sessionService.removeSession("test-session-2");
        assertThat(sessionService.getActiveSessionCount()).isEqualTo(before);
    }
}
