package com.pharmacy.dispensing.notification.service;

import com.pharmacy.dispensing.notification.dto.NotificationDto;
import com.pharmacy.dispensing.notification.entity.Notification;
import com.pharmacy.dispensing.notification.entity.NotificationSeverity;
import com.pharmacy.dispensing.notification.entity.NotificationType;
import com.pharmacy.dispensing.notification.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void createNotification(NotificationType type, NotificationSeverity severity, String title, String message, String recipient, String relatedEntityType, String relatedEntityId) {
        Notification notification = new Notification(type, severity, title, message, recipient, relatedEntityType, relatedEntityId);
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public Page<NotificationDto> getNotificationsForUser(String username, Pageable pageable) {
        return notificationRepository.findByRecipientOrRecipientIsNullOrderByCreatedAtDesc(username, pageable)
                .map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<NotificationDto> getUnreadNotificationsForUser(String username, Pageable pageable) {
        return notificationRepository.findByIsReadFalseAndRecipientOrRecipientIsNullOrderByCreatedAtDesc(username, pageable)
                .map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String username) {
        return notificationRepository.countUnreadByRecipient(username);
    }

    @Transactional
    public void markAsRead(Long id, String username) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        
        // Ensure user can only read their own notifications or global ones
        if (notification.getRecipient() != null && !notification.getRecipient().equals(username)) {
            throw new IllegalArgumentException("Unauthorized to access this notification");
        }
        
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(String username) {
        notificationRepository.markAllAsReadForRecipient(username);
    }

    private NotificationDto mapToDto(Notification n) {
        return new NotificationDto(
                n.getId(), n.getType(), n.getSeverity(), n.getTitle(), n.getMessage(),
                n.isRead(), n.getRecipient(), n.getRelatedEntityType(), n.getRelatedEntityId(), n.getCreatedAt()
        );
    }
}
