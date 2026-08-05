package com.disaster.controller;

import com.disaster.dto.CaseStudyDTO;
import com.disaster.dto.CaseStudyRequest;
import com.disaster.service.CaseStudyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/case-studies")
public class CaseStudyController {
    private final CaseStudyService caseStudyService;

    public CaseStudyController(CaseStudyService caseStudyService) {
        this.caseStudyService = caseStudyService;
    }

    @GetMapping
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'CASE_STUDY_VIEW')")
    public ResponseEntity<List<CaseStudyDTO>> getAllCaseStudies() {
        return ResponseEntity.ok(caseStudyService.getAllCaseStudies());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'CASE_STUDY_VIEW')")
    public ResponseEntity<CaseStudyDTO> getCaseStudyById(@PathVariable Long id) {
        return ResponseEntity.ok(caseStudyService.getCaseStudyById(id));
    }

    @PostMapping
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'CASE_STUDY_MANAGE')")
    public ResponseEntity<CaseStudyDTO> createCaseStudy(@Valid @RequestBody CaseStudyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(caseStudyService.createCaseStudy(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'CASE_STUDY_MANAGE')")
    public ResponseEntity<CaseStudyDTO> updateCaseStudy(@PathVariable Long id, @Valid @RequestBody CaseStudyRequest request) {
        return ResponseEntity.ok(caseStudyService.updateCaseStudy(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'CASE_STUDY_MANAGE')")
    public ResponseEntity<Void> deleteCaseStudy(@PathVariable Long id) {
        caseStudyService.deleteCaseStudy(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'CASE_STUDY_VIEW')")
    public ResponseEntity<?> search(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String disasterType,
            @RequestParam(required = false) String location) {
        String effectiveType = (disasterType != null && !disasterType.isEmpty()) ? disasterType : type;
        if (year != null)
            return ResponseEntity.ok(caseStudyService.searchByYear(year));
        if (effectiveType != null && !effectiveType.isEmpty())
            return ResponseEntity.ok(caseStudyService.searchByDisasterType(effectiveType));
        if (location != null && !location.isEmpty())
            return ResponseEntity.ok(caseStudyService.searchByLocation(location));
        return ResponseEntity.ok(caseStudyService.getAllCaseStudies());
    }

    @GetMapping("/comparison")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'CASE_STUDY_VIEW')")
    public ResponseEntity<List<CaseStudyDTO>> getComparison(
            @RequestParam(required = false) String disasterType,
            @RequestParam(required = false) Integer yearFrom,
            @RequestParam(required = false) Integer yearTo,
            @RequestParam(required = false) Integer fromYear,
            @RequestParam(required = false) Integer toYear) {
        Integer effectiveFrom = fromYear != null ? fromYear : yearFrom;
        Integer effectiveTo = toYear != null ? toYear : yearTo;
        return ResponseEntity.ok(caseStudyService.getComparisonData(disasterType, effectiveFrom, effectiveTo));
    }
}
