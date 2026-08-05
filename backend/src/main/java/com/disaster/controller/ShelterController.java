package com.disaster.controller;

import com.disaster.dto.ShelterDTO;
import com.disaster.service.ShelterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/shelters")
public class ShelterController {
    private final ShelterService shelterService;
    
    public ShelterController(ShelterService shelterService) {
        this.shelterService = shelterService;
    }
    
    @GetMapping
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'SHELTER_VIEW')")
    public ResponseEntity<List<ShelterDTO>> getAll() {
        return ResponseEntity.ok(shelterService.getAll());
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'SHELTER_VIEW')")
    public ResponseEntity<ShelterDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(shelterService.getById(id));
    }
    
    @PostMapping
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'SHELTER_MANAGE')")
    public ResponseEntity<ShelterDTO> create(@Valid @RequestBody ShelterDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shelterService.create(dto));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'SHELTER_MANAGE')")
    public ResponseEntity<ShelterDTO> update(@PathVariable Long id, @Valid @RequestBody ShelterDTO dto) {
        return ResponseEntity.ok(shelterService.update(id, dto));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'SHELTER_MANAGE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        shelterService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
