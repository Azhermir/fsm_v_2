package com.fsm.notification.service;

import com.fsm.notification.domain.Notification;
import com.fsm.notification.domain.NotificationChannel;
import com.fsm.notification.domain.NotificationStatus;
import com.fsm.notification.domain.NotificationType;
import com.fsm.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Mock implementation of INotificationService
 * Logs notifications to console instead of sending them through actual providers
 * Ready for actual notification provider integration in future tasks (TASK-027, TASK-028)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MockNotificationService implements INotificationService {
    
    private final NotificationRepository notificationRepository;
    
    @Override
    @Transactional
    public Notification sendNotification(Long recipientId, NotificationType type, NotificationChannel channel, String message) {
        log.info("Preparing to send notification - RecipientId: {}, Type: {}, Channel: {}", 
                recipientId, type, channel);
        
        // Create notification entity
        Notification notification = Notification.createNotification(recipientId, type, channel, message);
        
        // Save notification with PENDING status
        notification = notificationRepository.save(notification);
        
        // Simulate sending the notification by logging to console
        boolean sendSuccess = logNotificationToConsole(notification);
        
        // Update notification status based on send result
        if (sendSuccess) {
            notification.markAsSent();
            log.info("Notification {} marked as SENT", notification.getId());
        } else {
            notification.markAsFailed();
            log.error("Notification {} marked as FAILED", notification.getId());
        }
        
        // Save updated status
        notification = notificationRepository.save(notification);
        
        log.info("Notification {} processed successfully with status: {}", 
                notification.getId(), notification.getStatus());
        
        return notification;
    }
    
    @Override
    public List<Notification> getNotificationsByRecipient(Long recipientId) {
        log.info("Retrieving notifications for recipient: {}", recipientId);
        return notificationRepository.findByRecipientId(recipientId);
    }
    
    @Override
    public Notification getNotificationById(Long id) {
        log.info("Retrieving notification with ID: {}", id);
        return notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found with ID: " + id));
    }
    
    @Override
    @Transactional
    public Notification retryNotification(Long notificationId) {
        log.info("Retrying notification with ID: {}", notificationId);
        
        Notification notification = getNotificationById(notificationId);
        
        if (notification.getStatus() == NotificationStatus.SENT) {
            throw new IllegalStateException("Cannot retry a notification that has already been sent");
        }
        
        // Simulate resending
        boolean sendSuccess = logNotificationToConsole(notification);
        
        if (sendSuccess) {
            notification.markAsSent();
            log.info("Retry successful - Notification {} marked as SENT", notification.getId());
        } else {
            notification.markAsFailed();
            log.error("Retry failed - Notification {} marked as FAILED", notification.getId());
        }
        
        return notificationRepository.save(notification);
    }
    
    /**
     * Simulates sending a notification by logging it to the console
     * This is a mock implementation that will be replaced with actual email/SMS/push providers
     * 
     * @param notification the notification to log
     * @return true if logging is successful, false otherwise
     */
    private boolean logNotificationToConsole(Notification notification) {
        try {
            log.info("=== NOTIFICATION MOCK SEND ===");
            log.info("Notification ID: {}", notification.getId());
            log.info("Recipient ID: {}", notification.getRecipientId());
            log.info("Type: {}", notification.getType());
            log.info("Channel: {}", notification.getChannel());
            log.info("Message: {}", notification.getMessage());
            log.info("Created At: {}", notification.getCreatedAt());
            log.info("==============================");
            
            // Simulate successful send
            return true;
        } catch (Exception e) {
            log.error("Error logging notification to console", e);
            return false;
        }
    }
}
