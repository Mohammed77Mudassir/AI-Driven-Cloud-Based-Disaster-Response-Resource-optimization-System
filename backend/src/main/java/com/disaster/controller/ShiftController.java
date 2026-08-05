package com.disaster.controller;

import com.disaster.dto.DutyRosterSummaryDTO;
import com.disaster.dto.TeamShiftDTO;
import com.disaster.enums.ShiftStatus;
import com.disaster.security.UserDetailsImpl;
import com.disaster.service.ShiftService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shifts")
public class ShiftController {

    private final ShiftService shiftService;

    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    @GetMapping
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_VIEW')")
    public ResponseEntity<List<TeamShiftDTO>> getAll() {
        return ResponseEntity.ok(shiftService.getAll());
    }

    @GetMapping("/roster")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_VIEW')")
    public ResponseEntity<List<TeamShiftDTO>> getRoster(@RequestParam(required = false)
                                                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                        @RequestParam(required = false)
                                                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                        @RequestParam(required = false)
                                                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (from != null || to != null) {
            return ResponseEntity.ok(shiftService.getRosterForRange(
                    from != null ? from : LocalDate.now(), to != null ? to : from));
        }
        LocalDate rosterDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(shiftService.getRosterForDate(rosterDate));
    }

    @GetMapping("/duty-roster")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_VIEW')")
    public ResponseEntity<DutyRosterSummaryDTO> getDutyRoster(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(shiftService.getDutyRoster(date != null ? date : LocalDate.now()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_VIEW')")
    public ResponseEntity<TeamShiftDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(shiftService.getById(id));
    }

    @GetMapping("/team/{teamId}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_VIEW')")
    public ResponseEntity<List<TeamShiftDTO>> getByTeam(@PathVariable Long teamId) {
        return ResponseEntity.ok(shiftService.getByTeam(teamId));
    }

    @GetMapping("/member/{memberId}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_VIEW')")
    public ResponseEntity<List<TeamShiftDTO>> getByMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(shiftService.getByMember(memberId));
    }

    @PostMapping
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_MANAGE')")
    public ResponseEntity<TeamShiftDTO> create(@Valid @RequestBody TeamShiftDTO dto,
                                               @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(shiftService.create(dto, user != null ? user.getUsername() : "system"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_MANAGE')")
    public ResponseEntity<TeamShiftDTO> update(@PathVariable Long id, @Valid @RequestBody TeamShiftDTO dto) {
        return ResponseEntity.ok(shiftService.update(id, dto));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_MANAGE')")
    public ResponseEntity<TeamShiftDTO> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(shiftService.updateStatus(id, ShiftStatus.valueOf(body.get("status"))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_MANAGE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        shiftService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
