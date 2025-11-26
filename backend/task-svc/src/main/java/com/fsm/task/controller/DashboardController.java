package com.fsm.task.controller;

import com.fsm.task.domain.Priority;
import com.fsm.task.domain.ServiceTask;
import com.fsm.task.domain.TaskStatus;
import com.fsm.task.dto.DashboardMetricsResponse;
import com.fsm.task.dto.ErrorResponse;
import com.fsm.task.repository.IServiceTaskRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for Dashboard Metrics
 * Provides endpoints for analytics and reporting
 */
@RestController
@RequestMapping("/api/dashboard")
@Slf4j
@Tag(name = "Dashboard", description = "Dashboard Metrics and Analytics API")
public class DashboardController {
    
    private final IServiceTaskRepository taskRepository;
    
    @Autowired
    public DashboardController(IServiceTaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
    
    /**
     * Get dashboard metrics with optional date range filtering
     * 
     * @param range Date range filter: "today", "this_week", or null for all time
     * @param startDate Custom start date (format: yyyy-MM-dd) - used when range is "custom"
     * @param endDate Custom end date (format: yyyy-MM-dd) - used when range is "custom"
     * @return Dashboard metrics response
     */
    @GetMapping("/metrics")
    @Operation(summary = "Get dashboard metrics", 
               description = "Retrieves real-time aggregated metrics for dashboard display. Supports date range filtering: today, this week, or custom date range.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", 
                     description = "Metrics retrieved successfully",
                     content = @Content(mediaType = "application/json",
                                      schema = @Schema(implementation = DashboardMetricsResponse.class))),
        @ApiResponse(responseCode = "400", 
                     description = "Invalid request parameters",
                     content = @Content(mediaType = "application/json",
                                      schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", 
                     description = "Internal server error",
                     content = @Content(mediaType = "application/json",
                                      schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DashboardMetricsResponse> getDashboardMetrics(
            @Parameter(description = "Date range filter: 'today', 'this_week', or omit for all time")
            @RequestParam(required = false) String range,
            @Parameter(description = "Custom start date (yyyy-MM-dd) - required when range='custom'")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "Custom end date (yyyy-MM-dd) - required when range='custom'")
            @RequestParam(required = false) String endDate) {
        
        log.info("GET /api/dashboard/metrics - range: {}, startDate: {}, endDate: {}", range, startDate, endDate);
        
        try {
            // Determine date range
            LocalDateTime start = null;
            LocalDateTime end = null;
            
            if (range != null) {
                switch (range.toLowerCase()) {
                    case "today":
                        start = LocalDate.now().atStartOfDay();
                        end = LocalDate.now().atTime(LocalTime.MAX);
                        break;
                    case "this_week":
                        start = LocalDate.now().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)).atStartOfDay();
                        end = LocalDate.now().with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY)).atTime(LocalTime.MAX);
                        break;
                    case "custom":
                        if (startDate == null || endDate == null) {
                            throw new IllegalArgumentException("Both startDate and endDate are required for custom range");
                        }
                        start = LocalDate.parse(startDate).atStartOfDay();
                        end = LocalDate.parse(endDate).atTime(LocalTime.MAX);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid range parameter. Use 'today', 'this_week', or 'custom'");
                }
            }
            
            // Fetch tasks based on date range
            List<ServiceTask> tasks;
            if (start != null && end != null) {
                tasks = taskRepository.findByCreatedAtBetween(start, end);
            } else {
                tasks = taskRepository.findAll();
            }
            
            // Calculate metrics
            DashboardMetricsResponse metrics = calculateMetrics(tasks);
            
            log.info("Dashboard metrics calculated successfully - Total tasks: {}, Completed: {}, Open: {}", 
                    metrics.getTotalTasks(), metrics.getCompletedTasks(), metrics.getOpenTasks());
            
            return ResponseEntity.ok(metrics);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving dashboard metrics", e);
            throw e;
        }
    }
    
    /**
     * Calculate dashboard metrics from a list of tasks
     * 
     * @param tasks List of tasks to calculate metrics from
     * @return Dashboard metrics response
     */
    private DashboardMetricsResponse calculateMetrics(List<ServiceTask> tasks) {
        // Total tasks
        long totalTasks = tasks.size();
        
        // Completed and open tasks
        long completedTasks = tasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.COMPLETED)
                .count();
        long openTasks = tasks.stream()
                .filter(task -> task.getStatus() != TaskStatus.COMPLETED)
                .count();
        
        // Tasks by priority
        Map<Priority, Long> tasksByPriority = new HashMap<>();
        for (Priority priority : Priority.values()) {
            long count = tasks.stream()
                    .filter(task -> task.getPriority() == priority)
                    .count();
            tasksByPriority.put(priority, count);
        }
        
        // Average completion time (using estimated duration as proxy since we don't have actual completion times)
        Double avgCompletionTime = null;
        List<ServiceTask> completedTasksList = tasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.COMPLETED)
                .collect(Collectors.toList());
        
        if (!completedTasksList.isEmpty()) {
            avgCompletionTime = completedTasksList.stream()
                    .mapToInt(ServiceTask::getEstimatedDuration)
                    .average()
                    .orElse(0.0);
        }
        
        // Technician workload - count of assigned tasks per technician
        Map<Long, Long> technicianWorkload = tasks.stream()
                .filter(task -> task.getAssignedTo() != null)
                .collect(Collectors.groupingBy(
                        ServiceTask::getAssignedTo,
                        Collectors.counting()
                ));
        
        return DashboardMetricsResponse.builder()
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .openTasks(openTasks)
                .tasksByPriority(tasksByPriority)
                .averageCompletionTimeMinutes(avgCompletionTime)
                .technicianWorkload(technicianWorkload)
                .build();
    }
    
    /**
     * Handle illegal argument exceptions (validation errors)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(ex.getMessage())
                .path("/api/dashboard/metrics")
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred")
                .path("/api/dashboard/metrics")
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
