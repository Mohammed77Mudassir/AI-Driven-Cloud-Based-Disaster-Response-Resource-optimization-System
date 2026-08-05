package com.disaster.service;

import com.disaster.dto.NotificationDTO;
import java.util.List;

public interface NotificationService {
    List<NotificationDTO> getUserNotifications(Long userId);
    List<NotificationDTO> getUnreadNotifications(Long userId);
    long getUnreadCount(Long userId);
    NotificationDTO createNotification(Long userId, String title, String message, String type);
    void markAsRead(Long notificationId, Long userId);
    void markAllAsRead(Long userId);
}
