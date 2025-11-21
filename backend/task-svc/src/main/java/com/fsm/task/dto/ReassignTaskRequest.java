package com.fsm.task.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for reassigning a task to a different technician
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReassignTaskRequest {
    
    /**
     * ID of the new technician to assign the task to
     */
    @NotNull(message = "Technician ID must not be null")
    @Positive(message = "Technician ID must be positive")
    private Long technicianId;
    
    /**
     * Optional reason for reassignment (for audit trail)
     */
    private String reason;
    
    /**
     * User ID of the person who initiated the reassignment
     */
    private String reassignedBy;
}
