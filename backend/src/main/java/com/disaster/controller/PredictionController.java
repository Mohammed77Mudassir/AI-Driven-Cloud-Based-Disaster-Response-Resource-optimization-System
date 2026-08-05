package com.disaster.controller;

import com.disaster.dto.PredictionDTO;
import com.disaster.service.AIPredictionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/predictions")
public class PredictionController {
    private final AIPredictionService predictionService;
    
    public PredictionController(AIPredictionService predictionService) {
        this.predictionService = predictionService;
    }
    
    @PostMapping
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'AI_VIEW')")
    public ResponseEntity<PredictionDTO> predict(@Valid @RequestBody PredictionDTO request) {
        PredictionDTO result = predictionService.predict(
            request.getDisasterType(),
            request.getSeverity(),
            request.getLocation(),
            request.getLatitude(),
            request.getLongitude()
        );
        return ResponseEntity.ok(result);
    }
}
