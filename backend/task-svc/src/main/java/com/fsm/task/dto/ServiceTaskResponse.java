package com.fsm.task.dto;

import com.fsm.task.domain.Priority;
import com.fsm.task.domain.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for ServiceTask response
 * Represents the response body for task-related API operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Service task details")
public class ServiceTaskResponse {
    
    @Schema(description = "Unique identifier of the task", example = "1")
    private Long id;
    
    @Schema(description = "Title of the service task", example = "Fix HVAC System")
    private String title;
    
    @Schema(description = "Detailed description of the service task", 
            example = "Air conditioning not working properly in the main office")
    private String description;
    
    @Schema(description = "Physical address where the service task will be performed", 
            example = "123 Main St, Springfield, IL 62701")
    private String clientAddress;
    
    @Schema(description = "Latitude coordinate of the service location", example = "39.7817")
    private Double latitude;
    
    @Schema(description = "Longitude coordinate of the service location", example = "-89.6501")
    private Double longitude;
    
    @Schema(description = "Priority level of the task", 
            example = "HIGH", 
            allowableValues = {"LOW", "MEDIUM", "HIGH", "CRITICAL"})
    private Priority priority;
    
    @Schema(description = "Estimated duration in minutes", example = "120")
    private Integer estimatedDuration;
    
    @Schema(description = "Current status of the task", 
            example = "UNASSIGNED",
            allowableValues = {"UNASSIGNED", "ASSIGNED", "IN_PROGRESS", "COMPLETED"})
    private TaskStatus status;
    
    @Schema(description = "ID of the technician assigned to this task", example = "1")
    private Long assignedTo;
    
    @Schema(description = "ID of the user who created this task", example = "1")
    private Long createdBy;
    
    @Schema(description = "Timestamp when the task was created", 
            example = "2025-11-20T22:00:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Timestamp when the task status changed to IN_PROGRESS", 
            example = "2025-11-20T23:00:00")
    private LocalDateTime startedAt;
    
    @Schema(description = "Timestamp when the task status changed to COMPLETED", 
            example = "2025-11-21T01:00:00")
    private LocalDateTime completedAt;
    
    @Schema(description = "Summary of work completed", 
            example = "Replaced air filter and checked refrigerant levels")
    private String workSummary;
}
