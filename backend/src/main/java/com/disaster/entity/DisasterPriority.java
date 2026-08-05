package com.disaster.entity;

/**
 * Operational priority assigned to a disaster report. This drives triage and
 * dispatch order in the response workflow. Defaults to MEDIUM when a report
 * does not explicitly state a priority.
 */
public enum DisasterPriority {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW
}
