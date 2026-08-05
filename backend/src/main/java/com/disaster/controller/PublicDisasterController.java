package com.disaster.controller;

import com.disaster.dto.PublicDisasterReportRequest;
import com.disaster.dto.PublicReportDTO;
import com.disaster.dto.StatusTimelineDTO;
import com.disaster.service.DisasterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public endpoints for citizens. Intentionally has NO authentication —
 * anyone may submit a disaster report or track one using the generated
 * Report ID. Only the report the citizen submitted (looked up by its ID)
 * is ever returned; no admin/authorized data is exposed here.
 */
@RestController
@RequestMapping("/api/public/disasters")
public class PublicDisasterController {

    private final DisasterService disasterService;

    public PublicDisasterController(DisasterService disasterService) {
        this.disasterService = disasterService;
    }

    @PostMapping
    public ResponseEntity<PublicReportDTO> submitReport(@Valid @RequestBody PublicDisasterReportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(disasterService.createPublicReport(request));
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<PublicReportDTO> trackReport(@PathVariable String reportId) {
        return ResponseEntity.ok(disasterService.getPublicReport(reportId));
    }

    @GetMapping("/{reportId}/timeline")
    public ResponseEntity<List<StatusTimelineDTO>> trackReportTimeline(@PathVariable String reportId) {
        return ResponseEntity.ok(disasterService.getPublicReportTimeline(reportId));
    }
}
