package com.disaster;

import com.disaster.service.EmailProvider;
import com.disaster.service.ResendEmailProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * End-to-end fallback test: with a blank {@code RESEND_API_KEY} the primary
 * {@link ResendEmailProvider} must transparently delegate to the built-in mock
 * provider, keep returning {@code true} and never call the Resend API.
 */
@SpringBootTest(properties = "email.resend.api-key=")
class ResendEmailFallbackIntegrationTest {

    @Autowired
    private EmailProvider emailProvider;

    @MockBean(name = "emailRestTemplate")
    private RestTemplate emailRestTemplate;

    @Test
    void fallsBackToMockWhenApiKeyIsMissing() {
        assertTrue(emailProvider instanceof ResendEmailProvider);

        boolean sent = emailProvider.sendEmail("user@example.com", "Test", "Hello");

        assertTrue(sent, "mock fallback must report success exactly like before");
        verifyNoInteractions(emailRestTemplate);
    }
}
