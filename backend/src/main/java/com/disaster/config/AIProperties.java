package com.disaster.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for AI provider credentials.
 *
 * <p>Binds the {@code ai.*} properties, populated from the
 * {@code GOOGLE_GEMINI_API_KEY} and {@code GROQ_API_KEY} environment
 * variables (see application.properties). New AI providers are added by
 * extending this class with another nested {@link Provider} - no existing
 * code needs to change.</p>
 *
 * <pre>
 * ai.gemini.api-key = ${GOOGLE_GEMINI_API_KEY:}
 * ai.groq.api-key  = ${GROQ_API_KEY:}
 * </pre>
 */
@ConfigurationProperties(prefix = "ai")
public class AIProperties {

    private Provider gemini = new Provider();
    private Provider groq = new Provider();

    public Provider getGemini() {
        return gemini;
    }

    public void setGemini(Provider gemini) {
        this.gemini = gemini;
    }

    public Provider getGroq() {
        return groq;
    }

    public void setGroq(Provider groq) {
        this.groq = groq;
    }

    /** Generic API credential holder so new providers require no new class. */
    public static class Provider {
        private String apiKey = "";
        private String apiUrl = "";

        public boolean isConfigured() {
            return apiKey != null && !apiKey.isBlank();
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getApiUrl() {
            return apiUrl;
        }

        public void setApiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
        }
    }
}
