package com.fsm.user.domain;

/**
 * UserRole enum representing the role of a user in the system
 * Domain Concepts: Each user must have exactly one role
 */
public enum UserRole {
    /**
     * Dispatcher role - manages task assignments
     */
    DISPATCHER,
    
    /**
     * Technician role - performs field service tasks
     */
    TECHNICIAN,
    
    /**
     * Customer role - requests service
     */
    CUSTOMER,
    
    /**
     * Supervisor role - oversees operations
     */
    SUPERVISOR
}
