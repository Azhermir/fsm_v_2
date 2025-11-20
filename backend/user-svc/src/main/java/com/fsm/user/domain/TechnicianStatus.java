package com.fsm.user.domain;

/**
 * TechnicianStatus enum representing the current status of a technician
 * Domain Concepts: Status must be one of the defined enum values
 */
public enum TechnicianStatus {
    /**
     * Technician is available for assignment
     */
    AVAILABLE,
    
    /**
     * Technician is currently assigned to a task
     */
    BUSY,
    
    /**
     * Technician is offline and not available
     */
    OFFLINE
}
