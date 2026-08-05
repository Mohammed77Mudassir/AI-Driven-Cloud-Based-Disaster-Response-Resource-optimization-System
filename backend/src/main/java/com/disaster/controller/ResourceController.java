package com.disaster.controller;

import com.disaster.dto.ResourceDTO;
import com.disaster.dto.ResourceMovementDTO;
import com.disaster.enums.ResourceMovementType;
import com.disaster.security.UserDetailsImpl;
import com.disaster.service.ResourceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {
    private final ResourceService resourceService;
    
    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }
    
    @GetMapping
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESOURCE_VIEW')")
    public ResponseEntity<List<ResourceDTO>> getAll() {
        return ResponseEntity.ok(resourceService.getAll());
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESOURCE_VIEW')")
    public ResponseEntity<ResourceDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(resourceService.getById(id));
    }
    
    @GetMapping("/available")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESOURCE_VIEW')")
    public ResponseEntity<List<ResourceDTO>> getAvailable() {
        return ResponseEntity.ok(resourceService.getAvailable());
    }
    
    @GetMapping("/disaster/{disasterId}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESOURCE_VIEW')")
    public ResponseEntity<List<ResourceDTO>> getByDisaster(@PathVariable Long disasterId) {
        return ResponseEntity.ok(resourceService.getByDisaster(disasterId));
    }
    
    @GetMapping("/mission/{missionId}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESOURCE_VIEW')")
    public ResponseEntity<List<ResourceDTO>> getByMission(@PathVariable Long missionId) {
        return ResponseEntity.ok(resourceService.getByMission(missionId));
    }
    
    @GetMapping("/movements")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESOURCE_VIEW')")
    public ResponseEntity<List<ResourceMovementDTO>> getMovements(
            @RequestParam(required = false) ResourceMovementType type,
            @RequestParam(required = false) Long missionId) {
        if (type != null) {
            return ResponseEntity.ok(resourceService.getMovementsByType(type));
        }
        if (missionId != null) {
            return ResponseEntity.ok(resourceService.getMovementsByMission(missionId));
        }
        return ResponseEntity.ok(resourceService.getMovements());
    }
    
    @GetMapping("/{id}/movements")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESOURCE_VIEW')")
    public ResponseEntity<List<ResourceMovementDTO>> getResourceMovements(@PathVariable Long id) {
        return ResponseEntity.ok(resourceService.getMovementsByResource(id));
    }
    
    @PostMapping
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESOURCE_MANAGE')")
    public ResponseEntity<ResourceDTO> create(@Valid @RequestBody ResourceDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resourceService.create(dto));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESOURCE_MANAGE')")
    public ResponseEntity<ResourceDTO> update(@PathVariable Long id, @Valid @RequestBody ResourceDTO dto) {
        return ResponseEntity.ok(resourceService.update(id, dto));
    }
    
    @PutMapping("/{id}/deploy")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESOURCE_MANAGE')")
    public ResponseEntity<ResourceDTO> deploy(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                              @AuthenticationPrincipal UserDetailsImpl user) {
        int quantity = body.get("quantity") != null ? Integer.parseInt(body.get("quantity").toString()) : 0;
        Long missionId = body.get("missionId") != null ? Long.valueOf(body.get("missionId").toString()) : null;
        if (missionId == null) {
            throw new IllegalArgumentException("missionId is required to deploy a resource");
        }
        return ResponseEntity.ok(resourceService.deploy(id, quantity, missionId,
                user != null ? user.getUsername() : "system"));
    }
    
    @PutMapping("/{id}/return")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESOURCE_MANAGE')")
    public ResponseEntity<ResourceDTO> returnResource(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                                      @AuthenticationPrincipal UserDetailsImpl user) {
        int quantity = body.get("quantity") != null ? Integer.parseInt(body.get("quantity").toString()) : 0;
        return ResponseEntity.ok(resourceService.returnResource(id, quantity,
                user != null ? user.getUsername() : "system"));
    }
    
    @PutMapping("/{id}/maintenance")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESOURCE_MANAGE')")
    public ResponseEntity<ResourceDTO> startMaintenance(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                                        @AuthenticationPrincipal UserDetailsImpl user) {
        int quantity = body.get("quantity") != null ? Integer.parseInt(body.get("quantity").toString()) : 0;
        return ResponseEntity.ok(resourceService.startMaintenance(id, quantity,
                user != null ? user.getUsername() : "system"));
    }
    
    @PutMapping("/{id}/maintenance/complete")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESOURCE_MANAGE')")
    public ResponseEntity<ResourceDTO> completeMaintenance(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                                           @AuthenticationPrincipal UserDetailsImpl user) {
        int quantity = body.get("quantity") != null ? Integer.parseInt(body.get("quantity").toString()) : 0;
        return ResponseEntity.ok(resourceService.completeMaintenance(id, quantity,
                user != null ? user.getUsername() : "system"));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'RESOURCE_MANAGE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        resourceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
