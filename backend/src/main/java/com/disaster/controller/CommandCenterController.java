package com.disaster.controller;

import com.disaster.dto.command.CommandCenterDTO;
import com.disaster.service.command.CommandCenterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-only operational dashboard for the Rescue Team Command Center.
 * Aggregation logic lives entirely in {@link CommandCenterService}.
 */
@RestController
@RequestMapping("/api/rescue-teams/command-center")
public class CommandCenterController {

    private final CommandCenterService commandCenterService;

    public CommandCenterController(CommandCenterService commandCenterService) {
        this.commandCenterService = commandCenterService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_VIEW')")
    public ResponseEntity<CommandCenterDTO> getDashboard() {
        return ResponseEntity.ok(commandCenterService.getDashboard());
    }
}
