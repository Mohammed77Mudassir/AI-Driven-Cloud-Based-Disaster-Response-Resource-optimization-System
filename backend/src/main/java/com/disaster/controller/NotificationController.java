package com.disaster.controller;

import com.disaster.dto.NotificationDTO;
import com.disaster.security.UserDetailsImpl;
import com.disaster.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@PreAuthorize("@rbacService.hasPermission(authentication.principal, 'NOTIFICATION_VIEW')")
public class NotificationController {
    private final NotificationService notificationService;
    
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getNotifications(@AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(notificationService.getUserNotifications(user.getId()));
    }
    
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDTO>> getUnread(@AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(user.getId()));
    }
    
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(Map.of("count", notificationService.getUnreadCount(user.getId())));
    }
    
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id,
                                           @AuthenticationPrincipal UserDetailsImpl user) {
        notificationService.markAsRead(id, user.getId());
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal UserDetailsImpl user) {
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok().build();
    }
}
