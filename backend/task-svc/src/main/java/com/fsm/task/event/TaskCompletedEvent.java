package com.fsm.task.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Domain event published when a task is completed
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCompletedEvent {
    
    private Long taskId;
    private String title;
    private String clientAddress;
    private String workSummary;
    private LocalDateTime completionTime;
}
