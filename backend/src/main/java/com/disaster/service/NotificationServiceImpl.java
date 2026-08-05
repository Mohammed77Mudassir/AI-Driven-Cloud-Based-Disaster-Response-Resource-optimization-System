package com.disaster.service;

import com.disaster.dto.NotificationDTO;
import com.disaster.entity.Notification;
import com.disaster.entity.User;
import com.disaster.enums.NotificationType;
import com.disaster.repository.NotificationRepository;
import com.disaster.repository.UserRepository;
import com.disaster.config.WebSocketConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final WebSocketConfig webSocketConfig;
    
    public NotificationServiceImpl(NotificationRepository notificationRepository, UserRepository userRepository,
                                   WebSocketConfig webSocketConfig) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.webSocketConfig = webSocketConfig;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
            .stream().map(NotificationDTO::fromEntity).toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId)
            .stream().map(NotificationDTO::fromEntity).toList();
    }
    
    @Override
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }
    
    @Override
    public NotificationDTO createNotification(Long userId, String title, String message, String type) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;
        Notification n = new Notification();
        n.setTitle(title);
        n.setMessage(message);
        n.setType(NotificationType.valueOf(type));
        n.setRead(false);
        n.setUser(user);
        n.setCreatedAt(LocalDateTime.now());
        NotificationDTO saved = NotificationDTO.fromEntity(notificationRepository.save(n));
        webSocketConfig.broadcastUpdate("NOTIFICATION", saved);
        return saved;
    }
    
    @Override
    public void markAsRead(Long notificationId, Long userId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (!n.getUser().getId().equals(userId)) {
                throw new com.disaster.exception.ResourceNotFoundException("Notification not found");
            }
            n.setRead(true);
            notificationRepository.save(n);
        });
    }
    
    @Override
    public void markAllAsRead(Long userId) {
        notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId)
            .forEach(n -> {
                n.setRead(true);
                notificationRepository.save(n);
            });
    }
}
