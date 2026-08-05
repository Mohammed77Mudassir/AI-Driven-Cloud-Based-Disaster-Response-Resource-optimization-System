package com.disaster.controller;

import com.disaster.ai.AIEngineService;
import com.disaster.dto.ai.AIAnalysisRequest;
import com.disaster.dto.ai.AIAnalysisResponse;
import com.disaster.dto.ai.AISelfTestResult;
import com.disaster.dto.ai.ModelMetaDTO;
import com.disaster.dto.ai.RecommendationResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Public contract of the AI engine. Every route requires the AI_VIEW permission.
 */
@RestController
@RequestMapping("/api/ai")
public class AIEngineController {

    private final AIEngineService engine;

    public AIEngineController(AIEngineService engine) {
        this.engine = engine;
    }

    @PostMapping("/analyze")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'AI_VIEW')")
    public ResponseEntity<AIAnalysisResponse> analyze(@Valid @RequestBody AIAnalysisRequest request) {
        return ResponseEntity.ok(engine.analyze(request));
    }

    @PostMapping("/recommendations")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'AI_VIEW')")
    public ResponseEntity<RecommendationResponse> recommend(@Valid @RequestBody AIAnalysisRequest request) {
        return ResponseEntity.ok(engine.recommend(request));
    }

    @GetMapping("/models")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'AI_VIEW')")
    public ResponseEntity<ModelMetaDTO> models() {
        return ResponseEntity.ok(engine.modelMetadata());
    }

    @GetMapping("/self-test")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'AI_VIEW')")
    public ResponseEntity<AISelfTestResult> selfTest() {
        return ResponseEntity.ok(engine.selfTest());
    }
}
