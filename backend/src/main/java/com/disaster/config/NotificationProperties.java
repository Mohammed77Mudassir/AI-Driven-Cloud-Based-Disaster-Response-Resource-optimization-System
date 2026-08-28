package com.disaster.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for notification and alerting providers (push, email, SMS).
 *
 * <p>Binds the {@code notification.*} properties, populated from the
 * {@code FIREBASE_SERVER_KEY}, {@code BREVO_API_KEY} and {@code TWILIO_*}
 * environment variables (see application.properties). Blank values simply
 * disable the provider and the application falls back to its mock
 * implementations.</p>
 *
 * <pre>
 * notification.firebase.server-key  = ${FIREBASE_SERVER_KEY:}
 * notification.brevo.api-key        = ${BREVO_API_KEY:}
 * notification.twilio.account-sid   = ${TWILIO_ACCOUNT_SID:}
 * notification.twilio.auth-token    = ${TWILIO_AUTH_TOKEN:}
 * notification.twilio.phone-number  = ${TWILIO_PHONE_NUMBER:}
 * </pre>
 */
@ConfigurationProperties(prefix = "notification")
public class NotificationProperties {

    private Firebase firebase = new Firebase();
    private Brevo brevo = new Brevo();
    private Twilio twilio = new Twilio();

    public Firebase getFirebase() {
        return firebase;
    }

    public void setFirebase(Firebase firebase) {
        this.firebase = firebase;
    }

    public Brevo getBrevo() {
        return brevo;
    }

    public void setBrevo(Brevo brevo) {
        this.brevo = brevo;
    }

    public Twilio getTwilio() {
        return twilio;
    }

    public void setTwilio(Twilio twilio) {
        this.twilio = twilio;
    }

    /** Firebase Cloud Messaging server key. */
    public static class Firebase {
        private String serverKey = "";

        public boolean isConfigured() {
            return serverKey != null && !serverKey.isBlank();
        }

        public String getServerKey() {
            return serverKey;
        }

        public void setServerKey(String serverKey) {
            this.serverKey = serverKey;
        }
    }

    /** Brevo (Sendinblue) transactional email credentials. */
    public static class Brevo {
        private String apiKey = "";

        public boolean isConfigured() {
            return apiKey != null && !apiKey.isBlank();
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
    }

    /** Twilio SMS credentials. */
    public static class Twilio {
        private String accountSid = "";
        private String authToken = "";
        private String phoneNumber = "";

        public boolean isConfigured() {
            return accountSid != null && !accountSid.isBlank()
                    && authToken != null && !authToken.isBlank()
                    && phoneNumber != null && !phoneNumber.isBlank();
        }

        public String getAccountSid() {
            return accountSid;
        }

        public void setAccountSid(String accountSid) {
            this.accountSid = accountSid;
        }

        public String getAuthToken() {
            return authToken;
        }

        public void setAuthToken(String authToken) {
            this.authToken = authToken;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }
    }
}
