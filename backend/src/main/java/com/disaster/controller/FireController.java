package com.disaster.controller;

import com.disaster.dto.FireDTO;
import com.disaster.service.FireService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Live NASA FIRMS fire monitoring. Returns active-fire detections inside India
 * from the NASA FIRMS feed, newest first. Automatically degrades to the last
 * cached data (or an empty list - HTTP 200 - when FIRMS is temporarily
 * unavailable or no {@code NASA_FIRMS_API_KEY} is configured).
 */
@RestController
@RequestMapping("/api/fires")
public class FireController {

    private final FireService fireService;

    public FireController(FireService fireService) {
        this.fireService = fireService;
    }

    @GetMapping
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'MAP_VIEW')")
    public ResponseEntity<List<FireDTO>> getFires() {
        return ResponseEntity.ok(fireService.getFires());
    }
}
