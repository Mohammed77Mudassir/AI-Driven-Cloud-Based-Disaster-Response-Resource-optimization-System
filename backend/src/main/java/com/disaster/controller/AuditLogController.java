package com.disaster.controller;

import com.disaster.dto.AuditLogDTO;
import com.disaster.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {
    private final AuditService auditService;
    
    public AuditLogController(AuditService auditService) {
        this.auditService = auditService;
    }
    
    @GetMapping
    public ResponseEntity<List<AuditLogDTO>> getAll() {
        return ResponseEntity.ok(auditService.getAll());
    }
    
    @GetMapping("/user/{username}")
    public ResponseEntity<List<AuditLogDTO>> getByUser(@PathVariable String username) {
        return ResponseEntity.ok(auditService.getByUser(username));
    }
}
