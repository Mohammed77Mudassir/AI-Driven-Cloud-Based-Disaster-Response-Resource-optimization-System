package com.disaster.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MockSMSProvider implements SMSProvider {
    private static final Logger log = LoggerFactory.getLogger(MockSMSProvider.class);

    @Override
    public boolean sendSMS(String to, String message) {
        log.info("[MOCK SMS] To: {} | Message: {}", to, message);
        return true;
    }
}
