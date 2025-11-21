package com.fsm.task.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
     * Title of the service task - must not be blank, max 200 characters (domain invariant)
     */
    @NotBlank(message = "ServiceTask must have a title")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    @Column(nullable = false, length = 200)
    private String title;
    
    /**
     * Detailed description of the service task - required, max 2000 characters (domain invariant)
     */
    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Column(nullable = false, length = 2000)
    private String description;
    
    /**
     * Physical address where the service task will be performed
     */
    @NotBlank(message = "Client address must not be blank")
    @Column(nullable = false)
    private String clientAddress;
    
    /**
     * Latitude coordinate of the service location (geocoded from address)
     */
    @Column
    private Double latitude;
    
    /**
     * Longitude coordinate of the service location (geocoded from address)
     */
    @Column
    private Double longitude;
    
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
     * ID of the technician to whom this task is assigned
     */
    @Column
    private Long assignedTo;
    
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
     * @param title the task title (must not be blank, max 200 characters)
     * @param description the task description (must not be blank, max 2000 characters)
     * @param clientAddress the client's address (must not be blank)
     * @param latitude the latitude coordinate (optional, geocoded from address)
     * @param longitude the longitude coordinate (optional, geocoded from address)
     * @param priority the task priority (must be valid enum)
     * @param estimatedDuration the estimated duration in minutes (must be positive)
     * @return a new ServiceTask instance
     * @throws IllegalArgumentException if domain invariants are violated
     */
    public static ServiceTask createServiceTask(
            String title, 
            String description, 
            String clientAddress,
            Double latitude,
            Double longitude,
            Priority priority, 
            Integer estimatedDuration) {
        
        // Validate domain invariants
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("ServiceTask must have a title");
        }
        
        if (title.length() > 200) {
            throw new IllegalArgumentException("Title must not exceed 200 characters");
        }
        
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description is required");
        }
        
        if (description.length() > 2000) {
            throw new IllegalArgumentException("Description must not exceed 2000 characters");
        }
        
        if (clientAddress == null || clientAddress.isBlank()) {
            throw new IllegalArgumentException("Client address must not be blank");
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
                .latitude(latitude)
                .longitude(longitude)
                .priority(priority)
                .estimatedDuration(estimatedDuration)
                .status(TaskStatus.UNASSIGNED)
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * Overloaded factory method for backward compatibility (without coordinates)
     */
    public static ServiceTask createServiceTask(
            String title, 
            String description, 
            String clientAddress, 
            Priority priority, 
            Integer estimatedDuration) {
        return createServiceTask(title, description, clientAddress, null, null, priority, estimatedDuration);
    }
}
