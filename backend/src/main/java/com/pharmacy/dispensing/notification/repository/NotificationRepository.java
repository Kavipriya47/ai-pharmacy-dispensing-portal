package com.pharmacy.dispensing.notification.repository;

import com.pharmacy.dispensing.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByRecipientOrRecipientIsNullOrderByCreatedAtDesc(String recipient, Pageable pageable);

    Page<Notification> findByIsReadFalseAndRecipientOrRecipientIsNullOrderByCreatedAtDesc(String recipient, Pageable pageable);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.isRead = false AND (n.recipient = :recipient OR n.recipient IS NULL)")
    long countUnreadByRecipient(@Param("recipient") String recipient);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.isRead = false AND (n.recipient = :recipient OR n.recipient IS NULL)")
    int markAllAsReadForRecipient(@Param("recipient") String recipient);
}
