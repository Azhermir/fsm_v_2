package com.fsm.taskmanagement.dto;

import com.fsm.taskmanagement.entity.Priority;
import com.fsm.taskmanagement.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceTaskResponse {

    private UUID id;
    private String title;
    private String description;
    private String clientAddress;
    private Priority priority;
    private Integer estimatedDuration;
    private Double latitude;
    private Double longitude;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
