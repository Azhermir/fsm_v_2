package com.fsm.notification.service;

import com.fsm.notification.domain.Notification;
import com.fsm.notification.domain.NotificationChannel;
import com.fsm.notification.domain.NotificationStatus;
import com.fsm.notification.domain.NotificationType;
import com.fsm.notification.provider.IPushNotificationProvider;
import com.fsm.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mock implementation of INotificationService
 * Logs notifications to console instead of sending them through actual providers
 * Integrates with push notification provider for PUSH channel notifications
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MockNotificationService implements INotificationService {
    
    private final NotificationRepository notificationRepository;
    private final IPushNotificationProvider pushNotificationProvider;
    private final DeviceTokenService deviceTokenService;
    
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
     * For PUSH notifications, uses the push notification provider
     * This is a mock implementation that will be replaced with actual email/SMS providers
     * 
     * @param notification the notification to log/send
     * @return true if logging/sending is successful, false otherwise
     */
    private boolean logNotificationToConsole(Notification notification) {
        try {
            // For PUSH notifications, use the push notification provider
            if (notification.getChannel() == NotificationChannel.PUSH) {
                return sendPushNotification(notification);
            }
            
            // For other channels (EMAIL, SMS), just log to console
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
    
    /**
     * Send push notification using push notification provider
     * 
     * @param notification the notification to send
     * @return true if successful, false otherwise
     */
    private boolean sendPushNotification(Notification notification) {
        try {
            // Get all active FCM tokens for the recipient
            List<String> fcmTokens = deviceTokenService.getActiveFcmTokens(notification.getRecipientId());
            
            if (fcmTokens.isEmpty()) {
                log.warn("No active device tokens found for user {}, skipping push notification", 
                         notification.getRecipientId());
                // Still return true as this is not an error - user just doesn't have devices registered
                return true;
            }
            
            // Extract title and body from message (first line as title, rest as body)
            String[] lines = notification.getMessage().split("\n", 2);
            String title = lines[0];
            String body = lines.length > 1 ? lines[1] : "";
            
            // Build data payload for deep linking
            Map<String, String> data = new HashMap<>();
            data.put("notificationId", notification.getId().toString());
            data.put("type", notification.getType().toString());
            
            // Extract task ID from message if it's a task notification
            if (notification.getType() == NotificationType.TASK_ASSIGNED || 
                notification.getType() == NotificationType.TASK_COMPLETED) {
                String taskId = extractTaskId(notification.getMessage());
                if (taskId != null) {
                    data.put("taskId", taskId);
                }
            }
            
            // Send push notification to all devices
            int successCount = pushNotificationProvider.sendPushNotificationToMultiple(
                    fcmTokens, title, body, data);
            
            log.info("Push notification sent to {} out of {} devices for recipient {}", 
                     successCount, fcmTokens.size(), notification.getRecipientId());
            
            // Consider it successful if at least one device received the notification
            return successCount > 0;
        } catch (Exception e) {
            log.error("Error logging notification to console", e);
            return false;
        }
    }
    
    /**
     * Extract task ID from notification message
     * Looks for "Task ID: {id}" pattern in the message
     * 
     * @param message the notification message
     * @return the task ID if found, null otherwise
     */
    private String extractTaskId(String message) {
        try {
            String pattern = "Task ID: ";
            int index = message.indexOf(pattern);
            if (index >= 0) {
                int start = index + pattern.length();
                int end = message.indexOf("\n", start);
                if (end < 0) {
                    end = message.length();
                }
                return message.substring(start, end).trim();
            }
        } catch (Exception e) {
            log.debug("Could not extract task ID from message", e);
        }
        return null;
    }
}
