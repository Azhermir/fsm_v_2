package com.fsm.task.dto;

import com.fsm.task.domain.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating task status
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskStatusRequest {
    
    @NotNull(message = "Status must not be null")
    private TaskStatus status;
    
    private String workSummary;
}
