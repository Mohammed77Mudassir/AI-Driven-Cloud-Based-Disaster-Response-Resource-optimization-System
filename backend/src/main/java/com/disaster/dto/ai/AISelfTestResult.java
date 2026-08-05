package com.disaster.dto.ai;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Self-test result that exercises every AI engine capability and reports
 * pass/fail per check so the whole engine can be validated at runtime.
 */
public class AISelfTestResult {

    private boolean passed;
    private LocalDateTime checkedAt;
    private List<Check> checks = new ArrayList<>();

    public AISelfTestResult() {}

    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }
    public LocalDateTime getCheckedAt() { return checkedAt; }
    public void setCheckedAt(LocalDateTime checkedAt) { this.checkedAt = checkedAt; }
    public List<Check> getChecks() { return checks; }
    public void setChecks(List<Check> checks) { this.checks = checks; }

    public static class Check {
        private String name;
        private boolean passed;
        private String details;

        public Check() {}

        public Check(String name, boolean passed, String details) {
            this.name = name;
            this.passed = passed;
            this.details = details;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public boolean isPassed() { return passed; }
        public void setPassed(boolean passed) { this.passed = passed; }
        public String getDetails() { return details; }
        public void setDetails(String details) { this.details = details; }
    }
}
