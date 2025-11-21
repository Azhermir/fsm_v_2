package com.fsm.task.service;

import com.fsm.task.domain.Priority;
import com.fsm.task.domain.ServiceTask;
import com.fsm.task.domain.TaskAssignment;
import com.fsm.task.domain.TaskStatus;
import com.fsm.task.dto.AssignTaskRequest;
import com.fsm.task.dto.CreateServiceTaskRequest;
import com.fsm.task.dto.ServiceTaskResponse;
import com.fsm.task.dto.TaskAssignmentResponse;
import com.fsm.task.dto.UpdateTaskStatusRequest;
import com.fsm.task.event.TaskCompletedEvent;
import com.fsm.task.repository.IServiceTaskRepository;
import com.fsm.task.repository.TaskAssignmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaskService
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    
    @Mock
    private IServiceTaskRepository taskRepository;
    
    @Mock
    private TaskAssignmentRepository assignmentRepository;
    
    @Mock
    private ApplicationEventPublisher eventPublisher;
    
    @InjectMocks
    private TaskService taskService;
    
    private CreateServiceTaskRequest validRequest;
    private ServiceTask mockSavedTask;
    
    @BeforeEach
    void setUp() {
        validRequest = CreateServiceTaskRequest.builder()
                .title("Fix HVAC System")
                .description("Air conditioning not working")
                .clientAddress("123 Main St, Springfield")
                .priority(Priority.HIGH)
                .estimatedDuration(120)
                .createdBy(1L)
                .build();
        
        mockSavedTask = ServiceTask.builder()
                .id(1L)
                .title("Fix HVAC System")
                .description("Air conditioning not working")
                .clientAddress("123 Main St, Springfield")
                .priority(Priority.HIGH)
                .estimatedDuration(120)
                .status(TaskStatus.UNASSIGNED)
                .createdBy(1L)
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    @Test
    @DisplayName("Should create task successfully")
    void shouldCreateTaskSuccessfully() {
        // Arrange
        when(taskRepository.save(any(ServiceTask.class))).thenReturn(mockSavedTask);
        
        // Act
        ServiceTaskResponse response = taskService.createTask(validRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Fix HVAC System", response.getTitle());
        assertEquals("Air conditioning not working", response.getDescription());
        assertEquals("123 Main St, Springfield", response.getClientAddress());
        assertEquals(Priority.HIGH, response.getPriority());
        assertEquals(120, response.getEstimatedDuration());
        assertEquals(TaskStatus.UNASSIGNED, response.getStatus());
        assertNotNull(response.getCreatedAt());
        
        verify(taskRepository, times(1)).save(any(ServiceTask.class));
    }
    
    @Test
    @DisplayName("Should create task with correct status")
    void shouldCreateTaskWithCorrectStatus() {
        // Arrange
        when(taskRepository.save(any(ServiceTask.class))).thenReturn(mockSavedTask);
        
        // Act
        ServiceTaskResponse response = taskService.createTask(validRequest);
        
        // Assert
        assertEquals(TaskStatus.UNASSIGNED, response.getStatus());
    }
    
    @Test
    @DisplayName("Should invoke repository save method")
    void shouldInvokeRepositorySaveMethod() {
        // Arrange
        when(taskRepository.save(any(ServiceTask.class))).thenReturn(mockSavedTask);
        
        // Act
        taskService.createTask(validRequest);
        
        // Assert
        ArgumentCaptor<ServiceTask> taskCaptor = ArgumentCaptor.forClass(ServiceTask.class);
        verify(taskRepository).save(taskCaptor.capture());
        
        ServiceTask capturedTask = taskCaptor.getValue();
        assertEquals("Fix HVAC System", capturedTask.getTitle());
        assertEquals("Air conditioning not working", capturedTask.getDescription());
        assertEquals("123 Main St, Springfield", capturedTask.getClientAddress());
        assertEquals(Priority.HIGH, capturedTask.getPriority());
        assertEquals(120, capturedTask.getEstimatedDuration());
        assertEquals(TaskStatus.UNASSIGNED, capturedTask.getStatus());
    }
    
    @Test
    @DisplayName("Should reject task with null description")
    void shouldRejectTaskWithNullDescription() {
        // Arrange
        validRequest.setDescription(null);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> taskService.createTask(validRequest));
    }
    
    @Test
    @DisplayName("Should reject task with blank description")
    void shouldRejectTaskWithBlankDescription() {
        // Arrange
        validRequest.setDescription("   ");
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> taskService.createTask(validRequest));
    }
    
    @Test
    @DisplayName("Should reject task with description exceeding max length")
    void shouldRejectTaskWithDescriptionExceedingMaxLength() {
        // Arrange
        String longDescription = "a".repeat(2001);
        validRequest.setDescription(longDescription);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> taskService.createTask(validRequest));
    }
    
    @Test
    @DisplayName("Should reject task with title exceeding max length")
    void shouldRejectTaskWithTitleExceedingMaxLength() {
        // Arrange
        String longTitle = "a".repeat(201);
        validRequest.setTitle(longTitle);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> taskService.createTask(validRequest));
    }
    
    @Test
    @DisplayName("Should create tasks with all priority levels")
    void shouldCreateTasksWithAllPriorityLevels() {
        for (Priority priority : Priority.values()) {
            // Arrange
            validRequest.setPriority(priority);
            ServiceTask taskWithPriority = ServiceTask.builder()
                    .id(1L)
                    .title("Task")
                    .description("Description")
                    .clientAddress("Address")
                    .priority(priority)
                    .estimatedDuration(60)
                    .status(TaskStatus.UNASSIGNED)
                    .createdBy(1L)
                    .createdAt(LocalDateTime.now())
                    .build();
            when(taskRepository.save(any(ServiceTask.class))).thenReturn(taskWithPriority);
            
            // Act
            ServiceTaskResponse response = taskService.createTask(validRequest);
            
            // Assert
            assertEquals(priority, response.getPriority());
        }
    }
    
    @Test
    @DisplayName("Should throw exception when title is null")
    void shouldThrowExceptionWhenTitleIsNull() {
        // Arrange
        validRequest.setTitle(null);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
                () -> taskService.createTask(validRequest));
        verify(taskRepository, never()).save(any(ServiceTask.class));
    }
    
    @Test
    @DisplayName("Should throw exception when title is blank")
    void shouldThrowExceptionWhenTitleIsBlank() {
        // Arrange
        validRequest.setTitle("   ");
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
                () -> taskService.createTask(validRequest));
        verify(taskRepository, never()).save(any(ServiceTask.class));
    }
    
    @Test
    @DisplayName("Should throw exception when client address is null")
    void shouldThrowExceptionWhenClientAddressIsNull() {
        // Arrange
        validRequest.setClientAddress(null);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
                () -> taskService.createTask(validRequest));
        verify(taskRepository, never()).save(any(ServiceTask.class));
    }
    
    @Test
    @DisplayName("Should throw exception when priority is null")
    void shouldThrowExceptionWhenPriorityIsNull() {
        // Arrange
        validRequest.setPriority(null);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
                () -> taskService.createTask(validRequest));
        verify(taskRepository, never()).save(any(ServiceTask.class));
    }
    
    @Test
    @DisplayName("Should throw exception when estimated duration is null")
    void shouldThrowExceptionWhenEstimatedDurationIsNull() {
        // Arrange
        validRequest.setEstimatedDuration(null);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
                () -> taskService.createTask(validRequest));
        verify(taskRepository, never()).save(any(ServiceTask.class));
    }
    
    @Test
    @DisplayName("Should throw exception when estimated duration is zero")
    void shouldThrowExceptionWhenEstimatedDurationIsZero() {
        // Arrange
        validRequest.setEstimatedDuration(0);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
                () -> taskService.createTask(validRequest));
        verify(taskRepository, never()).save(any(ServiceTask.class));
    }
    
    @Test
    @DisplayName("Should throw exception when estimated duration is negative")
    void shouldThrowExceptionWhenEstimatedDurationIsNegative() {
        // Arrange
        validRequest.setEstimatedDuration(-10);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
                () -> taskService.createTask(validRequest));
        verify(taskRepository, never()).save(any(ServiceTask.class));
    }
    
    @Test
    @DisplayName("Should get all tasks successfully")
    void shouldGetAllTasksSuccessfully() {
        // Arrange
        ServiceTask task1 = ServiceTask.builder()
                .id(1L)
                .title("Task 1")
                .description("Description 1")
                .clientAddress("Address 1")
                .priority(Priority.HIGH)
                .estimatedDuration(120)
                .status(TaskStatus.UNASSIGNED)
                .createdBy(1L)
                .createdAt(LocalDateTime.now().minusDays(2))
                .build();
        
        ServiceTask task2 = ServiceTask.builder()
                .id(2L)
                .title("Task 2")
                .description("Description 2")
                .clientAddress("Address 2")
                .priority(Priority.MEDIUM)
                .estimatedDuration(90)
                .status(TaskStatus.ASSIGNED)
                .createdBy(1L)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();
        
        when(taskRepository.findAllOrderByCreatedAtDesc()).thenReturn(Arrays.asList(task2, task1));
        
        // Act
        List<ServiceTaskResponse> responses = taskService.getAllTasks();
        
        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("Task 2", responses.get(0).getTitle());
        assertEquals("Task 1", responses.get(1).getTitle());
        verify(taskRepository, times(1)).findAllOrderByCreatedAtDesc();
    }
    
    @Test
    @DisplayName("Should return empty list when no tasks exist")
    void shouldReturnEmptyListWhenNoTasksExist() {
        // Arrange
        when(taskRepository.findAllOrderByCreatedAtDesc()).thenReturn(Collections.emptyList());
        
        // Act
        List<ServiceTaskResponse> responses = taskService.getAllTasks();
        
        // Assert
        assertNotNull(responses);
        assertEquals(0, responses.size());
        verify(taskRepository, times(1)).findAllOrderByCreatedAtDesc();
    }
    
    @Test
    @DisplayName("Should get tasks by status successfully")
    void shouldGetTasksByStatusSuccessfully() {
        // Arrange
        ServiceTask task1 = ServiceTask.builder()
                .id(1L)
                .title("Unassigned Task 1")
                .description("Description 1")
                .clientAddress("Address 1")
                .priority(Priority.HIGH)
                .estimatedDuration(120)
                .status(TaskStatus.UNASSIGNED)
                .createdBy(1L)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();
        
        ServiceTask task2 = ServiceTask.builder()
                .id(2L)
                .title("Unassigned Task 2")
                .description("Description 2")
                .clientAddress("Address 2")
                .priority(Priority.MEDIUM)
                .estimatedDuration(90)
                .status(TaskStatus.UNASSIGNED)
                .createdBy(1L)
                .createdAt(LocalDateTime.now())
                .build();
        
        when(taskRepository.findByStatusOrderByCreatedAtDesc(TaskStatus.UNASSIGNED))
                .thenReturn(Arrays.asList(task2, task1));
        
        // Act
        List<ServiceTaskResponse> responses = taskService.getTasksByStatus(TaskStatus.UNASSIGNED);
        
        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("Unassigned Task 2", responses.get(0).getTitle());
        assertEquals("Unassigned Task 1", responses.get(1).getTitle());
        responses.forEach(r -> assertEquals(TaskStatus.UNASSIGNED, r.getStatus()));
        verify(taskRepository, times(1)).findByStatusOrderByCreatedAtDesc(TaskStatus.UNASSIGNED);
    }
    
    @Test
    @DisplayName("Should return empty list when no tasks with status exist")
    void shouldReturnEmptyListWhenNoTasksWithStatusExist() {
        // Arrange
        when(taskRepository.findByStatusOrderByCreatedAtDesc(TaskStatus.COMPLETED))
                .thenReturn(Collections.emptyList());
        
        // Act
        List<ServiceTaskResponse> responses = taskService.getTasksByStatus(TaskStatus.COMPLETED);
        
        // Assert
        assertNotNull(responses);
        assertEquals(0, responses.size());
        verify(taskRepository, times(1)).findByStatusOrderByCreatedAtDesc(TaskStatus.COMPLETED);
    }
    
    @Test
    @DisplayName("Should get tasks for all status types")
    void shouldGetTasksForAllStatusTypes() {
        // Test all TaskStatus enum values
        for (TaskStatus status : TaskStatus.values()) {
            // Arrange
            ServiceTask task = ServiceTask.builder()
                    .id(1L)
                    .title("Task")
                    .description("Description")
                    .clientAddress("Address")
                    .priority(Priority.MEDIUM)
                    .estimatedDuration(60)
                    .status(status)
                    .createdBy(1L)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            when(taskRepository.findByStatusOrderByCreatedAtDesc(status))
                    .thenReturn(Collections.singletonList(task));
            
            // Act
            List<ServiceTaskResponse> responses = taskService.getTasksByStatus(status);
            
            // Assert
            assertNotNull(responses);
            assertEquals(1, responses.size());
            assertEquals(status, responses.get(0).getStatus());
        }
    }
    
    @Test
    @DisplayName("Should update task status successfully")
    void shouldUpdateTaskStatusSuccessfully() {
        // Arrange
        ServiceTask existingTask = ServiceTask.builder()
                .id(1L)
                .title("Fix HVAC System")
                .description("Air conditioning not working")
                .clientAddress("123 Main St, Springfield")
                .priority(Priority.HIGH)
                .estimatedDuration(120)
                .status(TaskStatus.IN_PROGRESS)
                .createdBy(1L)
                .createdAt(LocalDateTime.now())
                .build();
        
        ServiceTask updatedTask = ServiceTask.builder()
                .id(1L)
                .title("Fix HVAC System")
                .description("Air conditioning not working")
                .clientAddress("123 Main St, Springfield")
                .priority(Priority.HIGH)
                .estimatedDuration(120)
                .status(TaskStatus.COMPLETED)
                .createdBy(1L)
                .createdAt(LocalDateTime.now())
                .build();
        
        UpdateTaskStatusRequest request = UpdateTaskStatusRequest.builder()
                .status(TaskStatus.COMPLETED)
                .workSummary("Fixed the air conditioning unit")
                .build();
        
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(ServiceTask.class))).thenReturn(updatedTask);
        
        // Act
        ServiceTaskResponse response = taskService.updateTaskStatus(1L, request);
        
        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(TaskStatus.COMPLETED, response.getStatus());
        
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(any(ServiceTask.class));
    }
    
    @Test
    @DisplayName("Should publish TaskCompletedEvent when status changes to COMPLETED")
    void shouldPublishTaskCompletedEventWhenStatusChangesToCompleted() {
        // Arrange
        ServiceTask existingTask = ServiceTask.builder()
                .id(1L)
                .title("Fix HVAC System")
                .description("Air conditioning not working")
                .clientAddress("123 Main St, Springfield")
                .priority(Priority.HIGH)
                .estimatedDuration(120)
                .status(TaskStatus.IN_PROGRESS)
                .createdBy(1L)
                .createdAt(LocalDateTime.now())
                .build();
        
        ServiceTask updatedTask = ServiceTask.builder()
                .id(1L)
                .title("Fix HVAC System")
                .description("Air conditioning not working")
                .clientAddress("123 Main St, Springfield")
                .priority(Priority.HIGH)
                .estimatedDuration(120)
                .status(TaskStatus.COMPLETED)
                .createdBy(1L)
                .createdAt(LocalDateTime.now())
                .build();
        
        UpdateTaskStatusRequest request = UpdateTaskStatusRequest.builder()
                .status(TaskStatus.COMPLETED)
                .workSummary("Fixed the air conditioning unit")
                .build();
        
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(ServiceTask.class))).thenReturn(updatedTask);
        
        // Act
        taskService.updateTaskStatus(1L, request);
        
        // Assert
        ArgumentCaptor<TaskCompletedEvent> eventCaptor = ArgumentCaptor.forClass(TaskCompletedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        
        TaskCompletedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(1L, capturedEvent.getTaskId());
        assertEquals("Fix HVAC System", capturedEvent.getTitle());
        assertEquals("123 Main St, Springfield", capturedEvent.getClientAddress());
        assertEquals("Fixed the air conditioning unit", capturedEvent.getWorkSummary());
        assertNotNull(capturedEvent.getCompletionTime());
    }
    
    @Test
    @DisplayName("Should not publish event when status does not change to COMPLETED")
    void shouldNotPublishEventWhenStatusDoesNotChangeToCompleted() {
        // Arrange
        ServiceTask existingTask = ServiceTask.builder()
                .id(1L)
                .title("Fix HVAC System")
                .clientAddress("123 Main St, Springfield")
                .priority(Priority.HIGH)
                .estimatedDuration(120)
                .status(TaskStatus.UNASSIGNED)
                .createdBy(1L)
                .createdAt(LocalDateTime.now())
                .build();
        
        ServiceTask updatedTask = ServiceTask.builder()
                .id(1L)
                .title("Fix HVAC System")
                .clientAddress("123 Main St, Springfield")
                .priority(Priority.HIGH)
                .estimatedDuration(120)
                .status(TaskStatus.IN_PROGRESS)
                .createdBy(1L)
                .createdAt(LocalDateTime.now())
                .build();
        
        UpdateTaskStatusRequest request = UpdateTaskStatusRequest.builder()
                .status(TaskStatus.IN_PROGRESS)
                .build();
        
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(ServiceTask.class))).thenReturn(updatedTask);
        
        // Act
        taskService.updateTaskStatus(1L, request);
        
        // Assert
        verify(eventPublisher, never()).publishEvent(any());
    }
    
    @Test
    @DisplayName("Should not publish event when task is already COMPLETED")
    void shouldNotPublishEventWhenTaskAlreadyCompleted() {
        // Arrange
        ServiceTask existingTask = ServiceTask.builder()
                .id(1L)
                .title("Fix HVAC System")
                .clientAddress("123 Main St, Springfield")
                .priority(Priority.HIGH)
                .estimatedDuration(120)
                .status(TaskStatus.COMPLETED)
                .createdBy(1L)
                .createdAt(LocalDateTime.now())
                .build();
        
        UpdateTaskStatusRequest request = UpdateTaskStatusRequest.builder()
                .status(TaskStatus.COMPLETED)
                .workSummary("Already completed")
                .build();
        
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(ServiceTask.class))).thenReturn(existingTask);
        
        // Act
        taskService.updateTaskStatus(1L, request);
        
        // Assert
        verify(eventPublisher, never()).publishEvent(any());
    }
    
    @Test
    @DisplayName("Should throw exception when task not found")
    void shouldThrowExceptionWhenTaskNotFound() {
        // Arrange
        UpdateTaskStatusRequest request = UpdateTaskStatusRequest.builder()
                .status(TaskStatus.COMPLETED)
                .build();
        
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.updateTaskStatus(999L, request)
        );
        
        assertEquals("Task not found with id: 999", exception.getMessage());
        verify(taskRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }
    
    // ========== Assignment Tests ==========
    
    @Test
    @DisplayName("Should assign task to technician successfully")
    void shouldAssignTaskSuccessfully() {
        // Arrange
        Long taskId = 1L;
        Long technicianId = 10L;
        String assignedBy = "dispatcher1";
        
        ServiceTask task = ServiceTask.builder()
                .id(taskId)
                .title("Fix HVAC")
                .description("AC issue")
                .clientAddress("123 Main St")
                .priority(Priority.HIGH)
                .estimatedDuration(120)
                .status(TaskStatus.UNASSIGNED)
                .createdBy(1L)
                .createdAt(LocalDateTime.now())
                .build();
        
        TaskAssignment assignment = TaskAssignment.builder()
                .id(1L)
                .taskId(taskId)
                .technicianId(technicianId)
                .assignedBy(assignedBy)
                .assignedAt(LocalDateTime.now())
                .build();
        
        AssignTaskRequest request = AssignTaskRequest.builder()
                .technicianId(technicianId)
                .assignedBy(assignedBy)
                .build();
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(assignmentRepository.save(any(TaskAssignment.class))).thenReturn(assignment);
        when(taskRepository.save(any(ServiceTask.class))).thenReturn(task);
        
        // Act
        TaskAssignmentResponse response = taskService.assignTask(taskId, request);
        
        // Assert
        assertNotNull(response);
        assertEquals(taskId, response.getTaskId());
        assertEquals(technicianId, response.getTechnicianId());
        assertEquals(assignedBy, response.getAssignedBy());
        assertNotNull(response.getAssignedAt());
        
        // Verify task status was updated to ASSIGNED
        ArgumentCaptor<ServiceTask> taskCaptor = ArgumentCaptor.forClass(ServiceTask.class);
        verify(taskRepository, times(1)).save(taskCaptor.capture());
        ServiceTask savedTask = taskCaptor.getValue();
        assertEquals(TaskStatus.ASSIGNED, savedTask.getStatus());
        assertEquals(technicianId, savedTask.getAssignedTo());
        
        // Verify assignment was saved
        verify(assignmentRepository, times(1)).save(any(TaskAssignment.class));
    }
    
    @Test
    @DisplayName("Should assign task with system as assignedBy when not provided")
    void shouldAssignTaskWithSystemWhenAssignedByNotProvided() {
        // Arrange
        Long taskId = 1L;
        Long technicianId = 10L;
        
        ServiceTask task = ServiceTask.builder()
                .id(taskId)
                .title("Fix HVAC")
                .description("AC issue")
                .clientAddress("123 Main St")
                .priority(Priority.HIGH)
                .estimatedDuration(120)
                .status(TaskStatus.UNASSIGNED)
                .createdBy(1L)
                .createdAt(LocalDateTime.now())
                .build();
        
        TaskAssignment assignment = TaskAssignment.builder()
                .id(1L)
                .taskId(taskId)
                .technicianId(technicianId)
                .assignedBy("system")
                .assignedAt(LocalDateTime.now())
                .build();
        
        AssignTaskRequest request = AssignTaskRequest.builder()
                .technicianId(technicianId)
                .assignedBy(null)
                .build();
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(assignmentRepository.save(any(TaskAssignment.class))).thenReturn(assignment);
        when(taskRepository.save(any(ServiceTask.class))).thenReturn(task);
        
        // Act
        TaskAssignmentResponse response = taskService.assignTask(taskId, request);
        
        // Assert
        assertNotNull(response);
        assertEquals("system", response.getAssignedBy());
        
        // Verify assignment was created with "system" as assignedBy
        ArgumentCaptor<TaskAssignment> assignmentCaptor = ArgumentCaptor.forClass(TaskAssignment.class);
        verify(assignmentRepository, times(1)).save(assignmentCaptor.capture());
        TaskAssignment savedAssignment = assignmentCaptor.getValue();
        assertEquals("system", savedAssignment.getAssignedBy());
    }
    
    @Test
    @DisplayName("Should allow reassigning already assigned task")
    void shouldAllowReassigningAlreadyAssignedTask() {
        // Arrange
        Long taskId = 1L;
        Long oldTechnicianId = 10L;
        Long newTechnicianId = 20L;
        String assignedBy = "dispatcher1";
        
        ServiceTask task = ServiceTask.builder()
                .id(taskId)
                .title("Fix HVAC")
                .description("AC issue")
                .clientAddress("123 Main St")
                .priority(Priority.HIGH)
                .estimatedDuration(120)
                .status(TaskStatus.ASSIGNED)
                .assignedTo(oldTechnicianId)
                .createdBy(1L)
                .createdAt(LocalDateTime.now())
                .build();
        
        TaskAssignment assignment = TaskAssignment.builder()
                .id(2L)
                .taskId(taskId)
                .technicianId(newTechnicianId)
                .assignedBy(assignedBy)
                .assignedAt(LocalDateTime.now())
                .build();
        
        AssignTaskRequest request = AssignTaskRequest.builder()
                .technicianId(newTechnicianId)
                .assignedBy(assignedBy)
                .build();
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(assignmentRepository.save(any(TaskAssignment.class))).thenReturn(assignment);
        when(taskRepository.save(any(ServiceTask.class))).thenReturn(task);
        
        // Act
        TaskAssignmentResponse response = taskService.assignTask(taskId, request);
        
        // Assert
        assertNotNull(response);
        assertEquals(newTechnicianId, response.getTechnicianId());
        
        // Verify task was updated with new technician
        ArgumentCaptor<ServiceTask> taskCaptor = ArgumentCaptor.forClass(ServiceTask.class);
        verify(taskRepository, times(1)).save(taskCaptor.capture());
        ServiceTask savedTask = taskCaptor.getValue();
        assertEquals(newTechnicianId, savedTask.getAssignedTo());
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
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.assignTask(taskId, request)
        );
        
        assertEquals("Task not found with id: 999", exception.getMessage());
        verify(assignmentRepository, never()).save(any());
        verify(taskRepository, times(1)).findById(taskId);
    }
    
    @Test
    @DisplayName("Should throw exception when assigning task in IN_PROGRESS status")
    void shouldThrowExceptionWhenAssigningInProgressTask() {
        // Arrange
        Long taskId = 1L;
        ServiceTask task = ServiceTask.builder()
                .id(taskId)
                .title("Fix HVAC")
                .description("AC issue")
                .clientAddress("123 Main St")
                .priority(Priority.HIGH)
                .estimatedDuration(120)
                .status(TaskStatus.IN_PROGRESS)
                .assignedTo(10L)
                .createdBy(1L)
                .createdAt(LocalDateTime.now())
                .build();
        
        AssignTaskRequest request = AssignTaskRequest.builder()
                .technicianId(20L)
                .assignedBy("dispatcher1")
                .build();
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.assignTask(taskId, request)
        );
        
        assertTrue(exception.getMessage().contains("Task can only be assigned when in UNASSIGNED or ASSIGNED status"));
        verify(assignmentRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Should throw exception when assigning completed task")
    void shouldThrowExceptionWhenAssigningCompletedTask() {
        // Arrange
        Long taskId = 1L;
        ServiceTask task = ServiceTask.builder()
                .id(taskId)
                .title("Fix HVAC")
                .description("AC issue")
                .clientAddress("123 Main St")
                .priority(Priority.HIGH)
                .estimatedDuration(120)
                .status(TaskStatus.COMPLETED)
                .assignedTo(10L)
                .createdBy(1L)
                .createdAt(LocalDateTime.now())
                .build();
        
        AssignTaskRequest request = AssignTaskRequest.builder()
                .technicianId(20L)
                .assignedBy("dispatcher1")
                .build();
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.assignTask(taskId, request)
        );
        
        assertTrue(exception.getMessage().contains("Task can only be assigned when in UNASSIGNED or ASSIGNED status"));
        verify(assignmentRepository, never()).save(any());
    }
    
    // ==================== Reassignment Tests ====================
    
    @Test
    @DisplayName("Should successfully reassign task to different technician")
    void shouldSuccessfullyReassignTask() {
        // Arrange
        Long taskId = 1L;
        Long oldTechnicianId = 100L;
        Long newTechnicianId = 200L;
        
        com.fsm.task.dto.ReassignTaskRequest request = com.fsm.task.dto.ReassignTaskRequest.builder()
                .technicianId(newTechnicianId)
                .reason("Technician 100 is unavailable")
                .reassignedBy("dispatcher1")
                .build();
        
        ServiceTask task = ServiceTask.builder()
                .id(taskId)
                .title("Test Task")
                .description("Description")
                .clientAddress("123 Test St")
                .priority(Priority.HIGH)
                .estimatedDuration(60)
                .status(TaskStatus.ASSIGNED)
                .assignedTo(oldTechnicianId)
                .createdBy(1L)
                .createdAt(LocalDateTime.now())
                .build();
        
        TaskAssignment newAssignment = TaskAssignment.builder()
                .id(2L)
                .taskId(taskId)
                .technicianId(newTechnicianId)
                .assignedBy("dispatcher1")
                .reason("Technician 100 is unavailable")
                .assignedAt(LocalDateTime.now())
                .build();
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(assignmentRepository.save(any(TaskAssignment.class))).thenReturn(newAssignment);
        when(taskRepository.save(any(ServiceTask.class))).thenReturn(task);
        
        // Act
        TaskAssignmentResponse response = taskService.reassignTask(taskId, request);
        
        // Assert
        assertNotNull(response);
        assertEquals(newTechnicianId, response.getTechnicianId());
        assertEquals(taskId, response.getTaskId());
        assertEquals("dispatcher1", response.getAssignedBy());
        
        // Verify the task's assignedTo field was updated
        ArgumentCaptor<ServiceTask> taskCaptor = ArgumentCaptor.forClass(ServiceTask.class);
        verify(taskRepository).save(taskCaptor.capture());
        assertEquals(newTechnicianId, taskCaptor.getValue().getAssignedTo());
        
        // Verify new assignment was created with reason
        ArgumentCaptor<TaskAssignment> assignmentCaptor = ArgumentCaptor.forClass(TaskAssignment.class);
        verify(assignmentRepository).save(assignmentCaptor.capture());
        assertEquals(newTechnicianId, assignmentCaptor.getValue().getTechnicianId());
        assertEquals("Technician 100 is unavailable", assignmentCaptor.getValue().getReason());
    }
    
    @Test
    @DisplayName("Should use 'system' as default reassignedBy when not provided")
    void shouldUseSystemAsDefaultReassignedBy() {
        // Arrange
        Long taskId = 1L;
        Long oldTechnicianId = 100L;
        Long newTechnicianId = 200L;
        
        com.fsm.task.dto.ReassignTaskRequest request = com.fsm.task.dto.ReassignTaskRequest.builder()
                .technicianId(newTechnicianId)
                .reason("Reassignment reason")
                .build();
        
        ServiceTask task = ServiceTask.builder()
                .id(taskId)
                .title("Test Task")
                .description("Description")
                .clientAddress("123 Test St")
                .priority(Priority.HIGH)
                .estimatedDuration(60)
                .status(TaskStatus.ASSIGNED)
                .assignedTo(oldTechnicianId)
                .createdBy(1L)
                .createdAt(LocalDateTime.now())
                .build();
        
        TaskAssignment newAssignment = TaskAssignment.builder()
                .id(2L)
                .taskId(taskId)
                .technicianId(newTechnicianId)
                .assignedBy("system")
                .reason("Reassignment reason")
                .assignedAt(LocalDateTime.now())
                .build();
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(assignmentRepository.save(any(TaskAssignment.class))).thenReturn(newAssignment);
        when(taskRepository.save(any(ServiceTask.class))).thenReturn(task);
        
        // Act
        taskService.reassignTask(taskId, request);
        
        // Assert
        ArgumentCaptor<TaskAssignment> assignmentCaptor = ArgumentCaptor.forClass(TaskAssignment.class);
        verify(assignmentRepository).save(assignmentCaptor.capture());
        assertEquals("system", assignmentCaptor.getValue().getAssignedBy());
    }
    
    @Test
    @DisplayName("Should fail to reassign task when task not found")
    void shouldFailToReassignTaskWhenTaskNotFound() {
        // Arrange
        Long taskId = 999L;
        com.fsm.task.dto.ReassignTaskRequest request = com.fsm.task.dto.ReassignTaskRequest.builder()
                .technicianId(200L)
                .reason("Test reason")
                .reassignedBy("dispatcher1")
                .build();
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.reassignTask(taskId, request)
        );
        
        assertTrue(exception.getMessage().contains("Task not found with id: " + taskId));
        verify(assignmentRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Should fail to reassign task when task is not currently assigned")
    void shouldFailToReassignTaskWhenNotAssigned() {
        // Arrange
        Long taskId = 1L;
        
        com.fsm.task.dto.ReassignTaskRequest request = com.fsm.task.dto.ReassignTaskRequest.builder()
                .technicianId(200L)
                .reason("Test reason")
                .reassignedBy("dispatcher1")
                .build();
        
        ServiceTask task = ServiceTask.builder()
                .id(taskId)
                .title("Test Task")
                .description("Description")
                .clientAddress("123 Test St")
                .priority(Priority.HIGH)
                .estimatedDuration(60)
                .status(TaskStatus.UNASSIGNED)
                .assignedTo(null) // Not assigned
                .createdBy(1L)
                .createdAt(LocalDateTime.now())
                .build();
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.reassignTask(taskId, request)
        );
        
        assertTrue(exception.getMessage().contains("Task must be currently assigned to reassign"));
        verify(assignmentRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Should fail to reassign task to the same technician")
    void shouldFailToReassignTaskToSameTechnician() {
        // Arrange
        Long taskId = 1L;
        Long technicianId = 100L;
        
        com.fsm.task.dto.ReassignTaskRequest request = com.fsm.task.dto.ReassignTaskRequest.builder()
                .technicianId(technicianId)
                .reason("Test reason")
                .reassignedBy("dispatcher1")
                .build();
        
        ServiceTask task = ServiceTask.builder()
                .id(taskId)
                .title("Test Task")
                .description("Description")
                .clientAddress("123 Test St")
                .priority(Priority.HIGH)
                .estimatedDuration(60)
                .status(TaskStatus.ASSIGNED)
                .assignedTo(technicianId) // Same technician
                .createdBy(1L)
                .createdAt(LocalDateTime.now())
                .build();
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.reassignTask(taskId, request)
        );
        
        assertTrue(exception.getMessage().contains("Cannot reassign to the same technician"));
        verify(assignmentRepository, never()).save(any());
    }
    
    // ==================== Mobile Task List Tests ====================
    
    @Test
    @DisplayName("Should get all tasks for technician without status filter")
    void shouldGetAllTasksForTechnician() {
        // Arrange
        Long technicianId = 100L;
        
        ServiceTask task1 = ServiceTask.builder()
                .id(1L)
                .title("High Priority Task")
                .description("Description")
                .clientAddress("123 Test St")
                .priority(Priority.HIGH)
                .estimatedDuration(60)
                .status(TaskStatus.ASSIGNED)
                .assignedTo(technicianId)
                .createdBy(1L)
                .createdAt(LocalDateTime.now().minusDays(2))
                .build();
        
        ServiceTask task2 = ServiceTask.builder()
                .id(2L)
                .title("Medium Priority Task")
                .description("Description")
                .clientAddress("456 Test Ave")
                .priority(Priority.MEDIUM)
                .estimatedDuration(90)
                .status(TaskStatus.IN_PROGRESS)
                .assignedTo(technicianId)
                .createdBy(1L)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();
        
        List<ServiceTask> tasks = Arrays.asList(task1, task2);
        
        when(taskRepository.findByAssignedToOrderByPriorityDescCreatedAtAsc(technicianId))
                .thenReturn(tasks);
        
        // Act
        List<ServiceTaskResponse> responses = taskService.getTechnicianTasks(technicianId, null);
        
        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(1L, responses.get(0).getId());
        assertEquals(2L, responses.get(1).getId());
        
        verify(taskRepository).findByAssignedToOrderByPriorityDescCreatedAtAsc(technicianId);
        verify(taskRepository, never()).findByAssignedToAndStatusOrderByPriorityDescCreatedAtAsc(any(), any());
    }
    
    @Test
    @DisplayName("Should get tasks for technician filtered by status")
    void shouldGetTasksForTechnicianWithStatusFilter() {
        // Arrange
        Long technicianId = 100L;
        TaskStatus status = TaskStatus.ASSIGNED;
        
        ServiceTask task1 = ServiceTask.builder()
                .id(1L)
                .title("Task 1")
                .description("Description")
                .clientAddress("123 Test St")
                .priority(Priority.HIGH)
                .estimatedDuration(60)
                .status(TaskStatus.ASSIGNED)
                .assignedTo(technicianId)
                .createdBy(1L)
                .createdAt(LocalDateTime.now())
                .build();
        
        List<ServiceTask> tasks = Collections.singletonList(task1);
        
        when(taskRepository.findByAssignedToAndStatusOrderByPriorityDescCreatedAtAsc(technicianId, status))
                .thenReturn(tasks);
        
        // Act
        List<ServiceTaskResponse> responses = taskService.getTechnicianTasks(technicianId, status);
        
        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).getId());
        assertEquals(TaskStatus.ASSIGNED, responses.get(0).getStatus());
        
        verify(taskRepository).findByAssignedToAndStatusOrderByPriorityDescCreatedAtAsc(technicianId, status);
        verify(taskRepository, never()).findByAssignedToOrderByPriorityDescCreatedAtAsc(any());
    }
    
    @Test
    @DisplayName("Should return empty list when technician has no tasks")
    void shouldReturnEmptyListWhenTechnicianHasNoTasks() {
        // Arrange
        Long technicianId = 999L;
        
        when(taskRepository.findByAssignedToOrderByPriorityDescCreatedAtAsc(technicianId))
                .thenReturn(Collections.emptyList());
        
        // Act
        List<ServiceTaskResponse> responses = taskService.getTechnicianTasks(technicianId, null);
        
        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        
        verify(taskRepository).findByAssignedToOrderByPriorityDescCreatedAtAsc(technicianId);
    }
    
    @Test
    @DisplayName("Should sort tasks by priority descending and creation date ascending")
    void shouldSortTasksByPriorityAndCreationDate() {
        // Arrange
        Long technicianId = 100L;
        LocalDateTime now = LocalDateTime.now();
        
        // Tasks with different priorities and creation times
        ServiceTask criticalOld = ServiceTask.builder()
                .id(1L)
                .title("Critical Old")
                .description("Description")
                .clientAddress("123 Test St")
                .priority(Priority.CRITICAL)
                .estimatedDuration(60)
                .status(TaskStatus.ASSIGNED)
                .assignedTo(technicianId)
                .createdBy(1L)
                .createdAt(now.minusDays(3))
                .build();
        
        ServiceTask criticalNew = ServiceTask.builder()
                .id(2L)
                .title("Critical New")
                .description("Description")
                .clientAddress("456 Test Ave")
                .priority(Priority.CRITICAL)
                .estimatedDuration(90)
                .status(TaskStatus.ASSIGNED)
                .assignedTo(technicianId)
                .createdBy(1L)
                .createdAt(now.minusDays(1))
                .build();
        
        ServiceTask highPriority = ServiceTask.builder()
                .id(3L)
                .title("High Priority")
                .description("Description")
                .clientAddress("789 Test Blvd")
                .priority(Priority.HIGH)
                .estimatedDuration(120)
                .status(TaskStatus.ASSIGNED)
                .assignedTo(technicianId)
                .createdBy(1L)
                .createdAt(now.minusDays(2))
                .build();
        
        // Repository should return tasks sorted: CRITICAL (oldest first), then HIGH, then MEDIUM, then LOW
        List<ServiceTask> tasks = Arrays.asList(criticalOld, criticalNew, highPriority);
        
        when(taskRepository.findByAssignedToOrderByPriorityDescCreatedAtAsc(technicianId))
                .thenReturn(tasks);
        
        // Act
        List<ServiceTaskResponse> responses = taskService.getTechnicianTasks(technicianId, null);
        
        // Assert
        assertNotNull(responses);
        assertEquals(3, responses.size());
        
        // Verify order: CRITICAL tasks first (oldest to newest), then HIGH
        assertEquals(1L, responses.get(0).getId()); // Critical old
        assertEquals(Priority.CRITICAL, responses.get(0).getPriority());
        
        assertEquals(2L, responses.get(1).getId()); // Critical new
        assertEquals(Priority.CRITICAL, responses.get(1).getPriority());
        
        assertEquals(3L, responses.get(2).getId()); // High
        assertEquals(Priority.HIGH, responses.get(2).getPriority());
        
        verify(taskRepository).findByAssignedToOrderByPriorityDescCreatedAtAsc(technicianId);
    }
}
