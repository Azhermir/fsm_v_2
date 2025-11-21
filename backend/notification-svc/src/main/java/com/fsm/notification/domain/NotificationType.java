package com.fsm.notification.domain;

/**
 * Enum representing the type of notification
 * Domain concept: Notification type classification
 */
public enum NotificationType {
    /**
     * Notification sent when a task is assigned to a technician
     */
    TASK_ASSIGNED,
    
    /**
     * Notification sent when a task status is updated
     */
    TASK_STATUS_UPDATE,
    
    /**
     * Notification sent when a task is completed
     */
    TASK_COMPLETED
}
