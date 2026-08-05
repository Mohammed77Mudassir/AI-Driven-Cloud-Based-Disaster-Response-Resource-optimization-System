package com.disaster.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MockEmailProvider implements EmailProvider {
    private static final Logger log = LoggerFactory.getLogger(MockEmailProvider.class);

    @Override
    public boolean sendEmail(String to, String subject, String body) {
        log.info("[MOCK EMAIL] To: {} | Subject: {} | Body: {}", to, subject, body);
        return true;
    }
}
