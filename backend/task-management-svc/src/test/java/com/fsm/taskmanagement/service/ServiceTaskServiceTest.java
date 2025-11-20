package com.fsm.taskmanagement.service;

import com.fsm.taskmanagement.dto.CreateServiceTaskRequest;
import com.fsm.taskmanagement.dto.ServiceTaskResponse;
import com.fsm.taskmanagement.entity.Priority;
import com.fsm.taskmanagement.entity.ServiceTask;
import com.fsm.taskmanagement.entity.Status;
import com.fsm.taskmanagement.repository.ServiceTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ServiceTaskService Tests")
class ServiceTaskServiceTest {

    @Mock
    private ServiceTaskRepository repository;

    @InjectMocks
    private ServiceTaskService service;

    private CreateServiceTaskRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = CreateServiceTaskRequest.builder()
                .title("Fix HVAC System")
                .description("Repair broken air conditioning unit")
                .clientAddress("123 Main St, City, State")
                .priority(Priority.HIGH)
                .estimatedDuration(120)
                .build();
    }

    @Test
    @DisplayName("Should create task successfully with valid request")
    void shouldCreateTaskSuccessfully() {
        // Given
        ServiceTask savedTask = ServiceTask.builder()
                .id(UUID.randomUUID())
                .title(validRequest.getTitle())
                .description(validRequest.getDescription())
                .clientAddress(validRequest.getClientAddress())
                .priority(validRequest.getPriority())
                .estimatedDuration(validRequest.getEstimatedDuration())
                .status(Status.UNASSIGNED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(repository.save(any(ServiceTask.class))).thenReturn(savedTask);

        // When
        ServiceTaskResponse response = service.createTask(validRequest);

        // Then
        assertNotNull(response);
        assertEquals(savedTask.getId(), response.getId());
        assertEquals(validRequest.getTitle(), response.getTitle());
        assertEquals(validRequest.getDescription(), response.getDescription());
        assertEquals(validRequest.getClientAddress(), response.getClientAddress());
        assertEquals(validRequest.getPriority(), response.getPriority());
        assertEquals(validRequest.getEstimatedDuration(), response.getEstimatedDuration());
        assertEquals(Status.UNASSIGNED, response.getStatus());
        assertNotNull(response.getCreatedAt());
        assertNotNull(response.getUpdatedAt());

        verify(repository, times(1)).save(any(ServiceTask.class));
    }

    @Test
    @DisplayName("Should set default status to UNASSIGNED")
    void shouldSetDefaultStatusToUnassigned() {
        // Given
        ArgumentCaptor<ServiceTask> taskCaptor = ArgumentCaptor.forClass(ServiceTask.class);
        ServiceTask savedTask = ServiceTask.builder()
                .id(UUID.randomUUID())
                .title(validRequest.getTitle())
                .description(validRequest.getDescription())
                .clientAddress(validRequest.getClientAddress())
                .priority(validRequest.getPriority())
                .estimatedDuration(validRequest.getEstimatedDuration())
                .status(Status.UNASSIGNED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(repository.save(any(ServiceTask.class))).thenReturn(savedTask);

        // When
        ServiceTaskResponse response = service.createTask(validRequest);

        // Then
        verify(repository).save(taskCaptor.capture());
        ServiceTask capturedTask = taskCaptor.getValue();
        assertEquals(Status.UNASSIGNED, capturedTask.getStatus());
        assertEquals(Status.UNASSIGNED, response.getStatus());
    }

    @Test
    @DisplayName("Should create task with null estimated duration")
    void shouldCreateTaskWithNullEstimatedDuration() {
        // Given
        CreateServiceTaskRequest requestWithoutDuration = CreateServiceTaskRequest.builder()
                .title("Task without duration")
                .description("Description")
                .clientAddress("Address")
                .priority(Priority.LOW)
                .estimatedDuration(null)
                .build();

        ServiceTask savedTask = ServiceTask.builder()
                .id(UUID.randomUUID())
                .title(requestWithoutDuration.getTitle())
                .description(requestWithoutDuration.getDescription())
                .clientAddress(requestWithoutDuration.getClientAddress())
                .priority(requestWithoutDuration.getPriority())
                .estimatedDuration(null)
                .status(Status.UNASSIGNED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(repository.save(any(ServiceTask.class))).thenReturn(savedTask);

        // When
        ServiceTaskResponse response = service.createTask(requestWithoutDuration);

        // Then
        assertNull(response.getEstimatedDuration());
        verify(repository, times(1)).save(any(ServiceTask.class));
    }

    @Test
    @DisplayName("Should create task with all priority levels")
    void shouldCreateTaskWithAllPriorityLevels() {
        for (Priority priority : Priority.values()) {
            // Given
            CreateServiceTaskRequest request = CreateServiceTaskRequest.builder()
                    .title("Task " + priority)
                    .description("Description")
                    .clientAddress("Address")
                    .priority(priority)
                    .build();

            ServiceTask savedTask = ServiceTask.builder()
                    .id(UUID.randomUUID())
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .clientAddress(request.getClientAddress())
                    .priority(priority)
                    .status(Status.UNASSIGNED)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(repository.save(any(ServiceTask.class))).thenReturn(savedTask);

            // When
            ServiceTaskResponse response = service.createTask(request);

            // Then
            assertEquals(priority, response.getPriority());
        }

        verify(repository, times(Priority.values().length)).save(any(ServiceTask.class));
    }

    @Test
    @DisplayName("Should throw exception when title is null")
    void shouldThrowExceptionWhenTitleIsNull() {
        // Given
        CreateServiceTaskRequest invalidRequest = CreateServiceTaskRequest.builder()
                .title(null)
                .description("Description")
                .clientAddress("Address")
                .priority(Priority.HIGH)
                .build();

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> service.createTask(invalidRequest));
        verify(repository, never()).save(any(ServiceTask.class));
    }

    @Test
    @DisplayName("Should throw exception when title is empty")
    void shouldThrowExceptionWhenTitleIsEmpty() {
        // Given
        CreateServiceTaskRequest invalidRequest = CreateServiceTaskRequest.builder()
                .title("")
                .description("Description")
                .clientAddress("Address")
                .priority(Priority.HIGH)
                .build();

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> service.createTask(invalidRequest));
        verify(repository, never()).save(any(ServiceTask.class));
    }

    @Test
    @DisplayName("Should throw exception when title is blank")
    void shouldThrowExceptionWhenTitleIsBlank() {
        // Given
        CreateServiceTaskRequest invalidRequest = CreateServiceTaskRequest.builder()
                .title("   ")
                .description("Description")
                .clientAddress("Address")
                .priority(Priority.HIGH)
                .build();

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> service.createTask(invalidRequest));
        verify(repository, never()).save(any(ServiceTask.class));
    }

    @Test
    @DisplayName("Should throw exception when description is null")
    void shouldThrowExceptionWhenDescriptionIsNull() {
        // Given
        CreateServiceTaskRequest invalidRequest = CreateServiceTaskRequest.builder()
                .title("Title")
                .description(null)
                .clientAddress("Address")
                .priority(Priority.HIGH)
                .build();

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> service.createTask(invalidRequest));
        verify(repository, never()).save(any(ServiceTask.class));
    }

    @Test
    @DisplayName("Should throw exception when description is empty")
    void shouldThrowExceptionWhenDescriptionIsEmpty() {
        // Given
        CreateServiceTaskRequest invalidRequest = CreateServiceTaskRequest.builder()
                .title("Title")
                .description("")
                .clientAddress("Address")
                .priority(Priority.HIGH)
                .build();

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> service.createTask(invalidRequest));
        verify(repository, never()).save(any(ServiceTask.class));
    }

    @Test
    @DisplayName("Should throw exception when clientAddress is null")
    void shouldThrowExceptionWhenClientAddressIsNull() {
        // Given
        CreateServiceTaskRequest invalidRequest = CreateServiceTaskRequest.builder()
                .title("Title")
                .description("Description")
                .clientAddress(null)
                .priority(Priority.HIGH)
                .build();

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> service.createTask(invalidRequest));
        verify(repository, never()).save(any(ServiceTask.class));
    }

    @Test
    @DisplayName("Should throw exception when clientAddress is empty")
    void shouldThrowExceptionWhenClientAddressIsEmpty() {
        // Given
        CreateServiceTaskRequest invalidRequest = CreateServiceTaskRequest.builder()
                .title("Title")
                .description("Description")
                .clientAddress("")
                .priority(Priority.HIGH)
                .build();

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> service.createTask(invalidRequest));
        verify(repository, never()).save(any(ServiceTask.class));
    }

    @Test
    @DisplayName("Should throw exception when priority is null")
    void shouldThrowExceptionWhenPriorityIsNull() {
        // Given
        CreateServiceTaskRequest invalidRequest = CreateServiceTaskRequest.builder()
                .title("Title")
                .description("Description")
                .clientAddress("Address")
                .priority(null)
                .build();

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> service.createTask(invalidRequest));
        verify(repository, never()).save(any(ServiceTask.class));
    }

    @Test
    @DisplayName("Should map all fields from request to entity")
    void shouldMapAllFieldsFromRequestToEntity() {
        // Given
        ArgumentCaptor<ServiceTask> taskCaptor = ArgumentCaptor.forClass(ServiceTask.class);
        ServiceTask savedTask = ServiceTask.builder()
                .id(UUID.randomUUID())
                .title(validRequest.getTitle())
                .description(validRequest.getDescription())
                .clientAddress(validRequest.getClientAddress())
                .priority(validRequest.getPriority())
                .estimatedDuration(validRequest.getEstimatedDuration())
                .status(Status.UNASSIGNED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(repository.save(any(ServiceTask.class))).thenReturn(savedTask);

        // When
        service.createTask(validRequest);

        // Then
        verify(repository).save(taskCaptor.capture());
        ServiceTask capturedTask = taskCaptor.getValue();
        assertEquals(validRequest.getTitle(), capturedTask.getTitle());
        assertEquals(validRequest.getDescription(), capturedTask.getDescription());
        assertEquals(validRequest.getClientAddress(), capturedTask.getClientAddress());
        assertEquals(validRequest.getPriority(), capturedTask.getPriority());
        assertEquals(validRequest.getEstimatedDuration(), capturedTask.getEstimatedDuration());
    }

    @Test
    @DisplayName("Should map all fields from entity to response")
    void shouldMapAllFieldsFromEntityToResponse() {
        // Given
        UUID taskId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        ServiceTask savedTask = ServiceTask.builder()
                .id(taskId)
                .title(validRequest.getTitle())
                .description(validRequest.getDescription())
                .clientAddress(validRequest.getClientAddress())
                .priority(validRequest.getPriority())
                .estimatedDuration(validRequest.getEstimatedDuration())
                .status(Status.UNASSIGNED)
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(repository.save(any(ServiceTask.class))).thenReturn(savedTask);

        // When
        ServiceTaskResponse response = service.createTask(validRequest);

        // Then
        assertEquals(taskId, response.getId());
        assertEquals(savedTask.getTitle(), response.getTitle());
        assertEquals(savedTask.getDescription(), response.getDescription());
        assertEquals(savedTask.getClientAddress(), response.getClientAddress());
        assertEquals(savedTask.getPriority(), response.getPriority());
        assertEquals(savedTask.getEstimatedDuration(), response.getEstimatedDuration());
        assertEquals(savedTask.getStatus(), response.getStatus());
        assertEquals(savedTask.getCreatedAt(), response.getCreatedAt());
        assertEquals(savedTask.getUpdatedAt(), response.getUpdatedAt());
    }
}
