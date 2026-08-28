package com.disaster.controller;

import com.disaster.dto.EonetDTO;
import com.disaster.service.EonetService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Live NASA EONET natural-disaster events. Returns only <em>active</em> events
 * detected inside India from the NASA EONET feed, newest first. Automatically
 * degrades to the last cached data (or an empty list - HTTP 200 - when NASA has
 * no active events inside India or is temporarily unavailable).
 */
@RestController
@RequestMapping("/api/eonet/events")
public class EonetController {

    private final EonetService eonetService;

    public EonetController(EonetService eonetService) {
        this.eonetService = eonetService;
    }

    @GetMapping
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'MAP_VIEW')")
    public ResponseEntity<List<EonetDTO>> getEvents() {
        return ResponseEntity.ok(eonetService.getEvents());
    }
}
