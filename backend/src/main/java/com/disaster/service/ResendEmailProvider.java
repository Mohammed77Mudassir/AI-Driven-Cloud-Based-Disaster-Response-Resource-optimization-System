package com.disaster.service;

import com.disaster.config.EmailProperties;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Production {@link EmailProvider} backed by the Resend REST API
 * ({@code POST /emails}).
 *
 * <p>Configuration is read exclusively from {@link EmailProperties} (bound from
 * the {@code RESEND_API_KEY} / {@code RESEND_FROM_EMAIL} /
 * {@code RESEND_FROM_NAME} environment variables) - nothing is hardcoded. When
 * no API key is configured the provider transparently delegates to
 * {@link MockEmailProvider}, preserving the exact demo behaviour.</p>
 *
 * <p>The provider supports plain-text and HTML bodies (HTML is auto-detected),
 * multiple recipients, bounded retries with exponential backoff for transient
 * failures, and always returns instead of throwing so notification creation is
 * never interrupted.</p>
 */
@Service
@Primary
public class ResendEmailProvider implements EmailProvider {

    private static final Logger log = LoggerFactory.getLogger(ResendEmailProvider.class);

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final EmailProperties properties;
    private final RestTemplate restTemplate;
    private final MockEmailProvider mockFallback;
    private final EmailTemplateService templateService;

    public ResendEmailProvider(EmailProperties properties,
                               RestTemplate emailRestTemplate,
                               MockEmailProvider mockFallback,
                               EmailTemplateService templateService) {
        this.properties = properties;
        this.restTemplate = emailRestTemplate;
        this.mockFallback = mockFallback;
        this.templateService = templateService;
    }

    @PostConstruct
    void reportMode() {
        if (properties.getResend().isConfigured()) {
            log.info("Resend email provider ACTIVE (from: {}, retries: {}, timeout: {} ms).",
                    properties.getResend().getFromEmail(),
                    properties.getResend().getRetryCount(),
                    properties.getResend().getTimeoutMs());
        } else {
            log.info("RESEND_API_KEY not configured; falling back to the mock email provider.");
        }
    }

    @Override
    public boolean sendEmail(String to, String subject, String body) {
        if (!properties.getResend().isConfigured()) {
            return mockFallback.sendEmail(to, subject, body);
        }

        log.info("[Email] Using Resend provider");

        List<String> recipients = parseRecipients(to);
        if (recipients.isEmpty()) {
            log.warn("Resend email skipped: no valid recipient(s) in '{}' (subject: {}).", to, subject);
            return false;
        }

        String from = buildFrom();
        if (from == null) {
            log.warn("Resend email skipped: RESEND_FROM_EMAIL is not configured.");
            return false;
        }

        String fromEmail = properties.getResend().getFromEmail();
        boolean htmlBody = body != null && body.trim().startsWith("<");
        String html = htmlBody ? body : templateService.wrap(subject, body);
        String text = htmlBody ? templateService.stripHtml(body) : body;

        boolean sent = sendWithRetry(from, fromEmail, recipients, subject, html, text);
        if (!sent) {
            log.error("[Email] Resend unavailable, using mock email fallback.");
            return mockFallback.sendEmail(to, subject, body);
        }
        return true;
    }

    // ------------------------------------------------------------------
    // Resend HTTP call
    // ------------------------------------------------------------------

    private boolean sendWithRetry(String from, String fromEmail,
                                  List<String> recipients, String subject,
                                  String html, String text) {
        String apiUrl = properties.getResend().getApiUrl();
        int attempts = Math.max(1, properties.getResend().getRetryCount() + 1);
        Exception lastFailure = null;

        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                HttpEntity<Map<String, Object>> request = buildRequest(from, fromEmail, recipients, subject, html, text);
                ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    log.info("Resend email sent to {} (subject: {}).", String.join(", ", recipients), subject);
                    return true;
                }
                lastFailure = new IllegalStateException("Resend returned HTTP " + response.getStatusCode().value());
            } catch (HttpStatusCodeException ex) {
                lastFailure = ex;
                int status = ex.getStatusCode().value();
                if (status >= 400 && status < 500 && status != 429) {
                    log.error("Resend rejected the request (HTTP {}): {}. Not retrying.",
                            status, ex.getResponseBodyAsString());
                    return false;
                }
            } catch (RestClientException ex) {
                lastFailure = ex;
            } catch (RuntimeException ex) {
                lastFailure = ex;
            }

            if (attempt < attempts) {
                sleepBackoff(attempt);
            }
        }

        log.error("Resend email to {} failed after {} attempt(s): {}",
                String.join(", ", recipients), attempts,
                lastFailure == null ? "unknown error" : lastFailure.getMessage());
        return false;
    }

    private HttpEntity<Map<String, Object>> buildRequest(String from, String fromEmail,
                                                         List<String> recipients, String subject,
                                                         String html, String text) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("from", from);
        payload.put("to", recipients);
        payload.put("subject", subject);
        if (html != null && !html.isBlank()) {
            payload.put("html", html);
        }
        if (text != null && !text.isBlank()) {
            payload.put("text", text);
        }
        if (fromEmail != null && !fromEmail.isBlank()) {
            payload.put("reply_to", fromEmail);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(properties.getResend().getApiKey());
        return new HttpEntity<>(payload, headers);
    }

    private void sleepBackoff(int attempt) {
        long backoff = properties.getResend().getRetryBackoffMs() * (1L << attempt);
        try {
            Thread.sleep(Math.min(backoff, 5000));
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /** Accepts a single address or a comma/semicolon-separated list. */
    private List<String> parseRecipients(String to) {
        List<String> recipients = new ArrayList<>();
        if (to == null || to.isBlank()) {
            return recipients;
        }
        for (String raw : to.split("[,;]")) {
            String email = raw.trim();
            if (email.isBlank() || !EMAIL_PATTERN.matcher(email).matches()) {
                log.warn("Resend email skipped invalid recipient: '{}'.", email);
                continue;
            }
            if (!recipients.contains(email)) {
                recipients.add(email);
            }
        }
        return recipients;
    }

    private String buildFrom() {
        String fromEmail = properties.getResend().getFromEmail();
        if (fromEmail == null || fromEmail.isBlank()) {
            return null;
        }
        String fromName = properties.getResend().getFromName();
        if (fromName != null && !fromName.isBlank()) {
            return fromName + " <" + fromEmail + ">";
        }
        return fromEmail;
    }
}
