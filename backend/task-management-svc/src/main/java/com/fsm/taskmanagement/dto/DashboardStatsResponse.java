package com.fsm.taskmanagement.dto;

import com.fsm.taskmanagement.entity.Priority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    
    private Long totalTasks;
    private Long openTasks;
    private Long completedTasks;
    private Double completionPercentage;
    private Map<Priority, Long> tasksByPriority;
}
