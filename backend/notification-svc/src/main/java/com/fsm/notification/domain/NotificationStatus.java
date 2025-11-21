package com.fsm.notification.domain;

/**
 * Enum representing the status of a notification
 * Domain concept: Notification delivery status tracking
 */
public enum NotificationStatus {
    /**
     * Notification is pending and not yet sent
     */
    PENDING,
    
    /**
     * Notification has been successfully sent
     */
    SENT,
    
    /**
     * Notification sending failed
     */
    FAILED
}
