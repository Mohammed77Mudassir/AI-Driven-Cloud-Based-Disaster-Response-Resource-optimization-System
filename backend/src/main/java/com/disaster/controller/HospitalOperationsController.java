package com.disaster.controller;

import com.disaster.dto.hospital.HospitalOperationsDashboardDTO;
import com.disaster.service.HospitalOperationsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-only operational dashboard for the Hospital Management module.
 * Aggregation logic lives entirely in {@link HospitalOperationsService}.
 */
@RestController
@RequestMapping("/api/hospitals/operations")
public class HospitalOperationsController {

    private final HospitalOperationsService hospitalOperationsService;

    public HospitalOperationsController(HospitalOperationsService hospitalOperationsService) {
        this.hospitalOperationsService = hospitalOperationsService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'HOSPITAL_VIEW')")
    public ResponseEntity<HospitalOperationsDashboardDTO> getDashboard() {
        return ResponseEntity.ok(hospitalOperationsService.getDashboard());
    }
}
