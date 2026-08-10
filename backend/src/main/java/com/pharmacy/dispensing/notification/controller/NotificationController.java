package com.pharmacy.dispensing.notification.controller;

import com.pharmacy.dispensing.notification.dto.NotificationDto;
import com.pharmacy.dispensing.notification.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<Page<NotificationDto>> getAllNotifications(Authentication authentication, Pageable pageable) {
        return ResponseEntity.ok(notificationService.getNotificationsForUser(authentication.getName(), pageable));
    }

    @GetMapping("/unread")
    public ResponseEntity<Page<NotificationDto>> getUnreadNotifications(Authentication authentication, Pageable pageable) {
        return ResponseEntity.ok(notificationService.getUnreadNotificationsForUser(authentication.getName(), pageable));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        return ResponseEntity.ok(notificationService.getUnreadCount(authentication.getName()));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, Authentication authentication) {
        notificationService.markAsRead(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        notificationService.markAllAsRead(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
