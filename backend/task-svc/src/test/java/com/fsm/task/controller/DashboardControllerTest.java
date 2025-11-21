package com.fsm.task.controller;

import com.fsm.task.domain.Priority;
import com.fsm.task.domain.ServiceTask;
import com.fsm.task.domain.TaskStatus;
import com.fsm.task.dto.DashboardMetricsResponse;
import com.fsm.task.repository.IServiceTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for DashboardController
 */
@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {
    
    @Mock
    private IServiceTaskRepository taskRepository;
    
    @InjectMocks
    private DashboardController dashboardController;
    
    private List<ServiceTask> mockTasks;
    
    @BeforeEach
    void setUp() {
        // Create mock tasks
        ServiceTask task1 = ServiceTask.builder()
                .id(1L)
                .title("Fix HVAC")
                .description("Air conditioning not working")
                .clientAddress("123 Main St")
                .priority(Priority.HIGH)
                .estimatedDuration(120)
                .status(TaskStatus.COMPLETED)
                .assignedTo(1L)
                .createdBy(1L)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();
        
        ServiceTask task2 = ServiceTask.builder()
                .id(2L)
                .title("Plumbing Inspection")
                .description("Routine plumbing inspection")
                .clientAddress("456 Oak Ave")
                .priority(Priority.MEDIUM)
                .estimatedDuration(90)
                .status(TaskStatus.IN_PROGRESS)
                .assignedTo(2L)
                .createdBy(1L)
                .createdAt(LocalDateTime.now().minusDays(2))
                .build();
        
        ServiceTask task3 = ServiceTask.builder()
                .id(3L)
                .title("Emergency Electrical")
                .description("Power outage")
                .clientAddress("789 Elm St")
                .priority(Priority.CRITICAL)
                .estimatedDuration(180)
                .status(TaskStatus.ASSIGNED)
                .assignedTo(1L)
                .createdBy(1L)
                .createdAt(LocalDateTime.now().minusHours(6))
                .build();
        
        ServiceTask task4 = ServiceTask.builder()
                .id(4L)
                .title("Routine Maintenance")
                .description("Regular maintenance check")
                .clientAddress("321 Pine Rd")
                .priority(Priority.LOW)
                .estimatedDuration(60)
                .status(TaskStatus.UNASSIGNED)
                .createdBy(1L)
                .createdAt(LocalDateTime.now())
                .build();
        
        ServiceTask task5 = ServiceTask.builder()
                .id(5L)
                .title("Completed Task")
                .description("Another completed task")
                .clientAddress("555 Maple St")
                .priority(Priority.HIGH)
                .estimatedDuration(150)
                .status(TaskStatus.COMPLETED)
                .assignedTo(2L)
                .createdBy(1L)
                .createdAt(LocalDateTime.now().minusDays(3))
                .build();
        
        mockTasks = Arrays.asList(task1, task2, task3, task4, task5);
    }
    
    @Test
    void getDashboardMetrics_AllTime_ReturnsCorrectMetrics() {
        // Arrange
        when(taskRepository.findAll()).thenReturn(mockTasks);
        
        // Act
        ResponseEntity<DashboardMetricsResponse> response = dashboardController.getDashboardMetrics(null, null, null);
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        DashboardMetricsResponse metrics = response.getBody();
        assertThat(metrics.getTotalTasks()).isEqualTo(5L);
        assertThat(metrics.getCompletedTasks()).isEqualTo(2L);
        assertThat(metrics.getOpenTasks()).isEqualTo(3L);
        
        // Verify priority counts
        assertThat(metrics.getTasksByPriority().get(Priority.HIGH)).isEqualTo(2L);
        assertThat(metrics.getTasksByPriority().get(Priority.MEDIUM)).isEqualTo(1L);
        assertThat(metrics.getTasksByPriority().get(Priority.CRITICAL)).isEqualTo(1L);
        assertThat(metrics.getTasksByPriority().get(Priority.LOW)).isEqualTo(1L);
        
        // Verify average completion time (average of 120 and 150)
        assertThat(metrics.getAverageCompletionTimeMinutes()).isEqualTo(135.0);
        
        // Verify technician workload
        assertThat(metrics.getTechnicianWorkload().get(1L)).isEqualTo(2L);
        assertThat(metrics.getTechnicianWorkload().get(2L)).isEqualTo(2L);
        assertThat(metrics.getTechnicianWorkload().containsKey(3L)).isFalse();
    }
    
    @Test
    void getDashboardMetrics_TodayRange_ReturnsFilteredMetrics() {
        // Arrange
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime endOfToday = LocalDate.now().atTime(LocalTime.MAX);
        
        List<ServiceTask> todayTasks = Arrays.asList(mockTasks.get(2), mockTasks.get(3)); // task3 and task4
        when(taskRepository.findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(todayTasks);
        
        // Act
        ResponseEntity<DashboardMetricsResponse> response = dashboardController.getDashboardMetrics("today", null, null);
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        DashboardMetricsResponse metrics = response.getBody();
        assertThat(metrics.getTotalTasks()).isEqualTo(2L);
        assertThat(metrics.getCompletedTasks()).isEqualTo(0L);
        assertThat(metrics.getOpenTasks()).isEqualTo(2L);
    }
    
    @Test
    void getDashboardMetrics_ThisWeekRange_ReturnsFilteredMetrics() {
        // Arrange
        List<ServiceTask> weekTasks = Arrays.asList(mockTasks.get(0), mockTasks.get(2), mockTasks.get(3)); // task1, task3, task4
        when(taskRepository.findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(weekTasks);
        
        // Act
        ResponseEntity<DashboardMetricsResponse> response = dashboardController.getDashboardMetrics("this_week", null, null);
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        DashboardMetricsResponse metrics = response.getBody();
        assertThat(metrics.getTotalTasks()).isEqualTo(3L);
        assertThat(metrics.getCompletedTasks()).isEqualTo(1L);
        assertThat(metrics.getOpenTasks()).isEqualTo(2L);
    }
    
    @Test
    void getDashboardMetrics_CustomRange_ReturnsFilteredMetrics() {
        // Arrange
        String startDate = "2025-11-18";
        String endDate = "2025-11-20";
        
        List<ServiceTask> customRangeTasks = Arrays.asList(mockTasks.get(0), mockTasks.get(1)); // task1, task2
        when(taskRepository.findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(customRangeTasks);
        
        // Act
        ResponseEntity<DashboardMetricsResponse> response = dashboardController.getDashboardMetrics("custom", startDate, endDate);
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        DashboardMetricsResponse metrics = response.getBody();
        assertThat(metrics.getTotalTasks()).isEqualTo(2L);
        assertThat(metrics.getCompletedTasks()).isEqualTo(1L);
        assertThat(metrics.getOpenTasks()).isEqualTo(1L);
    }
    
    @Test
    void getDashboardMetrics_CustomRangeWithoutStartDate_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> dashboardController.getDashboardMetrics("custom", null, "2025-11-20"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Both startDate and endDate are required for custom range");
    }
    
    @Test
    void getDashboardMetrics_CustomRangeWithoutEndDate_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> dashboardController.getDashboardMetrics("custom", "2025-11-18", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Both startDate and endDate are required for custom range");
    }
    
    @Test
    void getDashboardMetrics_InvalidRange_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> dashboardController.getDashboardMetrics("invalid_range", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid range parameter");
    }
    
    @Test
    void getDashboardMetrics_NoCompletedTasks_ReturnsNullAverageCompletionTime() {
        // Arrange
        List<ServiceTask> openTasks = Arrays.asList(mockTasks.get(1), mockTasks.get(2), mockTasks.get(3)); // no completed tasks
        when(taskRepository.findAll()).thenReturn(openTasks);
        
        // Act
        ResponseEntity<DashboardMetricsResponse> response = dashboardController.getDashboardMetrics(null, null, null);
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        DashboardMetricsResponse metrics = response.getBody();
        assertThat(metrics.getAverageCompletionTimeMinutes()).isNull();
    }
    
    @Test
    void getDashboardMetrics_NoTasks_ReturnsZeroMetrics() {
        // Arrange
        when(taskRepository.findAll()).thenReturn(Arrays.asList());
        
        // Act
        ResponseEntity<DashboardMetricsResponse> response = dashboardController.getDashboardMetrics(null, null, null);
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        DashboardMetricsResponse metrics = response.getBody();
        assertThat(metrics.getTotalTasks()).isEqualTo(0L);
        assertThat(metrics.getCompletedTasks()).isEqualTo(0L);
        assertThat(metrics.getOpenTasks()).isEqualTo(0L);
        assertThat(metrics.getAverageCompletionTimeMinutes()).isNull();
        assertThat(metrics.getTechnicianWorkload()).isEmpty();
    }
    
    @Test
    void getDashboardMetrics_TasksWithoutAssignedTechnician_ExcludesFromWorkload() {
        // Arrange
        when(taskRepository.findAll()).thenReturn(mockTasks);
        
        // Act
        ResponseEntity<DashboardMetricsResponse> response = dashboardController.getDashboardMetrics(null, null, null);
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        DashboardMetricsResponse metrics = response.getBody();
        // task4 is unassigned, so should not appear in workload
        assertThat(metrics.getTechnicianWorkload().size()).isEqualTo(2); // Only technicians 1 and 2
    }
    
    @Test
    void getDashboardMetrics_AllPriorities_ReturnsCorrectCounts() {
        // Arrange
        when(taskRepository.findAll()).thenReturn(mockTasks);
        
        // Act
        ResponseEntity<DashboardMetricsResponse> response = dashboardController.getDashboardMetrics(null, null, null);
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        DashboardMetricsResponse metrics = response.getBody();
        assertThat(metrics.getTasksByPriority()).hasSize(4);
        assertThat(metrics.getTasksByPriority()).containsKeys(Priority.LOW, Priority.MEDIUM, Priority.HIGH, Priority.CRITICAL);
    }
    
    @Test
    void handleIllegalArgumentException_ReturnsErrorResponse() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Test error message");
        
        // Act
        ResponseEntity<com.fsm.task.dto.ErrorResponse> response = dashboardController.handleIllegalArgumentException(exception);
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Test error message");
        assertThat(response.getBody().getStatus()).isEqualTo(400);
    }
    
    @Test
    void handleGenericException_ReturnsInternalServerError() {
        // Arrange
        Exception exception = new RuntimeException("Unexpected error");
        
        // Act
        ResponseEntity<com.fsm.task.dto.ErrorResponse> response = dashboardController.handleGenericException(exception);
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
        assertThat(response.getBody().getStatus()).isEqualTo(500);
    }
}
