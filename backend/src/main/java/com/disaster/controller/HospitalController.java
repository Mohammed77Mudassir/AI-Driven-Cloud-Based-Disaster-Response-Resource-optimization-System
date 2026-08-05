package com.disaster.controller;

import com.disaster.dto.HospitalDTO;
import com.disaster.service.HospitalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/hospitals")
public class HospitalController {
    private final HospitalService hospitalService;
    
    public HospitalController(HospitalService hospitalService) {
        this.hospitalService = hospitalService;
    }
    
    @GetMapping
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'HOSPITAL_VIEW')")
    public ResponseEntity<List<HospitalDTO>> getAll() {
        return ResponseEntity.ok(hospitalService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'HOSPITAL_VIEW')")
    public ResponseEntity<HospitalDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(hospitalService.getById(id));
    }

    @PostMapping
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'HOSPITAL_MANAGE')")
    public ResponseEntity<HospitalDTO> create(@Valid @RequestBody HospitalDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hospitalService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'HOSPITAL_MANAGE')")
    public ResponseEntity<HospitalDTO> update(@PathVariable Long id, @Valid @RequestBody HospitalDTO dto) {
        return ResponseEntity.ok(hospitalService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'HOSPITAL_MANAGE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        hospitalService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
