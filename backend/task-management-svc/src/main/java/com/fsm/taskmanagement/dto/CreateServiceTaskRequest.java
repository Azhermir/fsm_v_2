package com.fsm.taskmanagement.dto;

import com.fsm.taskmanagement.entity.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateServiceTaskRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Client address is required")
    private String clientAddress;

    @NotNull(message = "Priority is required")
    private Priority priority;

    private Integer estimatedDuration;
}
