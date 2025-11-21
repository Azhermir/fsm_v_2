package com.fsm.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for assigning a task to a technician
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to assign a task to a technician")
public class AssignTaskRequest {
    
    /**
     * ID of the technician to assign the task to
     */
    @NotNull(message = "Technician ID is required")
    @Schema(description = "ID of the technician to assign the task to", example = "1", required = true)
    private Long technicianId;
    
    /**
     * User who is assigning the task (e.g., dispatcher username)
     */
    @Schema(description = "User who is assigning the task", example = "dispatcher1")
    private String assignedBy;
}
