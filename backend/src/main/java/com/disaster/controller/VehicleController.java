package com.disaster.controller;

import com.disaster.dto.RescueVehicleDTO;
import com.disaster.enums.VehicleStatus;
import com.disaster.security.UserDetailsImpl;
import com.disaster.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rescue-vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_VIEW')")
    public ResponseEntity<List<RescueVehicleDTO>> getAll() {
        return ResponseEntity.ok(vehicleService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_VIEW')")
    public ResponseEntity<RescueVehicleDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.getById(id));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_VIEW')")
    public ResponseEntity<List<RescueVehicleDTO>> getByStatus(@PathVariable VehicleStatus status) {
        return ResponseEntity.ok(vehicleService.getByStatus(status));
    }

    @GetMapping("/team/{teamId}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_VIEW')")
    public ResponseEntity<List<RescueVehicleDTO>> getByTeam(@PathVariable Long teamId) {
        return ResponseEntity.ok(vehicleService.getByTeam(teamId));
    }

    @GetMapping("/mission/{missionId}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_VIEW')")
    public ResponseEntity<List<RescueVehicleDTO>> getByMission(@PathVariable Long missionId) {
        return ResponseEntity.ok(vehicleService.getByMission(missionId));
    }

    @PostMapping
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_MANAGE')")
    public ResponseEntity<RescueVehicleDTO> create(@Valid @RequestBody RescueVehicleDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_MANAGE')")
    public ResponseEntity<RescueVehicleDTO> update(@PathVariable Long id, @Valid @RequestBody RescueVehicleDTO dto) {
        return ResponseEntity.ok(vehicleService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_MANAGE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehicleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/deploy")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_MANAGE')")
    public ResponseEntity<RescueVehicleDTO> deploy(@PathVariable Long id,
                                                   @RequestBody(required = false) Map<String, Object> body,
                                                   @AuthenticationPrincipal UserDetailsImpl user) {
        Long missionId = body != null && body.get("missionId") != null
                ? Long.valueOf(body.get("missionId").toString()) : null;
        if (missionId == null) {
            throw new IllegalArgumentException("missionId is required to deploy a vehicle");
        }
        return ResponseEntity.ok(vehicleService.deploy(id, missionId,
                user != null ? user.getUsername() : "system"));
    }

    @PutMapping("/{id}/return")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_MANAGE')")
    public ResponseEntity<RescueVehicleDTO> returnVehicle(@PathVariable Long id,
                                                          @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(vehicleService.returnVehicle(id,
                user != null ? user.getUsername() : "system"));
    }

    @PutMapping("/{id}/maintenance")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_MANAGE')")
    public ResponseEntity<RescueVehicleDTO> startMaintenance(@PathVariable Long id,
                                                             @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(vehicleService.startMaintenance(id,
                user != null ? user.getUsername() : "system"));
    }

    @PutMapping("/{id}/maintenance/complete")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_MANAGE')")
    public ResponseEntity<RescueVehicleDTO> completeMaintenance(@PathVariable Long id,
                                                                @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(vehicleService.completeMaintenance(id,
                user != null ? user.getUsername() : "system"));
    }
}
