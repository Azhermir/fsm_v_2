package com.fsm.taskmanagement.service;

import com.fsm.taskmanagement.dto.DashboardStatsResponse;
import com.fsm.taskmanagement.entity.Priority;
import com.fsm.taskmanagement.entity.Status;
import com.fsm.taskmanagement.repository.ServiceTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final ServiceTaskRepository repository;

    @Transactional(readOnly = true)
    public DashboardStatsResponse getStatistics(String dateRange) {
        log.info("Fetching dashboard statistics for date range: {}", dateRange);

        LocalDateTime startDate = calculateStartDate(dateRange);
        
        long totalTasks;
        long completedTasks;
        Map<Priority, Long> tasksByPriority = new HashMap<>();

        if (startDate != null) {
            // Date range filtering
            totalTasks = repository.countByCreatedAtAfter(startDate);
            completedTasks = repository.countByStatusAndCreatedAtAfter(Status.COMPLETED, startDate);
            
            for (Priority priority : Priority.values()) {
                long count = repository.countByPriorityAndCreatedAtAfter(priority, startDate);
                tasksByPriority.put(priority, count);
            }
        } else {
            // All tasks
            totalTasks = repository.count();
            completedTasks = repository.countByStatus(Status.COMPLETED);
            
            for (Priority priority : Priority.values()) {
                long count = repository.countByPriority(priority);
                tasksByPriority.put(priority, count);
            }
        }

        // Calculate open tasks (all non-completed tasks)
        long openTasks = totalTasks - completedTasks;
        
        // Calculate completion percentage
        double completionPercentage = totalTasks > 0 
            ? (completedTasks * 100.0) / totalTasks 
            : 0.0;

        log.info("Statistics: total={}, open={}, completed={}, completion={}%", 
                totalTasks, openTasks, completedTasks, String.format("%.2f", completionPercentage));

        return DashboardStatsResponse.builder()
                .totalTasks(totalTasks)
                .openTasks(openTasks)
                .completedTasks(completedTasks)
                .completionPercentage(Math.round(completionPercentage * 100.0) / 100.0)
                .tasksByPriority(tasksByPriority)
                .build();
    }

    private LocalDateTime calculateStartDate(String dateRange) {
        if (dateRange == null || dateRange.isEmpty()) {
            return null;
        }

        LocalDate today = LocalDate.now();
        
        switch (dateRange.toLowerCase()) {
            case "today":
                return LocalDateTime.of(today, LocalTime.MIN);
            case "this week":
                // Start of the week (Monday)
                LocalDate startOfWeek = today.with(java.time.DayOfWeek.MONDAY);
                return LocalDateTime.of(startOfWeek, LocalTime.MIN);
            default:
                log.warn("Unknown date range: {}, returning all tasks", dateRange);
                return null;
        }
    }
}
