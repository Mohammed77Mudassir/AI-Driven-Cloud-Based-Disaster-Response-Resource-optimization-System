package com.disaster.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates external-API configuration at application startup.
 *
 * <p>Missing <em>optional</em> keys only produce a clear WARNING - the
 * corresponding service is simply disabled and the built-in mock / offline
 * provider is used, so the application always starts. Missing
 * <em>mandatory</em> configuration (the JWT signing secret) is reported as
 * an ERROR; the {@link com.disaster.security.JwtUtils} bean additionally
 * refuses to start the context when the secret is absent or too short.</p>
 */
@Component
public class ExternalApiKeyValidator implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ExternalApiKeyValidator.class);

    private final WeatherProperties weather;
    private final AIProperties ai;
    private final NotificationProperties notification;
    private final ApiProperties api;
    private final EmailProperties email;
    private final Environment environment;

    public ExternalApiKeyValidator(WeatherProperties weather,
                                   AIProperties ai,
                                   NotificationProperties notification,
                                   ApiProperties api,
                                   EmailProperties email,
                                   Environment environment) {
        this.weather = weather;
        this.ai = ai;
        this.notification = notification;
        this.api = api;
        this.email = email;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("==============================================================");
        log.info("[External APIs]");
        log.info("==============================================================");

        List<String> missing = new ArrayList<>();

        // --- Mandatory configuration ---
        checkMandatory();

        // --- Optional API keys (missing = service disabled, app keeps running) ---
        checkOptional("OpenWeather", weather.isConfigured(), "OPENWEATHER_API_KEY",
                "REAL provider", "MOCK fallback", missing);
        checkOptional("Google Gemini", ai.getGemini().isConfigured(), "GOOGLE_GEMINI_API_KEY",
                "REAL provider", "local AI engine fallback", missing);
        checkOptional("Groq", ai.getGroq().isConfigured(), "GROQ_API_KEY",
                "REAL provider", "local AI engine fallback", missing);
        checkOptional("OpenRouteService", isRoutingConfigured(), "OPENROUTESERVICE_API_KEY",
                "REAL provider", "HAVERSINE fallback", missing);
        checkOptional("Firebase push", notification.getFirebase().isConfigured(), "FIREBASE_SERVER_KEY",
                "REAL provider", "mock push fallback", missing);
        checkOptional("Brevo email", notification.getBrevo().isConfigured(), "BREVO_API_KEY",
                "REAL provider", "mock email fallback", missing);
        checkOptional("Resend", email.getResend().isConfigured(),
                "RESEND_API_KEY / RESEND_FROM_EMAIL", "REAL provider", "MOCK fallback", missing);
        checkOptional("Twilio SMS", notification.getTwilio().isConfigured(),
                "TWILIO_ACCOUNT_SID / TWILIO_AUTH_TOKEN / TWILIO_PHONE_NUMBER",
                "REAL provider", "MOCK SMS fallback", missing);
        checkOptional("OpenAQ air quality", api.getOpenaq().isConfigured(), "OPENAQ_API_KEY",
                "REAL provider", "mock air-quality fallback", missing);
        checkOptional("NASA FIRMS fire data", api.getNasaFirms().isConfigured(), "NASA_FIRMS_API_KEY",
                "REAL provider", "mock fire-data fallback", missing);

        if (missing.isEmpty()) {
            log.info("All optional external API keys are configured.");
        }
        log.info("==============================================================");
    }

    private void checkMandatory() {
        String secret = environment.getProperty("app.jwt.secret", "");
        if (secret == null || secret.isBlank()) {
            log.error("[MANDATORY] app.jwt.secret is not set. Set JWT_SECRET "
                    + "(at least 32 bytes) before starting in a production profile.");
        } else {
            try {
                byte[] bytes = java.util.Base64.getDecoder().decode(secret);
                if (bytes.length < 32) {
                    log.error("[MANDATORY] JWT_SECRET decodes to {} bytes; it must be at least 32 bytes (256 bits).",
                            bytes.length);
                } else {
                    log.info("[OK] JWT signing secret configured ({} bytes).", bytes.length);
                }
            } catch (IllegalArgumentException ex) {
                log.error("[MANDATORY] JWT_SECRET is not valid Base64. Generate one with: openssl rand -base64 64");
            }
        }
    }

    private void checkOptional(String service, boolean configured, String envVars,
                               String realLabel, String fallbackLabel, List<String> missing) {
        if (configured) {
            log.info("[OK] {} configured -> {}", service, realLabel);
        } else {
            log.warn("[OPTIONAL] {} not configured (set {}) -> {}", service, envVars, fallbackLabel);
            missing.add(service);
        }
    }

    private boolean isRoutingConfigured() {
        String provider = environment.getProperty("app.routing.provider", "openrouteservice");
        String key = switch (provider.toLowerCase()) {
            case "google", "google-maps" -> environment.getProperty("app.routing.google.api-key", "");
            case "mapbox" -> environment.getProperty("app.routing.mapbox.access-token", "");
            default -> environment.getProperty("app.routing.openrouteservice.api-key", "");
        };
        return key != null && !key.isBlank();
    }
}
