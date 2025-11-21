package com.fsm.notification.repository;

import com.fsm.notification.domain.Notification;
import com.fsm.notification.domain.NotificationStatus;
import com.fsm.notification.domain.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Notification entity
 * Provides data access methods for notification management
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * Find all notifications for a specific recipient
     * 
     * @param recipientId the recipient ID
     * @return list of notifications for the recipient
     */
    List<Notification> findByRecipientId(Long recipientId);
    
    /**
     * Find all notifications by status
     * 
     * @param status the notification status
     * @return list of notifications with the given status
     */
    List<Notification> findByStatus(NotificationStatus status);
    
    /**
     * Find all notifications by type
     * 
     * @param type the notification type
     * @return list of notifications of the given type
     */
    List<Notification> findByType(NotificationType type);
    
    /**
     * Find all notifications for a recipient by status
     * 
     * @param recipientId the recipient ID
     * @param status the notification status
     * @return list of notifications for the recipient with the given status
     */
    List<Notification> findByRecipientIdAndStatus(Long recipientId, NotificationStatus status);
}
