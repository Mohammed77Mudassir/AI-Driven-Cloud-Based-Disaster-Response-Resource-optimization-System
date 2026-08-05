package com.disaster.controller;

import com.disaster.dto.*;
import com.disaster.entity.DisasterPriority;
import com.disaster.security.UserDetailsImpl;
import com.disaster.service.DisasterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/disasters")
public class DisasterController {

    private final DisasterService disasterService;

    public DisasterController(DisasterService disasterService) {
        this.disasterService = disasterService;
    }

    @PostMapping
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'DISASTER_CREATE')")
    public ResponseEntity<DisasterResponse> createDisaster(
            @Valid @RequestBody DisasterRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(disasterService.createDisaster(request, userDetails.getId()));
    }

    @GetMapping("/my")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'DISASTER_VIEW')")
    public ResponseEntity<PageResponse<DisasterResponse>> getMyDisasters(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(disasterService.queryDisasters(
                userDetails.getId(), search, type, severity, status, priority, null,
                page, size, sortBy, sortDir));
    }

    @GetMapping
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'DISASTER_VIEW')")
    public ResponseEntity<PageResponse<DisasterResponse>> getAllDisasters(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String source,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(disasterService.queryDisasters(
                null, search, type, severity, status, priority, source,
                page, size, sortBy, sortDir));
    }

    /**
     * Status workflow definition (PENDING -> VERIFIED -> ASSIGNED ->
     * RESOURCES_DISPATCHED -> IN_PROGRESS -> RESOLVED) for workflow UIs.
     */
    @GetMapping("/status-flow")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'DISASTER_VIEW')")
    public ResponseEntity<Map<String, List<String>>> getStatusFlow() {
        return ResponseEntity.ok(disasterService.getStatusFlow());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'DISASTER_VIEW')")
    public ResponseEntity<DisasterResponse> getDisaster(@PathVariable Long id) {
        return ResponseEntity.ok(disasterService.getDisaster(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'DISASTER_UPDATE')")
    public ResponseEntity<DisasterResponse> updateDisaster(
            @PathVariable Long id, @Valid @RequestBody DisasterRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(disasterService.updateDisaster(id, request, userDetails.getUsername()));
    }

    @PutMapping("/{id}/priority")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'DISASTER_UPDATE')")
    public ResponseEntity<DisasterResponse> updatePriority(
            @PathVariable Long id, @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        DisasterPriority priority = DisasterPriority.valueOf(body.getOrDefault("priority", "MEDIUM").toUpperCase());
        return ResponseEntity.ok(disasterService.updatePriority(id, priority, userDetails.getUsername()));
    }

    @GetMapping("/{id}/detail")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'DISASTER_VIEW')")
    public ResponseEntity<DisasterDetailDTO> getDisasterDetail(@PathVariable Long id) {
        return ResponseEntity.ok(disasterService.getDisasterDetail(id));
    }

    @GetMapping("/{id}/timeline")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'DISASTER_VIEW')")
    public ResponseEntity<List<StatusTimelineDTO>> getTimeline(@PathVariable Long id) {
        return ResponseEntity.ok(disasterService.getTimeline(id));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'DISASTER_UPDATE')")
    public ResponseEntity<DisasterResponse> updateStatus(
            @PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(disasterService.updateStatus(id, request, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'DISASTER_DELETE')")
    public ResponseEntity<Void> deleteDisaster(@PathVariable Long id) {
        disasterService.deleteDisaster(id);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------
    // Comments
    // ------------------------------------------------------------------

    @PostMapping("/{id}/comments")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'DISASTER_VIEW')")
    public ResponseEntity<DisasterCommentDTO> addComment(
            @PathVariable Long id, @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(disasterService.addComment(id, request, userDetails.getId()));
    }

    @GetMapping("/{id}/comments")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'DISASTER_VIEW')")
    public ResponseEntity<List<DisasterCommentDTO>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(disasterService.getComments(id));
    }

    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'DISASTER_VIEW')")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId,
                                              @AuthenticationPrincipal UserDetailsImpl userDetails) {
        disasterService.deleteComment(commentId, userDetails.getId(), userDetails.getRole());
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------
    // Assignment history
    // ------------------------------------------------------------------

    @GetMapping("/{id}/assignments")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'DISASTER_VIEW')")
    public ResponseEntity<List<DisasterAssignmentDTO>> getAssignments(@PathVariable Long id) {
        return ResponseEntity.ok(disasterService.getAssignments(id));
    }
}
