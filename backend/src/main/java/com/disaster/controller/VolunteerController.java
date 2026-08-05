package com.disaster.controller;

import com.disaster.dto.VolunteerDTO;
import com.disaster.service.VolunteerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/volunteers")
public class VolunteerController {
    private final VolunteerService volunteerService;
    
    public VolunteerController(VolunteerService volunteerService) {
        this.volunteerService = volunteerService;
    }
    
    @GetMapping
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'VOLUNTEER_VIEW')")
    public ResponseEntity<List<VolunteerDTO>> getAll() {
        return ResponseEntity.ok(volunteerService.getAll());
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'VOLUNTEER_VIEW')")
    public ResponseEntity<VolunteerDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(volunteerService.getById(id));
    }
    
    @GetMapping("/available")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'VOLUNTEER_VIEW')")
    public ResponseEntity<List<VolunteerDTO>> getAvailable() {
        return ResponseEntity.ok(volunteerService.getAvailable());
    }
    
    @PostMapping
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'VOLUNTEER_MANAGE')")
    public ResponseEntity<VolunteerDTO> create(@Valid @RequestBody VolunteerDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(volunteerService.create(dto));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'VOLUNTEER_MANAGE')")
    public ResponseEntity<VolunteerDTO> update(@PathVariable Long id, @Valid @RequestBody VolunteerDTO dto) {
        return ResponseEntity.ok(volunteerService.update(id, dto));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'VOLUNTEER_MANAGE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        volunteerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
