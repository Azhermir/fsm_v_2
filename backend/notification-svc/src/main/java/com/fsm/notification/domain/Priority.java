package com.fsm.notification.domain;

/**
 * Priority levels for service tasks
 * This mirrors the Priority enum from task-svc for cross-service communication
 */
public enum Priority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
