package com.disaster.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for email delivery.
 *
 * <p>Binds the {@code email.*} properties, populated from the
 * {@code RESEND_API_KEY}, {@code RESEND_FROM_EMAIL} and
 * {@code RESEND_FROM_NAME} environment variables (see application.properties).
 * When the API key is blank the application transparently falls back to the
 * built-in {@link com.disaster.service.MockEmailProvider}.</p>
 *
 * <pre>
 * email.resend.api-key        = ${RESEND_API_KEY:}
 * email.resend.from-email     = ${RESEND_FROM_EMAIL:}
 * email.resend.from-name      = ${RESEND_FROM_NAME:AI Disaster Management System}
 * email.resend.api-url        = ${RESEND_API_URL:https://api.resend.com/emails}
 * email.resend.timeout-ms     = ${RESEND_TIMEOUT_MS:8000}
 * email.resend.retry-count    = ${RESEND_RETRY_COUNT:2}
 * email.resend.retry-backoff-ms = ${RESEND_RETRY_BACKOFF_MS:500}
 * email.emergency-contact     = ${EMAIL_EMERGENCY_CONTACT:112}
 * </pre>
 */
@ConfigurationProperties(prefix = "email")
public class EmailProperties {

    private String emergencyContact = "112 (National Emergency Helpline)";

    private Resend resend = new Resend();

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public Resend getResend() {
        return resend;
    }

    public void setResend(Resend resend) {
        this.resend = resend;
    }

    /** Resend transactional email provider settings. */
    public static class Resend {

        private String apiKey = "";
        private String fromEmail = "";
        private String fromName = "AI Disaster Management System";
        private String apiUrl = "https://api.resend.com/emails";
        private long timeoutMs = 8000;
        private int retryCount = 2;
        private long retryBackoffMs = 500;

        public boolean isConfigured() {
            return apiKey != null && !apiKey.isBlank();
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getFromEmail() {
            return fromEmail;
        }

        public void setFromEmail(String fromEmail) {
            this.fromEmail = fromEmail;
        }

        public String getFromName() {
            return fromName;
        }

        public void setFromName(String fromName) {
            this.fromName = fromName;
        }

        public String getApiUrl() {
            return apiUrl;
        }

        public void setApiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
        }

        public long getTimeoutMs() {
            return timeoutMs;
        }

        public void setTimeoutMs(long timeoutMs) {
            this.timeoutMs = timeoutMs;
        }

        public int getRetryCount() {
            return retryCount;
        }

        public void setRetryCount(int retryCount) {
            this.retryCount = retryCount;
        }

        public long getRetryBackoffMs() {
            return retryBackoffMs;
        }

        public void setRetryBackoffMs(long retryBackoffMs) {
            this.retryBackoffMs = retryBackoffMs;
        }
    }
}
