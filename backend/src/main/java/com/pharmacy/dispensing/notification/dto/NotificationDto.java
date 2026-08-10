package com.pharmacy.dispensing.notification.dto;

import com.pharmacy.dispensing.notification.entity.NotificationSeverity;
import com.pharmacy.dispensing.notification.entity.NotificationType;

import java.time.LocalDateTime;

public class NotificationDto {
    private Long id;
    private NotificationType type;
    private NotificationSeverity severity;
    private String title;
    private String message;
    private boolean isRead;
    private String recipient;
    private String relatedEntityType;
    private String relatedEntityId;
    private LocalDateTime createdAt;

    public NotificationDto(Long id, NotificationType type, NotificationSeverity severity, String title, String message, boolean isRead, String recipient, String relatedEntityType, String relatedEntityId, LocalDateTime createdAt) {
        this.id = id;
        this.type = type;
        this.severity = severity;
        this.title = title;
        this.message = message;
        this.isRead = isRead;
        this.recipient = recipient;
        this.relatedEntityType = relatedEntityType;
        this.relatedEntityId = relatedEntityId;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public NotificationType getType() { return type; }
    public NotificationSeverity getSeverity() { return severity; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public boolean isRead() { return isRead; }
    public String getRecipient() { return recipient; }
    public String getRelatedEntityType() { return relatedEntityType; }
    public String getRelatedEntityId() { return relatedEntityId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
