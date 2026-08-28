package com.disaster;

import com.disaster.service.EmailProvider;
import com.disaster.service.ResendEmailProvider;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * End-to-end tests for the Resend email integration. With {@code RESEND_API_KEY}
 * configured the {@code EmailProvider} bean resolves to the
 * {@link ResendEmailProvider}, which posts a proper JSON payload to the Resend
 * endpoint. The dedicated email {@link RestTemplate} is mocked so the suite
 * never touches the network.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
@SpringBootTest(properties = {
        "email.resend.api-key=re_test_123",
        "email.resend.from-email=noreply@example.com"
})
class ResendEmailIntegrationTest {

    @Autowired
    private EmailProvider emailProvider;

    @MockBean(name = "emailRestTemplate")
    private RestTemplate emailRestTemplate;

    @Test
    void resendProviderIsSelectedWhenApiKeyPresent() {
        assertTrue(emailProvider instanceof ResendEmailProvider);
    }

    @Test
    void sendsProfessionalHtmlEmailThroughResend() {
        when(emailRestTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(Map.of("id", "email_123"), HttpStatus.OK));

        boolean sent = emailProvider.sendEmail("user@example.com", "Test subject", "Hello body");

        assertTrue(sent);
        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(emailRestTemplate).postForEntity(eq("https://api.resend.com/emails"), captor.capture(), eq(Map.class));

        HttpEntity<Map<String, Object>> request = captor.getValue();
        Map<String, Object> payload = request.getBody();
        assertEquals("AI Disaster Management System <noreply@example.com>", payload.get("from"));
        assertEquals(List.of("user@example.com"), payload.get("to"));
        assertEquals("Test subject", payload.get("subject"));
        assertTrue(((String) payload.get("html")).contains("AI Disaster Management System"));
        assertEquals("Hello body", payload.get("text"));

        HttpHeaders headers = request.getHeaders();
        assertEquals("Bearer re_test_123", headers.getFirst(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void deliversToMultipleRecipients() {
        when(emailRestTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(Map.of("id", "email_124"), HttpStatus.OK));

        boolean sent = emailProvider.sendEmail("a@x.com,b@y.com", "Test", "Hello");

        assertTrue(sent);
        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(emailRestTemplate).postForEntity(anyString(), captor.capture(), eq(Map.class));
        assertEquals(List.of("a@x.com", "b@y.com"), ((Map<String, Object>) captor.getValue().getBody()).get("to"));
    }

    @Test
    void fallsBackToMockWhenResendApiFailsWithoutThrowing() {
        when(emailRestTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new org.springframework.web.client.ResourceAccessException("resend down"));

        boolean sent = emailProvider.sendEmail("user@example.com", "Test", "Hello");

        assertTrue(sent, "mock fallback must still report delivery when Resend is unavailable");
    }
}
