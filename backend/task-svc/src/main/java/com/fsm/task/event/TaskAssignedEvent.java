package com.fsm.task.event;

import com.fsm.task.domain.Priority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Domain event published when a task is assigned to a technician
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskAssignedEvent {
    
    private Long taskId;
    private String title;
    private String description;
    private String clientAddress;
    private Double latitude;
    private Double longitude;
    private Priority priority;
    private Integer estimatedDuration;
    private Long technicianId;
    private Long customerId;
    private LocalDateTime assignedAt;
    private String assignedBy;
}
