package com.disaster.service;

import com.disaster.config.EmailProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the Resend email provider. No network access: the
 * {@link RestTemplate} is mocked so every test exercises configuration
 * selection, payload building (multi-recipient, HTML/plain-text), retry logic
 * and failure handling in isolation.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
class ResendEmailProviderTest {

    private EmailProperties properties;
    private RestTemplate restTemplate;
    private MockEmailProvider mockFallback;
    private EmailTemplateService templateService;
    private ResendEmailProvider provider;

    @BeforeEach
    void setUp() {
        properties = new EmailProperties();
        properties.getResend().setRetryCount(0);
        properties.getResend().setRetryBackoffMs(1);
        restTemplate = mock(RestTemplate.class);
        mockFallback = mock(MockEmailProvider.class);
        templateService = new EmailTemplateService(properties);
        provider = new ResendEmailProvider(properties, restTemplate, mockFallback, templateService);
    }

    private void configureResend(String apiKey, String fromEmail) {
        properties.getResend().setApiKey(apiKey);
        properties.getResend().setFromEmail(fromEmail);
    }

    private Map<String, Object> capturePayload() {
        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(eq(properties.getResend().getApiUrl()), captor.capture(), eq(Map.class));
        return (Map<String, Object>) captor.getValue().getBody();
    }

    // ------------------------------------------------------------------

    @Test
    void delegatesToMockWhenApiKeyIsMissing() {
        when(mockFallback.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);

        boolean sent = provider.sendEmail("user@example.com", "Test", "Hello");

        assertTrue(sent);
        verify(mockFallback).sendEmail("user@example.com", "Test", "Hello");
        verifyNoInteractions(restTemplate);
    }

    @Test
    void sendsEmailSuccessfullyThroughResend() {
        configureResend("re_test_123", "noreply@example.com");
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(Map.of("id", "email_123"), HttpStatus.OK));

        boolean sent = provider.sendEmail("user@example.com", "Test subject", "Hello body");

        assertTrue(sent);
        Map<String, Object> payload = capturePayload();
        assertEquals("AI Disaster Management System <noreply@example.com>", payload.get("from"));
        assertEquals(List.of("user@example.com"), payload.get("to"));
        assertEquals("Test subject", payload.get("subject"));
        assertTrue(((String) payload.get("html")).contains("AI Disaster Management System"));
        assertTrue(((String) payload.get("html")).contains("Hello body"));
        assertEquals("Hello body", payload.get("text"));
    }

    @Test
    void sendsToMultipleRecipientsInOneMessage() {
        configureResend("re_test_123", "noreply@example.com");
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(Map.of("id", "email_123"), HttpStatus.OK));

        boolean sent = provider.sendEmail("a@x.com,b@y.com ; a@x.com", "Test", "Hello");

        assertTrue(sent);
        Map<String, Object> payload = capturePayload();
        assertEquals(List.of("a@x.com", "b@y.com"), payload.get("to"));
    }

    @Test
    void plainTextBodyIsWrappedIntoBrandedHtml() {
        configureResend("re_test_123", "noreply@example.com");
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(Map.of("id", "email_123"), HttpStatus.OK));

        provider.sendEmail("user@example.com", "Subject", "Plain body");

        Map<String, Object> payload = capturePayload();
        String html = (String) payload.get("html");
        assertTrue(html.startsWith("<!DOCTYPE html>"));
        assertTrue(html.contains("Plain body"));
        assertTrue(html.contains("AI Disaster Management System"));
        assertEquals("Plain body", payload.get("text"));
    }

    @Test
    void existingHtmlBodyIsNotDoubleWrapped() {
        configureResend("re_test_123", "noreply@example.com");
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(Map.of("id", "email_123"), HttpStatus.OK));

        String html = "<html><body><h1>Already HTML</h1></body></html>";
        provider.sendEmail("user@example.com", "Subject", html);

        Map<String, Object> payload = capturePayload();
        assertEquals(html, payload.get("html"));
        assertTrue(((String) payload.get("text")).contains("Already HTML"));
    }

    @Test
    void carriesAuthorizationHeaderWithApiKey() {
        configureResend("re_test_123", "noreply@example.com");
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(Map.of("id", "email_123"), HttpStatus.OK));

        provider.sendEmail("user@example.com", "Subject", "Body");

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(anyString(), captor.capture(), eq(Map.class));
        HttpHeaders headers = captor.getValue().getHeaders();
        assertEquals("Bearer re_test_123", headers.getFirst(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void retriesTransientFailuresThenSucceeds() {
        configureResend("re_test_123", "noreply@example.com");
        properties.getResend().setRetryCount(2);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new ResourceAccessException("timeout"))
                .thenThrow(new ResourceAccessException("timeout"))
                .thenReturn(new ResponseEntity<>(Map.of("id", "email_123"), HttpStatus.OK));

        boolean sent = provider.sendEmail("user@example.com", "Subject", "Body");

        assertTrue(sent);
        verify(restTemplate, times(3)).postForEntity(anyString(), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    void fallsBackToMockWhenRetriesAreExhausted() {
        configureResend("re_test_123", "noreply@example.com");
        properties.getResend().setRetryCount(1);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new ResourceAccessException("api down"));
        when(mockFallback.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);

        boolean sent = assertDoesNotThrow(() -> provider.sendEmail("user@example.com", "Subject", "Body"));

        assertTrue(sent, "mock fallback must report success when Resend is unavailable");
        verify(restTemplate, times(2)).postForEntity(anyString(), any(HttpEntity.class), eq(Map.class));
        verify(mockFallback).sendEmail("user@example.com", "Subject", "Body");
    }

    @Test
    void fallsBackToMockWhenResendRejectsRequest() {
        configureResend("re_test_123", "noreply@example.com");
        properties.getResend().setRetryCount(2);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        when(mockFallback.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);

        boolean sent = provider.sendEmail("user@example.com", "Subject", "Body");

        assertTrue(sent);
        verify(restTemplate, times(1)).postForEntity(anyString(), any(HttpEntity.class), eq(Map.class));
        verify(mockFallback).sendEmail("user@example.com", "Subject", "Body");
    }

    @Test
    void invalidRecipientReturnsFalseWithoutHttpCall() {
        configureResend("re_test_123", "noreply@example.com");

        assertFalse(provider.sendEmail("not-an-email", "Subject", "Body"));
        verifyNoInteractions(restTemplate);
        verifyNoInteractions(mockFallback);
    }

    @Test
    void blankRecipientListReturnsFalseWithoutHttpCall() {
        configureResend("re_test_123", "noreply@example.com");

        assertFalse(provider.sendEmail("", "Subject", "Body"));
        verifyNoInteractions(restTemplate);
    }

    @Test
    void missingFromEmailReturnsFalseWithoutHttpCall() {
        configureResend("re_test_123", "");

        assertFalse(provider.sendEmail("user@example.com", "Subject", "Body"));
        verifyNoInteractions(restTemplate);
    }
}
