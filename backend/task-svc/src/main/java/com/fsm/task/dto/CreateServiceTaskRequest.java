package com.fsm.task.dto;

import com.fsm.task.domain.Priority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new ServiceTask
 * Represents the request body for POST /api/tasks
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new service task")
public class CreateServiceTaskRequest {
    
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    @Schema(description = "Title of the service task", example = "Fix HVAC System", required = true)
    private String title;
    
    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Schema(description = "Detailed description of the service task", 
            example = "Air conditioning not working properly in the main office", required = true)
    private String description;
    
    @NotBlank(message = "Client address is required")
    @Schema(description = "Physical address where the service task will be performed", 
            example = "123 Main St, Springfield, IL 62701", required = true)
    private String clientAddress;
    
    @Schema(description = "Latitude coordinate of the service location (geocoded from address)", 
            example = "39.7817", required = false)
    private Double latitude;
    
    @Schema(description = "Longitude coordinate of the service location (geocoded from address)", 
            example = "-89.6501", required = false)
    private Double longitude;
    
    @NotNull(message = "Priority is required")
    @Schema(description = "Priority level of the task", 
            example = "HIGH", required = true, 
            allowableValues = {"LOW", "MEDIUM", "HIGH", "CRITICAL"})
    private Priority priority;
    
    @NotNull(message = "Estimated duration is required")
    @Positive(message = "Estimated duration must be positive")
    @Schema(description = "Estimated duration in minutes", 
            example = "120", required = true, minimum = "1")
    private Integer estimatedDuration;
}
