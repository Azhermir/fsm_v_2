package com.fsm.task.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ServiceTask aggregate domain model representing a field service task
 * This is the root entity of the ServiceTask aggregate
 */
@Entity
@Table(name = "service_tasks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceTask {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Title of the service task - must not be blank (domain invariant)
     */
    @NotBlank(message = "ServiceTask must have a title")
    @Column(nullable = false)
    private String title;
    
    /**
     * Detailed description of the service task
     */
    @Column(length = 1000)
    private String description;
    
    /**
     * Physical address where the service task will be performed
     */
    @Column(nullable = false)
    private String clientAddress;
    
    /**
     * Priority level of the task - must be one of the defined enum values (domain invariant)
     */
    @NotNull(message = "Priority must be one of the defined enum values")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;
    
    /**
     * Estimated duration in minutes - must be positive (domain invariant)
     */
    @Positive(message = "EstimatedDuration must be positive")
    @Column(nullable = false)
    private Integer estimatedDuration;
    
    /**
     * Current status of the task
     */
    @NotNull(message = "TaskStatus must be defined")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;
    
    /**
     * Timestamp when the task was created
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Pre-persist callback to set createdAt timestamp
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    
    /**
     * Factory method to create a ServiceTask with required fields and domain invariant validation
     * 
     * @param title the task title (must not be blank)
     * @param description the task description
     * @param clientAddress the client's address
     * @param priority the task priority (must be valid enum)
     * @param estimatedDuration the estimated duration in minutes (must be positive)
     * @return a new ServiceTask instance
     * @throws IllegalArgumentException if domain invariants are violated
     */
    public static ServiceTask createServiceTask(
            String title, 
            String description, 
            String clientAddress, 
            Priority priority, 
            Integer estimatedDuration) {
        
        // Validate domain invariants
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("ServiceTask must have a title");
        }
        
        if (priority == null) {
            throw new IllegalArgumentException("Priority must be one of the defined enum values");
        }
        
        if (estimatedDuration == null || estimatedDuration <= 0) {
            throw new IllegalArgumentException("EstimatedDuration must be positive");
        }
        
        return ServiceTask.builder()
                .title(title)
                .description(description)
                .clientAddress(clientAddress)
                .priority(priority)
                .estimatedDuration(estimatedDuration)
                .status(TaskStatus.UNASSIGNED)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
