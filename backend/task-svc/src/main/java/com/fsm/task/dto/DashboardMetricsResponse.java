package com.fsm.task.dto;

import com.fsm.task.domain.Priority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response DTO for dashboard metrics
 * Contains aggregated task statistics for dashboard display
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMetricsResponse {
    
    /**
     * Total number of tasks
     */
    private Long totalTasks;
    
    /**
     * Number of completed tasks
     */
    private Long completedTasks;
    
    /**
     * Number of open tasks (not completed)
     */
    private Long openTasks;
    
    /**
     * Task counts by priority level
     * Map of Priority to count
     */
    private Map<Priority, Long> tasksByPriority;
    
    /**
     * Average completion time in minutes
     * Calculated from completed tasks within the date range
     */
    private Double averageCompletionTimeMinutes;
    
    /**
     * Technician workload metrics
     * Map of technician ID to number of assigned tasks
     */
    private Map<Long, Long> technicianWorkload;
}
