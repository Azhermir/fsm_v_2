package com.fsm.task.controller;

import com.fsm.task.domain.Priority;
import com.fsm.task.domain.TaskStatus;
import com.fsm.task.dto.ServiceTaskResponse;
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
 * Unit tests for TechnicianController
 */
@ExtendWith(MockitoExtension.class)
class TechnicianControllerTest {
    
    @Mock
    private TaskService taskService;
    
    @InjectMocks
    private TechnicianController technicianController;
    
    private Long technicianId;
    private ServiceTaskResponse taskResponse1;
    private ServiceTaskResponse taskResponse2;
    
    @BeforeEach
    void setUp() {
        technicianId = 100L;
        
        taskResponse1 = ServiceTaskResponse.builder()
                .id(1L)
                .title("Task 1")
                .description("Description 1")
                .clientAddress("123 Test St")
                .priority(Priority.HIGH)
                .estimatedDuration(60)
                .status(TaskStatus.ASSIGNED)
                .assignedTo(technicianId)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();
        
        taskResponse2 = ServiceTaskResponse.builder()
                .id(2L)
                .title("Task 2")
                .description("Description 2")
                .clientAddress("456 Test Ave")
                .priority(Priority.MEDIUM)
                .estimatedDuration(90)
                .status(TaskStatus.IN_PROGRESS)
                .assignedTo(technicianId)
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    @Test
    @DisplayName("GET /api/technicians/{id}/tasks - Should return all tasks for technician without status filter")
    void testGetTechnicianTasks_Success() {
        // Arrange
        List<ServiceTaskResponse> tasks = Arrays.asList(taskResponse1, taskResponse2);
        
        when(taskService.getTechnicianTasks(technicianId, null)).thenReturn(tasks);
        
        // Act
        ResponseEntity<List<ServiceTaskResponse>> result = technicianController.getTechnicianTasks(technicianId, null);
        
        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(2, result.getBody().size());
        assertEquals(1L, result.getBody().get(0).getId());
        assertEquals(2L, result.getBody().get(1).getId());
        
        verify(taskService, times(1)).getTechnicianTasks(technicianId, null);
    }
    
    @Test
    @DisplayName("GET /api/technicians/{id}/tasks - Should return tasks filtered by status")
    void testGetTechnicianTasks_WithStatusFilter() {
        // Arrange
        TaskStatus status = TaskStatus.ASSIGNED;
        List<ServiceTaskResponse> tasks = Collections.singletonList(taskResponse1);
        
        when(taskService.getTechnicianTasks(technicianId, status)).thenReturn(tasks);
        
        // Act
        ResponseEntity<List<ServiceTaskResponse>> result = technicianController.getTechnicianTasks(technicianId, status);
        
        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
        assertEquals(TaskStatus.ASSIGNED, result.getBody().get(0).getStatus());
        
        verify(taskService, times(1)).getTechnicianTasks(technicianId, status);
    }
    
    @Test
    @DisplayName("GET /api/technicians/{id}/tasks - Should return empty list when no tasks")
    void testGetTechnicianTasks_NoTasks() {
        // Arrange
        when(taskService.getTechnicianTasks(technicianId, null)).thenReturn(Collections.emptyList());
        
        // Act
        ResponseEntity<List<ServiceTaskResponse>> result = technicianController.getTechnicianTasks(technicianId, null);
        
        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isEmpty());
        
        verify(taskService, times(1)).getTechnicianTasks(technicianId, null);
    }
    
    @Test
    @DisplayName("GET /api/technicians/{id}/tasks - Should handle IllegalArgumentException")
    void testGetTechnicianTasks_InvalidTechnicianId() {
        // Arrange
        when(taskService.getTechnicianTasks(technicianId, null))
                .thenThrow(new IllegalArgumentException("Technician ID cannot be null"));
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            technicianController.getTechnicianTasks(technicianId, null);
        });
        
        assertTrue(exception.getMessage().contains("Technician ID cannot be null"));
        verify(taskService, times(1)).getTechnicianTasks(technicianId, null);
    }
    
    @Test
    @DisplayName("GET /api/technicians/{id}/tasks - Should filter by IN_PROGRESS status")
    void testGetTechnicianTasks_InProgressFilter() {
        // Arrange
        TaskStatus status = TaskStatus.IN_PROGRESS;
        List<ServiceTaskResponse> tasks = Collections.singletonList(taskResponse2);
        
        when(taskService.getTechnicianTasks(technicianId, status)).thenReturn(tasks);
        
        // Act
        ResponseEntity<List<ServiceTaskResponse>> result = technicianController.getTechnicianTasks(technicianId, status);
        
        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
        assertEquals(TaskStatus.IN_PROGRESS, result.getBody().get(0).getStatus());
        
        verify(taskService, times(1)).getTechnicianTasks(technicianId, status);
    }
    
    @Test
    @DisplayName("GET /api/technicians/{id}/tasks - Should filter by COMPLETED status")
    void testGetTechnicianTasks_CompletedFilter() {
        // Arrange
        TaskStatus status = TaskStatus.COMPLETED;
        
        ServiceTaskResponse completedTask = ServiceTaskResponse.builder()
                .id(3L)
                .title("Completed Task")
                .description("Completed Description")
                .clientAddress("789 Test Blvd")
                .priority(Priority.HIGH)
                .estimatedDuration(120)
                .status(TaskStatus.COMPLETED)
                .assignedTo(technicianId)
                .createdAt(LocalDateTime.now().minusDays(2))
                .build();
        
        List<ServiceTaskResponse> tasks = Collections.singletonList(completedTask);
        
        when(taskService.getTechnicianTasks(technicianId, status)).thenReturn(tasks);
        
        // Act
        ResponseEntity<List<ServiceTaskResponse>> result = technicianController.getTechnicianTasks(technicianId, status);
        
        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
        assertEquals(TaskStatus.COMPLETED, result.getBody().get(0).getStatus());
        
        verify(taskService, times(1)).getTechnicianTasks(technicianId, status);
    }
}
