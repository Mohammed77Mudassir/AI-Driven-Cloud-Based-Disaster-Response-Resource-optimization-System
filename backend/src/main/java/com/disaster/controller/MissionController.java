package com.disaster.controller;

import com.disaster.dto.MissionEventDTO;
import com.disaster.dto.RescueMissionDTO;
import com.disaster.enums.MissionStatus;
import com.disaster.security.UserDetailsImpl;
import com.disaster.service.MissionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/missions")
public class MissionController {

    private final MissionService missionService;

    public MissionController(MissionService missionService) {
        this.missionService = missionService;
    }

    @GetMapping
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_VIEW')")
    public ResponseEntity<List<RescueMissionDTO>> getAll() {
        return ResponseEntity.ok(missionService.getAll());
    }

    @GetMapping("/transitions/{status}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_VIEW')")
    public ResponseEntity<List<MissionStatus>> getAllowedTransitions(@PathVariable MissionStatus status) {
        return ResponseEntity.ok(missionService.getAllowedTransitions(status));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_VIEW')")
    public ResponseEntity<List<RescueMissionDTO>> getByStatus(@PathVariable MissionStatus status) {
        return ResponseEntity.ok(missionService.getByStatus(status));
    }

    @GetMapping("/team/{teamId}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_VIEW')")
    public ResponseEntity<List<RescueMissionDTO>> getByTeam(@PathVariable Long teamId) {
        return ResponseEntity.ok(missionService.getByTeam(teamId));
    }

    @GetMapping("/team/{teamId}/status/{status}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_VIEW')")
    public ResponseEntity<List<RescueMissionDTO>> getByTeamAndStatus(@PathVariable Long teamId,
                                                                     @PathVariable MissionStatus status) {
        return ResponseEntity.ok(missionService.getByTeamAndStatus(teamId, status));
    }

    @GetMapping("/disaster/{disasterId}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_VIEW')")
    public ResponseEntity<List<RescueMissionDTO>> getByDisaster(@PathVariable Long disasterId) {
        return ResponseEntity.ok(missionService.getByDisaster(disasterId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_VIEW')")
    public ResponseEntity<RescueMissionDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(missionService.getById(id));
    }

    @GetMapping("/{id}/events")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_VIEW')")
    public ResponseEntity<List<MissionEventDTO>> getEvents(@PathVariable Long id) {
        return ResponseEntity.ok(missionService.getEvents(id));
    }

    @PostMapping
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_MANAGE')")
    public ResponseEntity<RescueMissionDTO> create(@Valid @RequestBody RescueMissionDTO dto,
                                                   @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(missionService.create(dto, user != null ? user.getUsername() : "system"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_MANAGE')")
    public ResponseEntity<RescueMissionDTO> update(@PathVariable Long id, @Valid @RequestBody RescueMissionDTO dto) {
        return ResponseEntity.ok(missionService.update(id, dto));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_MANAGE')")
    public ResponseEntity<RescueMissionDTO> updateStatus(@PathVariable Long id,
                                                         @RequestBody Map<String, String> body,
                                                         @AuthenticationPrincipal UserDetailsImpl user) {
        MissionStatus status = MissionStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(missionService.updateStatus(id, status, body.get("reason"),
                user != null ? user.getUsername() : "system"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_MANAGE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        missionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
