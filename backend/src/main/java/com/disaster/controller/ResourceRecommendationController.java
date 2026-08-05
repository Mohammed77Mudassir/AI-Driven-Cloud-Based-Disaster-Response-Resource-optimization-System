package com.disaster.controller;

import com.disaster.dto.ResourceRecommendationDTO;
import com.disaster.service.ResourceOptimizationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommendations")
public class ResourceRecommendationController {
    private final ResourceOptimizationService resourceOptimizationService;
    
    public ResourceRecommendationController(ResourceOptimizationService resourceOptimizationService) {
        this.resourceOptimizationService = resourceOptimizationService;
    }
    
    @PostMapping
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'AI_VIEW')")
    public ResponseEntity<ResourceRecommendationDTO> recommend(@Valid @RequestBody ResourceRecommendationDTO request) {
        ResourceRecommendationDTO result = resourceOptimizationService.recommend(
            request.getDisasterType(),
            request.getSeverity(),
            request.getPopulation() > 0 ? request.getPopulation() : 10000,
            request.getLocation()
        );
        return ResponseEntity.ok(result);
    }
}
