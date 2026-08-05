package com.disaster.controller;

import com.disaster.dto.StatusTimelineDTO;
import com.disaster.repository.StatusTimelineRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/timelines")
public class TimelineController {
    private final StatusTimelineRepository statusTimelineRepository;
    
    public TimelineController(StatusTimelineRepository statusTimelineRepository) {
        this.statusTimelineRepository = statusTimelineRepository;
    }
    
    @GetMapping("/disaster/{disasterId}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'DISASTER_VIEW')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<StatusTimelineDTO>> getTimeline(@PathVariable Long disasterId) {
        return ResponseEntity.ok(
            statusTimelineRepository.findByDisasterIdOrderByChangedAtAsc(disasterId)
                .stream().map(StatusTimelineDTO::fromEntity).toList()
        );
    }
}
