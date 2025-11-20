package com.fsm.taskmanagement.service;

import com.fsm.taskmanagement.dto.DashboardStatsResponse;
import com.fsm.taskmanagement.entity.Priority;
import com.fsm.taskmanagement.entity.ServiceTask;
import com.fsm.taskmanagement.entity.Status;
import com.fsm.taskmanagement.repository.ServiceTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardService Tests")
class DashboardServiceTest {

    @Mock
    private ServiceTaskRepository repository;

    @InjectMocks
    private DashboardService service;

    @BeforeEach
    void setUp() {
        // Common setup can go here if needed
    }

    @Test
    @DisplayName("Should get statistics for all tasks when no date range specified")
    void shouldGetStatisticsForAllTasks() {
        // Arrange
        when(repository.count()).thenReturn(100L);
        when(repository.countByStatus(Status.COMPLETED)).thenReturn(75L);
        when(repository.countByPriority(Priority.CRITICAL)).thenReturn(10L);
        when(repository.countByPriority(Priority.HIGH)).thenReturn(20L);
        when(repository.countByPriority(Priority.MEDIUM)).thenReturn(40L);
        when(repository.countByPriority(Priority.LOW)).thenReturn(30L);

        // Act
        DashboardStatsResponse stats = service.getStatistics(null);

        // Assert
        assertNotNull(stats);
        assertEquals(100L, stats.getTotalTasks());
        assertEquals(25L, stats.getOpenTasks()); // 100 - 75
        assertEquals(75L, stats.getCompletedTasks());
        assertEquals(75.0, stats.getCompletionPercentage());
        
        Map<Priority, Long> tasksByPriority = stats.getTasksByPriority();
        assertEquals(10L, tasksByPriority.get(Priority.CRITICAL));
        assertEquals(20L, tasksByPriority.get(Priority.HIGH));
        assertEquals(40L, tasksByPriority.get(Priority.MEDIUM));
        assertEquals(30L, tasksByPriority.get(Priority.LOW));

        // Verify repository calls
        verify(repository).count();
        verify(repository).countByStatus(Status.COMPLETED);
        verify(repository, times(4)).countByPriority(any(Priority.class));
        verify(repository, never()).countByCreatedAtAfter(any());
    }

    @Test
    @DisplayName("Should get statistics with empty string date range")
    void shouldGetStatisticsWithEmptyDateRange() {
        // Arrange
        when(repository.count()).thenReturn(50L);
        when(repository.countByStatus(Status.COMPLETED)).thenReturn(20L);
        when(repository.countByPriority(any(Priority.class))).thenReturn(0L);

        // Act
        DashboardStatsResponse stats = service.getStatistics("");

        // Assert
        assertNotNull(stats);
        assertEquals(50L, stats.getTotalTasks());
        assertEquals(30L, stats.getOpenTasks());
        assertEquals(20L, stats.getCompletedTasks());

        verify(repository).count();
        verify(repository, never()).countByCreatedAtAfter(any());
    }

    @Test
    @DisplayName("Should get statistics for today date range")
    void shouldGetStatisticsForToday() {
        // Arrange
        when(repository.countByCreatedAtAfter(any(LocalDateTime.class))).thenReturn(25L);
        when(repository.countByStatusAndCreatedAtAfter(eq(Status.COMPLETED), any(LocalDateTime.class))).thenReturn(10L);
        when(repository.countByPriorityAndCreatedAtAfter(eq(Priority.CRITICAL), any(LocalDateTime.class))).thenReturn(5L);
        when(repository.countByPriorityAndCreatedAtAfter(eq(Priority.HIGH), any(LocalDateTime.class))).thenReturn(8L);
        when(repository.countByPriorityAndCreatedAtAfter(eq(Priority.MEDIUM), any(LocalDateTime.class))).thenReturn(7L);
        when(repository.countByPriorityAndCreatedAtAfter(eq(Priority.LOW), any(LocalDateTime.class))).thenReturn(5L);

        // Act
        DashboardStatsResponse stats = service.getStatistics("today");

        // Assert
        assertNotNull(stats);
        assertEquals(25L, stats.getTotalTasks());
        assertEquals(15L, stats.getOpenTasks()); // 25 - 10
        assertEquals(10L, stats.getCompletedTasks());
        assertEquals(40.0, stats.getCompletionPercentage());

        Map<Priority, Long> tasksByPriority = stats.getTasksByPriority();
        assertEquals(5L, tasksByPriority.get(Priority.CRITICAL));
        assertEquals(8L, tasksByPriority.get(Priority.HIGH));
        assertEquals(7L, tasksByPriority.get(Priority.MEDIUM));
        assertEquals(5L, tasksByPriority.get(Priority.LOW));

        // Verify repository calls with date filtering
        verify(repository).countByCreatedAtAfter(any(LocalDateTime.class));
        verify(repository).countByStatusAndCreatedAtAfter(eq(Status.COMPLETED), any(LocalDateTime.class));
        verify(repository, times(4)).countByPriorityAndCreatedAtAfter(any(Priority.class), any(LocalDateTime.class));
        verify(repository, never()).count();
    }

    @Test
    @DisplayName("Should get statistics for this week date range")
    void shouldGetStatisticsForThisWeek() {
        // Arrange
        when(repository.countByCreatedAtAfter(any(LocalDateTime.class))).thenReturn(60L);
        when(repository.countByStatusAndCreatedAtAfter(eq(Status.COMPLETED), any(LocalDateTime.class))).thenReturn(30L);
        when(repository.countByPriorityAndCreatedAtAfter(any(Priority.class), any(LocalDateTime.class))).thenReturn(15L);

        // Act
        DashboardStatsResponse stats = service.getStatistics("this week");

        // Assert
        assertNotNull(stats);
        assertEquals(60L, stats.getTotalTasks());
        assertEquals(30L, stats.getOpenTasks());
        assertEquals(30L, stats.getCompletedTasks());
        assertEquals(50.0, stats.getCompletionPercentage());

        verify(repository).countByCreatedAtAfter(any(LocalDateTime.class));
        verify(repository).countByStatusAndCreatedAtAfter(eq(Status.COMPLETED), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should handle case insensitive date range values")
    void shouldHandleCaseInsensitiveDateRange() {
        // Arrange
        when(repository.countByCreatedAtAfter(any(LocalDateTime.class))).thenReturn(10L);
        when(repository.countByStatusAndCreatedAtAfter(any(Status.class), any(LocalDateTime.class))).thenReturn(5L);
        when(repository.countByPriorityAndCreatedAtAfter(any(Priority.class), any(LocalDateTime.class))).thenReturn(2L);

        // Act - test with uppercase
        DashboardStatsResponse stats1 = service.getStatistics("TODAY");
        DashboardStatsResponse stats2 = service.getStatistics("THIS WEEK");
        DashboardStatsResponse stats3 = service.getStatistics("This Week");

        // Assert
        assertNotNull(stats1);
        assertNotNull(stats2);
        assertNotNull(stats3);
        
        // Verify date filtering was used
        verify(repository, times(3)).countByCreatedAtAfter(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should handle unknown date range by returning all tasks")
    void shouldHandleUnknownDateRange() {
        // Arrange
        when(repository.count()).thenReturn(100L);
        when(repository.countByStatus(Status.COMPLETED)).thenReturn(60L);
        when(repository.countByPriority(any(Priority.class))).thenReturn(25L);

        // Act
        DashboardStatsResponse stats = service.getStatistics("unknown");

        // Assert
        assertNotNull(stats);
        assertEquals(100L, stats.getTotalTasks());
        assertEquals(40L, stats.getOpenTasks());
        assertEquals(60L, stats.getCompletedTasks());

        // Verify it fell back to all tasks
        verify(repository).count();
        verify(repository, never()).countByCreatedAtAfter(any());
    }

    @Test
    @DisplayName("Should calculate zero completion percentage when no tasks exist")
    void shouldCalculateZeroCompletionPercentageWhenNoTasks() {
        // Arrange
        when(repository.count()).thenReturn(0L);
        when(repository.countByStatus(Status.COMPLETED)).thenReturn(0L);
        when(repository.countByPriority(any(Priority.class))).thenReturn(0L);

        // Act
        DashboardStatsResponse stats = service.getStatistics(null);

        // Assert
        assertNotNull(stats);
        assertEquals(0L, stats.getTotalTasks());
        assertEquals(0L, stats.getOpenTasks());
        assertEquals(0L, stats.getCompletedTasks());
        assertEquals(0.0, stats.getCompletionPercentage());
    }

    @Test
    @DisplayName("Should calculate 100 percent completion when all tasks are completed")
    void shouldCalculate100PercentCompletion() {
        // Arrange
        when(repository.count()).thenReturn(50L);
        when(repository.countByStatus(Status.COMPLETED)).thenReturn(50L);
        when(repository.countByPriority(any(Priority.class))).thenReturn(12L);

        // Act
        DashboardStatsResponse stats = service.getStatistics(null);

        // Assert
        assertNotNull(stats);
        assertEquals(50L, stats.getTotalTasks());
        assertEquals(0L, stats.getOpenTasks());
        assertEquals(50L, stats.getCompletedTasks());
        assertEquals(100.0, stats.getCompletionPercentage());
    }

    @Test
    @DisplayName("Should round completion percentage to two decimal places")
    void shouldRoundCompletionPercentage() {
        // Arrange
        when(repository.count()).thenReturn(3L);
        when(repository.countByStatus(Status.COMPLETED)).thenReturn(1L);
        when(repository.countByPriority(any(Priority.class))).thenReturn(0L);

        // Act
        DashboardStatsResponse stats = service.getStatistics(null);

        // Assert
        // 1/3 = 0.333... -> should be rounded to 33.33
        assertEquals(33.33, stats.getCompletionPercentage());
    }

    @Test
    @DisplayName("Should include all priority levels in response")
    void shouldIncludeAllPriorityLevels() {
        // Arrange
        when(repository.count()).thenReturn(100L);
        when(repository.countByStatus(Status.COMPLETED)).thenReturn(50L);
        when(repository.countByPriority(Priority.CRITICAL)).thenReturn(5L);
        when(repository.countByPriority(Priority.HIGH)).thenReturn(15L);
        when(repository.countByPriority(Priority.MEDIUM)).thenReturn(50L);
        when(repository.countByPriority(Priority.LOW)).thenReturn(30L);

        // Act
        DashboardStatsResponse stats = service.getStatistics(null);

        // Assert
        Map<Priority, Long> tasksByPriority = stats.getTasksByPriority();
        assertEquals(4, tasksByPriority.size());
        assertTrue(tasksByPriority.containsKey(Priority.CRITICAL));
        assertTrue(tasksByPriority.containsKey(Priority.HIGH));
        assertTrue(tasksByPriority.containsKey(Priority.MEDIUM));
        assertTrue(tasksByPriority.containsKey(Priority.LOW));
    }

    @Test
    @DisplayName("Should handle date range with mixed case and extra spaces")
    void shouldHandleDateRangeWithMixedCaseAndSpaces() {
        // Arrange
        when(repository.countByCreatedAtAfter(any(LocalDateTime.class))).thenReturn(15L);
        when(repository.countByStatusAndCreatedAtAfter(any(Status.class), any(LocalDateTime.class))).thenReturn(10L);
        when(repository.countByPriorityAndCreatedAtAfter(any(Priority.class), any(LocalDateTime.class))).thenReturn(3L);

        // Act
        DashboardStatsResponse stats = service.getStatistics("ToDay");

        // Assert
        assertNotNull(stats);
        assertEquals(15L, stats.getTotalTasks());
        verify(repository).countByCreatedAtAfter(any(LocalDateTime.class));
    }
}
