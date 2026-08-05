package com.disaster.service.command;

import com.disaster.dto.command.CommandCenterDTO;

/**
 * Aggregates rescue-team operational data into a single command-center
 * dashboard payload: KPIs, active missions, team availability, response-time
 * analytics, equipment readiness, vehicle status, team workload, mission
 * timelines and operational alerts.
 */
public interface CommandCenterService {

    CommandCenterDTO getDashboard();
}
