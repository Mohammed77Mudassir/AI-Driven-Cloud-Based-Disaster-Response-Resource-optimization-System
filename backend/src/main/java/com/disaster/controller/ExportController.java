package com.disaster.controller;

import com.disaster.service.ExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exports")
@PreAuthorize("@rbacService.hasPermission(authentication.principal, 'EXPORT_VIEW')")
public class ExportController {
    private final ExportService exportService;
    
    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }
    
    @GetMapping("/csv")
    public ResponseEntity<byte[]> exportCSV() {
        byte[] data = exportService.exportCSV();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=disasters.csv")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(data);
    }
    
    @GetMapping("/pdf")
    public ResponseEntity<byte[]> exportPDF() {
        byte[] data = exportService.exportPDF();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=disasters.pdf")
            .contentType(MediaType.parseMediaType("application/pdf"))
            .body(data);
    }
    
    @GetMapping("/excel")
    public ResponseEntity<byte[]> exportExcel() {
        byte[] data = exportService.exportExcel();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=disasters.xls")
            .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
            .body(data);
    }
}
