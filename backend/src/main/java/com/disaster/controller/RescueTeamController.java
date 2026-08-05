package com.disaster.controller;

import com.disaster.dto.RescueTeamDTO;
import com.disaster.dto.TeamAvailabilityDTO;
import com.disaster.dto.TeamLocationDTO;
import com.disaster.dto.TeamMemberDTO;
import com.disaster.service.LiveTrackingService;
import com.disaster.service.RescueTeamService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rescue-teams")
public class RescueTeamController {
    private final RescueTeamService rescueTeamService;
    private final LiveTrackingService liveTrackingService;

    public RescueTeamController(RescueTeamService rescueTeamService,
                                LiveTrackingService liveTrackingService) {
        this.rescueTeamService = rescueTeamService;
        this.liveTrackingService = liveTrackingService;
    }

    @GetMapping
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_VIEW')")
    public ResponseEntity<List<RescueTeamDTO>> getAllTeams() {
        return ResponseEntity.ok(rescueTeamService.getAllTeams());
    }

    @GetMapping("/availability")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_VIEW')")
    public ResponseEntity<List<TeamAvailabilityDTO>> getAvailabilityOverview() {
        return ResponseEntity.ok(rescueTeamService.getAvailabilityOverview());
    }

    @GetMapping("/locations/latest")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_VIEW')")
    public ResponseEntity<List<TeamLocationDTO>> getLatestLocations() {
        return ResponseEntity.ok(liveTrackingService.getLatestForAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_VIEW')")
    public ResponseEntity<RescueTeamDTO> getTeamById(@PathVariable Long id) {
        return ResponseEntity.ok(rescueTeamService.getTeamById(id));
    }

    @GetMapping("/{id}/availability")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_VIEW')")
    public ResponseEntity<TeamAvailabilityDTO> getTeamAvailability(@PathVariable Long id) {
        return ResponseEntity.ok(rescueTeamService.getTeamAvailability(id));
    }

    @GetMapping("/{id}/locations")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_VIEW')")
    public ResponseEntity<List<TeamLocationDTO>> getTeamLocations(@PathVariable Long id) {
        return ResponseEntity.ok(liveTrackingService.getHistory(id));
    }

    @GetMapping("/{id}/locations/latest")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_VIEW')")
    public ResponseEntity<List<TeamLocationDTO>> getTeamLatestLocation(@PathVariable Long id) {
        return ResponseEntity.ok(liveTrackingService.getLatestForTeam(id));
    }

    @PostMapping("/{id}/location")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_VIEW')")
    public ResponseEntity<TeamLocationDTO> recordLocation(@PathVariable Long id, @RequestBody TeamLocationDTO dto) {
        return ResponseEntity.ok(liveTrackingService.recordLocation(id, dto));
    }

    @PostMapping
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_MANAGE')")
    public ResponseEntity<RescueTeamDTO> createTeam(@Valid @RequestBody RescueTeamDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rescueTeamService.createTeam(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_MANAGE')")
    public ResponseEntity<RescueTeamDTO> updateTeam(@PathVariable Long id, @Valid @RequestBody RescueTeamDTO dto) {
        return ResponseEntity.ok(rescueTeamService.updateTeam(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_MANAGE')")
    public ResponseEntity<Void> deleteTeam(@PathVariable Long id) {
        rescueTeamService.deleteTeam(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/assign/{disasterId}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_MANAGE')")
    public ResponseEntity<RescueTeamDTO> assignToDisaster(@PathVariable Long id, @PathVariable Long disasterId) {
        return ResponseEntity.ok(rescueTeamService.assignToDisaster(id, disasterId));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_MANAGE')")
    public ResponseEntity<RescueTeamDTO> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(rescueTeamService.updateTeamStatus(id, body.get("status")));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_VIEW')")
    public ResponseEntity<List<RescueTeamDTO>> getTeamsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(rescueTeamService.getTeamsByStatus(status));
    }

    @GetMapping("/disaster/{disasterId}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_VIEW')")
    public ResponseEntity<List<RescueTeamDTO>> getTeamsByDisaster(@PathVariable Long disasterId) {
        return ResponseEntity.ok(rescueTeamService.getTeamsByDisaster(disasterId));
    }

    @GetMapping("/{id}/members")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_VIEW')")
    public ResponseEntity<List<TeamMemberDTO>> getTeamMembers(@PathVariable Long id) {
        return ResponseEntity.ok(rescueTeamService.getTeamMembers(id));
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_MANAGE')")
    public ResponseEntity<TeamMemberDTO> addMember(@PathVariable Long id, @Valid @RequestBody TeamMemberDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rescueTeamService.addTeamMember(id, dto));
    }

    @PutMapping("/members/{memberId}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_MANAGE')")
    public ResponseEntity<TeamMemberDTO> updateMember(@PathVariable Long memberId, @Valid @RequestBody TeamMemberDTO dto) {
        return ResponseEntity.ok(rescueTeamService.updateTeamMember(memberId, dto));
    }

    @PutMapping("/members/{memberId}/leader")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_MANAGE')")
    public ResponseEntity<RescueTeamDTO> assignLeader(@PathVariable Long memberId, @RequestBody Map<String, Long> body) {
        Long teamId = body.get("teamId");
        if (teamId == null) {
            throw new IllegalArgumentException("teamId is required to assign a leader");
        }
        return ResponseEntity.ok(rescueTeamService.assignLeader(teamId, memberId));
    }

    @DeleteMapping("/members/{memberId}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_MANAGE')")
    public ResponseEntity<Void> removeMember(@PathVariable Long memberId) {
        rescueTeamService.removeTeamMember(memberId);
        return ResponseEntity.noContent().build();
    }
}
