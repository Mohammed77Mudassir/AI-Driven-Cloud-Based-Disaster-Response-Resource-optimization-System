package com.disaster.controller;

import com.disaster.dto.DroneDTO;
import com.disaster.service.DroneService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/drones")
public class DroneController {
    private final DroneService droneService;
    
    public DroneController(DroneService droneService) {
        this.droneService = droneService;
    }
    
    @GetMapping
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'DRONE_VIEW')")
    public ResponseEntity<List<DroneDTO>> getAll() {
        return ResponseEntity.ok(droneService.getAll());
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'DRONE_VIEW')")
    public ResponseEntity<DroneDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(droneService.getById(id));
    }
    
    @PostMapping
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'DRONE_MANAGE')")
    public ResponseEntity<DroneDTO> create(@Valid @RequestBody DroneDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(droneService.create(dto));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'DRONE_MANAGE')")
    public ResponseEntity<DroneDTO> update(@PathVariable Long id, @Valid @RequestBody DroneDTO dto) {
        return ResponseEntity.ok(droneService.update(id, dto));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'DRONE_MANAGE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        droneService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{id}/location")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'DRONE_MANAGE')")
    public ResponseEntity<DroneDTO> updateLocation(@PathVariable Long id, @RequestBody Map<String, Double> body) {
        return ResponseEntity.ok(droneService.updateLocation(id, body.get("latitude"), body.get("longitude")));
    }
}
