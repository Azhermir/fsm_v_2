package com.fsm.notification.service;

import com.fsm.notification.domain.Notification;
import com.fsm.notification.domain.NotificationChannel;
import com.fsm.notification.domain.NotificationType;

import java.util.List;

/**
 * Service interface for notification operations
 * Defines the contract for notification management and delivery
 */
public interface INotificationService {
    
    /**
     * Send a notification
     * Creates and sends a notification to the specified recipient
     * 
     * @param recipientId the ID of the recipient
     * @param type the type of notification
     * @param channel the delivery channel
     * @param message the notification message
     * @return the sent notification
     */
    Notification sendNotification(Long recipientId, NotificationType type, NotificationChannel channel, String message);
    
    /**
     * Get all notifications for a specific recipient
     * 
     * @param recipientId the recipient ID
     * @return list of notifications for the recipient
     */
    List<Notification> getNotificationsByRecipient(Long recipientId);
    
    /**
     * Get a notification by ID
     * 
     * @param id the notification ID
     * @return the notification
     */
    Notification getNotificationById(Long id);
    
    /**
     * Retry sending a failed notification
     * 
     * @param notificationId the ID of the notification to retry
     * @return the updated notification
     */
    Notification retryNotification(Long notificationId);
}
