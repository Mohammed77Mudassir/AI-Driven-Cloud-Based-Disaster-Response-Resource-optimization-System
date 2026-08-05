package com.disaster.controller;

import com.disaster.dto.shelter.ShelterOperationsDashboardDTO;
import com.disaster.service.ShelterOperationsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-only operational dashboard for the Shelter Management module.
 * Aggregation logic lives entirely in {@link ShelterOperationsService}.
 */
@RestController
@RequestMapping("/api/shelters/operations")
public class ShelterOperationsController {

    private final ShelterOperationsService shelterOperationsService;

    public ShelterOperationsController(ShelterOperationsService shelterOperationsService) {
        this.shelterOperationsService = shelterOperationsService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'SHELTER_VIEW')")
    public ResponseEntity<ShelterOperationsDashboardDTO> getDashboard() {
        return ResponseEntity.ok(shelterOperationsService.getDashboard());
    }
}
