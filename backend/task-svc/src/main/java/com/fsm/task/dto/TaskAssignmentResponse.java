package com.fsm.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for task assignment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing task assignment details")
public class TaskAssignmentResponse {
    
    @Schema(description = "Assignment ID", example = "1")
    private Long id;
    
    @Schema(description = "Task ID", example = "1")
    private Long taskId;
    
    @Schema(description = "Technician ID", example = "1")
    private Long technicianId;
    
    @Schema(description = "Timestamp when the task was assigned", example = "2025-11-21T03:30:00")
    private LocalDateTime assignedAt;
    
    @Schema(description = "User who assigned the task", example = "dispatcher1")
    private String assignedBy;
}
