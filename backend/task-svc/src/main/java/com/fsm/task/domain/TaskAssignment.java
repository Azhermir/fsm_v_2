package com.fsm.task.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * TaskAssignment entity representing the assignment of a task to a technician
 * This is part of the Dispatch & Assignment bounded context
 */
@Entity
@Table(name = "task_assignments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskAssignment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * ID of the task being assigned
     */
    @NotNull(message = "Task ID must not be null")
    @Column(nullable = false)
    private Long taskId;
    
    /**
     * ID of the technician to whom the task is assigned
     */
    @NotNull(message = "Technician ID must not be null")
    @Column(nullable = false)
    private Long technicianId;
    
    /**
     * Timestamp when the task was assigned
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime assignedAt;
    
    /**
     * User ID of the person who assigned the task
     */
    @Column(nullable = false)
    private String assignedBy;
    
    /**
     * Optional reason for reassignment (for audit trail)
     */
    @Column(length = 500)
    private String reason;
    
    /**
     * Pre-persist callback to set assignedAt timestamp
     */
    @PrePersist
    protected void onCreate() {
        if (assignedAt == null) {
            assignedAt = LocalDateTime.now();
        }
    }
    
    /**
     * Factory method to create a TaskAssignment with domain invariant validation
     * 
     * @param taskId the task ID (must not be null)
     * @param technicianId the technician ID (must not be null)
     * @param assignedBy the user who assigned the task (must not be blank)
     * @return a new TaskAssignment instance
     * @throws IllegalArgumentException if domain invariants are violated
     */
    public static TaskAssignment createAssignment(
            Long taskId,
            Long technicianId,
            String assignedBy) {
        
        return createAssignment(taskId, technicianId, assignedBy, null);
    }
    
    /**
     * Factory method to create a TaskAssignment with reason for reassignment
     * 
     * @param taskId the task ID (must not be null)
     * @param technicianId the technician ID (must not be null)
     * @param assignedBy the user who assigned the task (must not be blank)
     * @param reason optional reason for reassignment (for audit trail)
     * @return a new TaskAssignment instance
     * @throws IllegalArgumentException if domain invariants are violated
     */
    public static TaskAssignment createAssignment(
            Long taskId,
            Long technicianId,
            String assignedBy,
            String reason) {
        
        // Validate domain invariants
        if (taskId == null) {
            throw new IllegalArgumentException("Task ID must not be null");
        }
        
        if (technicianId == null) {
            throw new IllegalArgumentException("Technician ID must not be null");
        }
        
        if (assignedBy == null || assignedBy.isBlank()) {
            throw new IllegalArgumentException("Assigned by must not be blank");
        }
        
        return TaskAssignment.builder()
                .taskId(taskId)
                .technicianId(technicianId)
                .assignedBy(assignedBy)
                .reason(reason)
                .assignedAt(LocalDateTime.now())
                .build();
    }
}
