package com.disaster.controller;

import com.disaster.dto.RescueEquipmentDTO;
import com.disaster.enums.EquipmentStatus;
import com.disaster.security.UserDetailsImpl;
import com.disaster.service.EquipmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rescue-equipment")
public class EquipmentController {

    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @GetMapping
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_VIEW')")
    public ResponseEntity<List<RescueEquipmentDTO>> getAll() {
        return ResponseEntity.ok(equipmentService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_VIEW')")
    public ResponseEntity<RescueEquipmentDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(equipmentService.getById(id));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_VIEW')")
    public ResponseEntity<List<RescueEquipmentDTO>> getByStatus(@PathVariable EquipmentStatus status) {
        return ResponseEntity.ok(equipmentService.getByStatus(status));
    }

    @GetMapping("/team/{teamId}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_VIEW')")
    public ResponseEntity<List<RescueEquipmentDTO>> getByTeam(@PathVariable Long teamId) {
        return ResponseEntity.ok(equipmentService.getByTeam(teamId));
    }

    @GetMapping("/mission/{missionId}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_VIEW')")
    public ResponseEntity<List<RescueEquipmentDTO>> getByMission(@PathVariable Long missionId) {
        return ResponseEntity.ok(equipmentService.getByMission(missionId));
    }

    @PostMapping
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_MANAGE')")
    public ResponseEntity<RescueEquipmentDTO> create(@Valid @RequestBody RescueEquipmentDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipmentService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_MANAGE')")
    public ResponseEntity<RescueEquipmentDTO> update(@PathVariable Long id, @Valid @RequestBody RescueEquipmentDTO dto) {
        return ResponseEntity.ok(equipmentService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_MANAGE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        equipmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/deploy")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_MANAGE')")
    public ResponseEntity<RescueEquipmentDTO> deploy(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                                     @AuthenticationPrincipal UserDetailsImpl user) {
        int quantity = body.get("quantity") != null ? Integer.parseInt(body.get("quantity").toString()) : 0;
        Long missionId = body.get("missionId") != null ? Long.valueOf(body.get("missionId").toString()) : null;
        if (missionId == null) {
            throw new IllegalArgumentException("missionId is required to deploy equipment");
        }
        return ResponseEntity.ok(equipmentService.deploy(id, quantity, missionId,
                user != null ? user.getUsername() : "system"));
    }

    @PutMapping("/{id}/return")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_MANAGE')")
    public ResponseEntity<RescueEquipmentDTO> returnEquipment(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                                              @AuthenticationPrincipal UserDetailsImpl user) {
        int quantity = body.get("quantity") != null ? Integer.parseInt(body.get("quantity").toString()) : 0;
        return ResponseEntity.ok(equipmentService.returnEquipment(id, quantity,
                user != null ? user.getUsername() : "system"));
    }

    @PutMapping("/{id}/maintenance")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_MANAGE')")
    public ResponseEntity<RescueEquipmentDTO> startMaintenance(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                                               @AuthenticationPrincipal UserDetailsImpl user) {
        int quantity = body.get("quantity") != null ? Integer.parseInt(body.get("quantity").toString()) : 0;
        return ResponseEntity.ok(equipmentService.startMaintenance(id, quantity,
                user != null ? user.getUsername() : "system"));
    }

    @PutMapping("/{id}/maintenance/complete")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESCUE_TEAM_MANAGE')")
    public ResponseEntity<RescueEquipmentDTO> completeMaintenance(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                                                  @AuthenticationPrincipal UserDetailsImpl user) {
        int quantity = body.get("quantity") != null ? Integer.parseInt(body.get("quantity").toString()) : 0;
        return ResponseEntity.ok(equipmentService.completeMaintenance(id, quantity,
                user != null ? user.getUsername() : "system"));
    }
}
