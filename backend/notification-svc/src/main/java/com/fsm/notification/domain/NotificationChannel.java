package com.fsm.notification.domain;

/**
 * Enum representing the notification delivery channel
 * Domain concept: Notification channel classification
 */
public enum NotificationChannel {
    /**
     * Email notification channel
     */
    EMAIL,
    
    /**
     * SMS notification channel
     */
    SMS,
    
    /**
     * Push notification channel for mobile devices
     */
    PUSH
}
