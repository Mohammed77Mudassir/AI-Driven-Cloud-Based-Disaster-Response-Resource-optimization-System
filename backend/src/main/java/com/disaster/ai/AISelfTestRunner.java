package com.disaster.ai;

import com.disaster.dto.ai.AISelfTestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Runs the AI engine self-test once at startup and logs a health summary so any
 * regression in the offline engine surfaces immediately at boot.
 * {@code @Order(2)} runs after {@code DataSeeder} (@Order(1)) so the
 * recommendation checks execute against the seeded hospitals/shelters/volunteers.
 */
@Component
@Order(2)
public class AISelfTestRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AISelfTestRunner.class);

    private final AIEngineService engine;

    public AISelfTestRunner(AIEngineService engine) {
        this.engine = engine;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            AISelfTestResult result = engine.selfTest();
            log.info("AI Engine self-test completed: passed={}, checks={}",
                    result.isPassed(), result.getChecks().size());
            result.getChecks().forEach(c ->
                    log.info("  [{}] {} - {}", c.isPassed() ? "PASS" : "FAIL", c.getName(), c.getDetails()));
        } catch (Exception ex) {
            log.warn("AI Engine self-test could not run at startup: {}", ex.getMessage());
        }
    }
}
