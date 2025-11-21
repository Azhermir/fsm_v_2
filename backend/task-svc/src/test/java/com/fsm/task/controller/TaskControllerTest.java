package com.fsm.task.controller;

import com.fsm.task.domain.Priority;
import com.fsm.task.domain.TaskStatus;
import com.fsm.task.dto.AssignTaskRequest;
import com.fsm.task.dto.ServiceTaskResponse;
import com.fsm.task.dto.TaskAssignmentResponse;
import com.fsm.task.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaskController
 */
@ExtendWith(MockitoExtension.class)
class TaskControllerTest {
    
    @Mock
    private TaskService taskService;
    
    @InjectMocks
    private TaskController taskController;
    
    private ServiceTaskResponse mockTask1;
    private ServiceTaskResponse mockTask2;
    private ServiceTaskResponse mockTask3;
    
    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        
        mockTask1 = ServiceTaskResponse.builder()
                .id(1L)
                .title("Fix HVAC System")
                .description("Air conditioning not working")
                .clientAddress("123 Main St, Springfield")
                .priority(Priority.HIGH)
                .estimatedDuration(120)
                .status(TaskStatus.UNASSIGNED)
                .createdAt(now.minusDays(2))
                .build();
        
        mockTask2 = ServiceTaskResponse.builder()
                .id(2L)
                .title("Plumbing Inspection")
                .description("Routine inspection")
                .clientAddress("456 Oak Ave, Chicago")
                .priority(Priority.MEDIUM)
                .estimatedDuration(90)
                .status(TaskStatus.ASSIGNED)
                .createdAt(now.minusDays(1))
                .build();
        
        mockTask3 = ServiceTaskResponse.builder()
                .id(3L)
                .title("Emergency Electrical Repair")
                .description("Power outage")
                .clientAddress("789 Elm St, Boston")
                .priority(Priority.CRITICAL)
                .estimatedDuration(180)
                .status(TaskStatus.IN_PROGRESS)
                .createdAt(now)
                .build();
    }
    
    @Test
    @DisplayName("Should get all tasks successfully")
    void shouldGetAllTasksSuccessfully() {
        // Arrange
        List<ServiceTaskResponse> mockTasks = Arrays.asList(mockTask3, mockTask2, mockTask1);
        when(taskService.getAllTasks()).thenReturn(mockTasks);
        
        // Act
        ResponseEntity<List<ServiceTaskResponse>> response = taskController.getAllTasks(null);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());
        assertEquals("Emergency Electrical Repair", response.getBody().get(0).getTitle());
        assertEquals("Plumbing Inspection", response.getBody().get(1).getTitle());
        assertEquals("Fix HVAC System", response.getBody().get(2).getTitle());
        verify(taskService, times(1)).getAllTasks();
        verify(taskService, never()).getTasksByStatus(any());
    }
    
    @Test
    @DisplayName("Should return empty list when no tasks exist")
    void shouldReturnEmptyListWhenNoTasksExist() {
        // Arrange
        when(taskService.getAllTasks()).thenReturn(Collections.emptyList());
        
        // Act
        ResponseEntity<List<ServiceTaskResponse>> response = taskController.getAllTasks(null);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
        verify(taskService, times(1)).getAllTasks();
    }
    
    @Test
    @DisplayName("Should get tasks filtered by UNASSIGNED status")
    void shouldGetTasksFilteredByUnassignedStatus() {
        // Arrange
        List<ServiceTaskResponse> mockUnassignedTasks = Collections.singletonList(mockTask1);
        when(taskService.getTasksByStatus(TaskStatus.UNASSIGNED)).thenReturn(mockUnassignedTasks);
        
        // Act
        ResponseEntity<List<ServiceTaskResponse>> response = taskController.getAllTasks(TaskStatus.UNASSIGNED);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Fix HVAC System", response.getBody().get(0).getTitle());
        assertEquals(TaskStatus.UNASSIGNED, response.getBody().get(0).getStatus());
        verify(taskService, times(1)).getTasksByStatus(TaskStatus.UNASSIGNED);
        verify(taskService, never()).getAllTasks();
    }
    
    @Test
    @DisplayName("Should get tasks filtered by ASSIGNED status")
    void shouldGetTasksFilteredByAssignedStatus() {
        // Arrange
        List<ServiceTaskResponse> mockAssignedTasks = Collections.singletonList(mockTask2);
        when(taskService.getTasksByStatus(TaskStatus.ASSIGNED)).thenReturn(mockAssignedTasks);
        
        // Act
        ResponseEntity<List<ServiceTaskResponse>> response = taskController.getAllTasks(TaskStatus.ASSIGNED);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Plumbing Inspection", response.getBody().get(0).getTitle());
        assertEquals(TaskStatus.ASSIGNED, response.getBody().get(0).getStatus());
        verify(taskService, times(1)).getTasksByStatus(TaskStatus.ASSIGNED);
        verify(taskService, never()).getAllTasks();
    }
    
    @Test
    @DisplayName("Should get tasks filtered by IN_PROGRESS status")
    void shouldGetTasksFilteredByInProgressStatus() {
        // Arrange
        List<ServiceTaskResponse> mockInProgressTasks = Collections.singletonList(mockTask3);
        when(taskService.getTasksByStatus(TaskStatus.IN_PROGRESS)).thenReturn(mockInProgressTasks);
        
        // Act
        ResponseEntity<List<ServiceTaskResponse>> response = taskController.getAllTasks(TaskStatus.IN_PROGRESS);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Emergency Electrical Repair", response.getBody().get(0).getTitle());
        assertEquals(TaskStatus.IN_PROGRESS, response.getBody().get(0).getStatus());
        verify(taskService, times(1)).getTasksByStatus(TaskStatus.IN_PROGRESS);
        verify(taskService, never()).getAllTasks();
    }
    
    @Test
    @DisplayName("Should get tasks filtered by COMPLETED status")
    void shouldGetTasksFilteredByCompletedStatus() {
        // Arrange
        when(taskService.getTasksByStatus(TaskStatus.COMPLETED)).thenReturn(Collections.emptyList());
        
        // Act
        ResponseEntity<List<ServiceTaskResponse>> response = taskController.getAllTasks(TaskStatus.COMPLETED);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
        verify(taskService, times(1)).getTasksByStatus(TaskStatus.COMPLETED);
        verify(taskService, never()).getAllTasks();
    }
    
    @Test
    @DisplayName("Should return tasks in newest first order")
    void shouldReturnTasksInNewestFirstOrder() {
        // Arrange
        List<ServiceTaskResponse> mockTasks = Arrays.asList(mockTask3, mockTask2, mockTask1);
        when(taskService.getAllTasks()).thenReturn(mockTasks);
        
        // Act
        ResponseEntity<List<ServiceTaskResponse>> response = taskController.getAllTasks(null);
        
        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());
        // Verify order is newest first
        assertTrue(response.getBody().get(0).getCreatedAt().isAfter(response.getBody().get(1).getCreatedAt()));
        assertTrue(response.getBody().get(1).getCreatedAt().isAfter(response.getBody().get(2).getCreatedAt()));
    }
    
    @Test
    @DisplayName("Should handle service exception when getting all tasks")
    void shouldHandleServiceExceptionWhenGettingAllTasks() {
        // Arrange
        when(taskService.getAllTasks()).thenThrow(new RuntimeException("Database error"));
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> taskController.getAllTasks(null));
        verify(taskService, times(1)).getAllTasks();
    }
    
    @Test
    @DisplayName("Should handle service exception when getting tasks by status")
    void shouldHandleServiceExceptionWhenGettingTasksByStatus() {
        // Arrange
        when(taskService.getTasksByStatus(TaskStatus.UNASSIGNED))
                .thenThrow(new RuntimeException("Database error"));
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> taskController.getAllTasks(TaskStatus.UNASSIGNED));
        verify(taskService, times(1)).getTasksByStatus(TaskStatus.UNASSIGNED);
    }
    
    @Test
    @DisplayName("Should handle IllegalArgumentException when getting tasks by status")
    void shouldHandleIllegalArgumentExceptionWhenGettingTasksByStatus() {
        // Arrange
        when(taskService.getTasksByStatus(any()))
                .thenThrow(new IllegalArgumentException("Invalid status"));
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> taskController.getAllTasks(TaskStatus.UNASSIGNED));
        verify(taskService, times(1)).getTasksByStatus(TaskStatus.UNASSIGNED);
    }
    
    @Test
    @DisplayName("Should return OK status for all valid status filters")
    void shouldReturnOkStatusForAllValidStatusFilters() {
        // Test all TaskStatus enum values
        for (TaskStatus status : TaskStatus.values()) {
            // Arrange
            when(taskService.getTasksByStatus(status)).thenReturn(Collections.emptyList());
            
            // Act
            ResponseEntity<List<ServiceTaskResponse>> response = taskController.getAllTasks(status);
            
            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(taskService, times(1)).getTasksByStatus(status);
        }
    }
    
    @Test
    @DisplayName("Should verify correct service method is called based on status parameter")
    void shouldVerifyCorrectServiceMethodIsCalledBasedOnStatusParameter() {
        // Arrange
        when(taskService.getAllTasks()).thenReturn(Collections.emptyList());
        when(taskService.getTasksByStatus(any())).thenReturn(Collections.emptyList());
        
        // Act - call without status
        taskController.getAllTasks(null);
        
        // Assert
        verify(taskService, times(1)).getAllTasks();
        verify(taskService, never()).getTasksByStatus(any());
        
        // Reset mock
        reset(taskService);
        when(taskService.getTasksByStatus(any())).thenReturn(Collections.emptyList());
        
        // Act - call with status
        taskController.getAllTasks(TaskStatus.UNASSIGNED);
        
        // Assert
        verify(taskService, never()).getAllTasks();
        verify(taskService, times(1)).getTasksByStatus(TaskStatus.UNASSIGNED);
    }
    
    @Test
    @DisplayName("Should return all task fields correctly")
    void shouldReturnAllTaskFieldsCorrectly() {
        // Arrange
        List<ServiceTaskResponse> mockTasks = Collections.singletonList(mockTask1);
        when(taskService.getAllTasks()).thenReturn(mockTasks);
        
        // Act
        ResponseEntity<List<ServiceTaskResponse>> response = taskController.getAllTasks(null);
        
        // Assert
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        ServiceTaskResponse task = response.getBody().get(0);
        assertEquals(1L, task.getId());
        assertEquals("Fix HVAC System", task.getTitle());
        assertEquals("Air conditioning not working", task.getDescription());
        assertEquals("123 Main St, Springfield", task.getClientAddress());
        assertEquals(Priority.HIGH, task.getPriority());
        assertEquals(120, task.getEstimatedDuration());
        assertEquals(TaskStatus.UNASSIGNED, task.getStatus());
        assertNotNull(task.getCreatedAt());
    }
    
    // ========== Assignment Tests ==========
    
    @Test
    @DisplayName("Should assign task successfully")
    void shouldAssignTaskSuccessfully() {
        // Arrange
        Long taskId = 1L;
        Long technicianId = 10L;
        String assignedBy = "dispatcher1";
        LocalDateTime assignedAt = LocalDateTime.now();
        
        AssignTaskRequest request = AssignTaskRequest.builder()
                .technicianId(technicianId)
                .assignedBy(assignedBy)
                .build();
        
        TaskAssignmentResponse mockResponse = TaskAssignmentResponse.builder()
                .id(1L)
                .taskId(taskId)
                .technicianId(technicianId)
                .assignedBy(assignedBy)
                .assignedAt(assignedAt)
                .build();
        
        when(taskService.assignTask(taskId, request)).thenReturn(mockResponse);
        
        // Act
        ResponseEntity<TaskAssignmentResponse> response = taskController.assignTask(taskId, request);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals(taskId, response.getBody().getTaskId());
        assertEquals(technicianId, response.getBody().getTechnicianId());
        assertEquals(assignedBy, response.getBody().getAssignedBy());
        assertEquals(assignedAt, response.getBody().getAssignedAt());
        
        verify(taskService, times(1)).assignTask(taskId, request);
    }
    
    @Test
    @DisplayName("Should throw exception when assigning non-existent task")
    void shouldThrowExceptionWhenAssigningNonExistentTask() {
        // Arrange
        Long taskId = 999L;
        AssignTaskRequest request = AssignTaskRequest.builder()
                .technicianId(10L)
                .assignedBy("dispatcher1")
                .build();
        
        when(taskService.assignTask(taskId, request))
                .thenThrow(new IllegalArgumentException("Task not found with id: 999"));
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            taskController.assignTask(taskId, request);
        });
        
        verify(taskService, times(1)).assignTask(taskId, request);
    }
    
    @Test
    @DisplayName("Should throw exception when assigning task in invalid status")
    void shouldThrowExceptionWhenAssigningTaskInInvalidStatus() {
        // Arrange
        Long taskId = 1L;
        AssignTaskRequest request = AssignTaskRequest.builder()
                .technicianId(10L)
                .assignedBy("dispatcher1")
                .build();
        
        when(taskService.assignTask(taskId, request))
                .thenThrow(new IllegalArgumentException(
                        "Task can only be assigned when in UNASSIGNED or ASSIGNED status. Current status: IN_PROGRESS"));
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskController.assignTask(taskId, request);
        });
        
        assertTrue(exception.getMessage().contains("Task can only be assigned when in UNASSIGNED or ASSIGNED status"));
        verify(taskService, times(1)).assignTask(taskId, request);
    }
}
