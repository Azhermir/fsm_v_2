package com.fsm.task.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ServiceTask entity
 */
class ServiceTaskTest {
    
    @Test
    @DisplayName("Should create ServiceTask with valid data using factory method")
    void shouldCreateServiceTaskWithValidData() {
        // Arrange
        String title = "Fix HVAC System";
        String description = "Air conditioning not working";
        String clientAddress = "123 Main St, Springfield";
        Priority priority = Priority.HIGH;
        Integer estimatedDuration = 120;
        
        // Act
        ServiceTask task = ServiceTask.createServiceTask(
                title, description, clientAddress, priority, estimatedDuration);
        
        // Assert
        assertNotNull(task);
        assertEquals(title, task.getTitle());
        assertEquals(description, task.getDescription());
        assertEquals(clientAddress, task.getClientAddress());
        assertEquals(priority, task.getPriority());
        assertEquals(estimatedDuration, task.getEstimatedDuration());
        assertEquals(TaskStatus.UNASSIGNED, task.getStatus());
        assertNotNull(task.getCreatedAt());
    }
    
    @Test
    @DisplayName("Should create ServiceTask with builder")
    void shouldCreateServiceTaskWithBuilder() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        
        // Act
        ServiceTask task = ServiceTask.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .clientAddress("456 Oak Ave")
                .priority(Priority.LOW)
                .estimatedDuration(60)
                .status(TaskStatus.ASSIGNED)
                .createdAt(now)
                .build();
        
        // Assert
        assertNotNull(task);
        assertEquals(1L, task.getId());
        assertEquals("Test Task", task.getTitle());
        assertEquals("Test Description", task.getDescription());
        assertEquals("456 Oak Ave", task.getClientAddress());
        assertEquals(Priority.LOW, task.getPriority());
        assertEquals(60, task.getEstimatedDuration());
        assertEquals(TaskStatus.ASSIGNED, task.getStatus());
        assertEquals(now, task.getCreatedAt());
    }
    
    @Test
    @DisplayName("Should throw exception when title is null")
    void shouldThrowExceptionWhenTitleIsNull() {
        // Arrange & Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ServiceTask.createServiceTask(
                        null, "Description", "Address", Priority.MEDIUM, 90, 1L)
        );
        assertEquals("ServiceTask must have a title", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when title is blank")
    void shouldThrowExceptionWhenTitleIsBlank() {
        // Arrange & Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ServiceTask.createServiceTask(
                        "   ", "Description", "Address", Priority.MEDIUM, 90, 1L)
        );
        assertEquals("ServiceTask must have a title", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when title is empty")
    void shouldThrowExceptionWhenTitleIsEmpty() {
        // Arrange & Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ServiceTask.createServiceTask(
                        "", "Description", "Address", Priority.MEDIUM, 90, 1L)
        );
        assertEquals("ServiceTask must have a title", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when priority is null")
    void shouldThrowExceptionWhenPriorityIsNull() {
        // Arrange & Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ServiceTask.createServiceTask(
                        "Title", "Description", "Address", null, 90, 1L)
        );
        assertEquals("Priority must be one of the defined enum values", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when estimated duration is null")
    void shouldThrowExceptionWhenEstimatedDurationIsNull() {
        // Arrange & Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ServiceTask.createServiceTask(
                        "Title", "Description", "Address", Priority.MEDIUM, null)
        );
        assertEquals("EstimatedDuration must be positive", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when estimated duration is zero")
    void shouldThrowExceptionWhenEstimatedDurationIsZero() {
        // Arrange & Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ServiceTask.createServiceTask(
                        "Title", "Description", "Address", Priority.MEDIUM, 0, 1L)
        );
        assertEquals("EstimatedDuration must be positive", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when estimated duration is negative")
    void shouldThrowExceptionWhenEstimatedDurationIsNegative() {
        // Arrange & Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ServiceTask.createServiceTask(
                        "Title", "Description", "Address", Priority.MEDIUM, -10, 1L)
        );
        assertEquals("EstimatedDuration must be positive", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when client address is null")
    void shouldThrowExceptionWhenClientAddressIsNull() {
        // Arrange & Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ServiceTask.createServiceTask(
                        "Title", "Description", null, Priority.MEDIUM, 90, 1L)
        );
        assertEquals("Client address must not be blank", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when client address is blank")
    void shouldThrowExceptionWhenClientAddressIsBlank() {
        // Arrange & Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ServiceTask.createServiceTask(
                        "Title", "Description", "   ", Priority.MEDIUM, 90, 1L)
        );
        assertEquals("Client address must not be blank", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when client address is empty")
    void shouldThrowExceptionWhenClientAddressIsEmpty() {
        // Arrange & Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ServiceTask.createServiceTask(
                        "Title", "Description", "", Priority.MEDIUM, 90, 1L)
        );
        assertEquals("Client address must not be blank", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should create task with all priority levels")
    void shouldCreateTaskWithAllPriorityLevels() {
        // Test all priority enum values
        for (Priority priority : Priority.values()) {
            ServiceTask task = ServiceTask.createServiceTask(
                    "Task", "Description", "Address", priority, 60, 1L);
            assertEquals(priority, task.getPriority());
        }
    }
    
    @Test
    @DisplayName("Should create task with all status values")
    void shouldCreateTaskWithAllStatusValues() {
        // Test all status enum values
        for (TaskStatus status : TaskStatus.values()) {
            ServiceTask task = ServiceTask.builder()
                    .title("Task")
                    .description("Description")
                    .clientAddress("Address")
                    .priority(Priority.LOW)
                    .estimatedDuration(60)
                    .status(status)
                    .createdAt(LocalDateTime.now())
                    .build();
            assertEquals(status, task.getStatus());
        }
    }
    
    @Test
    @DisplayName("Should set createdAt on prePersist when null")
    void shouldSetCreatedAtOnPrePersist() {
        // Arrange
        ServiceTask task = ServiceTask.builder()
                .title("Task")
                .priority(Priority.LOW)
                .estimatedDuration(60)
                .status(TaskStatus.UNASSIGNED)
                .build();
        
        // Act
        task.onCreate();
        
        // Assert
        assertNotNull(task.getCreatedAt());
    }
    
    @Test
    @DisplayName("Should not override existing createdAt on prePersist")
    void shouldNotOverrideExistingCreatedAtOnPrePersist() {
        // Arrange
        LocalDateTime existingTime = LocalDateTime.now().minusDays(1);
        ServiceTask task = ServiceTask.builder()
                .title("Task")
                .priority(Priority.LOW)
                .estimatedDuration(60)
                .status(TaskStatus.UNASSIGNED)
                .createdAt(existingTime)
                .build();
        
        // Act
        task.onCreate();
        
        // Assert
        assertEquals(existingTime, task.getCreatedAt());
    }
    
    @Test
    @DisplayName("Should reject null description")
    void shouldRejectNullDescription() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ServiceTask.createServiceTask("Title", null, "Address", Priority.LOW, 60, 1L));
        
        assertEquals("Description is required", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should reject blank description")
    void shouldRejectBlankDescription() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ServiceTask.createServiceTask("Title", "   ", "Address", Priority.LOW, 60, 1L));
        
        assertEquals("Description is required", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should reject description exceeding max length")
    void shouldRejectDescriptionExceedingMaxLength() {
        // Arrange
        String longDescription = "a".repeat(2001);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ServiceTask.createServiceTask("Title", longDescription, "Address", Priority.LOW, 60, 1L));
        
        assertEquals("Description must not exceed 2000 characters", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should accept description at max length")
    void shouldAcceptDescriptionAtMaxLength() {
        // Arrange
        String maxDescription = "a".repeat(2000);
        
        // Act
        ServiceTask task = ServiceTask.createServiceTask("Title", maxDescription, "Address", Priority.LOW, 60, 1L);
        
        // Assert
        assertNotNull(task);
        assertEquals(maxDescription, task.getDescription());
    }
    
    @Test
    @DisplayName("Should reject title exceeding max length")
    void shouldRejectTitleExceedingMaxLength() {
        // Arrange
        String longTitle = "a".repeat(201);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ServiceTask.createServiceTask(longTitle, "Description", "Address", Priority.LOW, 60, 1L));
        
        assertEquals("Title must not exceed 200 characters", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should accept title at max length")
    void shouldAcceptTitleAtMaxLength() {
        // Arrange
        String maxTitle = "a".repeat(200);
        
        // Act
        ServiceTask task = ServiceTask.createServiceTask(maxTitle, "Description", "Address", Priority.LOW, 60, 1L);
        
        // Assert
        assertNotNull(task);
        assertEquals(maxTitle, task.getTitle());
    }
    
    @Test
    @DisplayName("Should use Lombok generated equals and hashCode")
    void shouldUseLombokGeneratedEqualsAndHashCode() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        ServiceTask task1 = ServiceTask.builder()
                .id(1L)
                .title("Task")
                .description("Description")
                .clientAddress("Address")
                .priority(Priority.HIGH)
                .estimatedDuration(120)
                .status(TaskStatus.ASSIGNED)
                .createdAt(now)
                .build();
        
        ServiceTask task2 = ServiceTask.builder()
                .id(1L)
                .title("Task")
                .description("Description")
                .clientAddress("Address")
                .priority(Priority.HIGH)
                .estimatedDuration(120)
                .status(TaskStatus.ASSIGNED)
                .createdAt(now)
                .build();
        
        // Assert
        assertEquals(task1, task2);
        assertEquals(task1.hashCode(), task2.hashCode());
    }
    
    @Test
    @DisplayName("Should use Lombok generated toString")
    void shouldUseLombokGeneratedToString() {
        // Arrange
        ServiceTask task = ServiceTask.createServiceTask(
                "Task", "Description", "Address", Priority.HIGH, 120, 1L);
        
        // Act
        String toString = task.toString();
        
        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("ServiceTask"));
        assertTrue(toString.contains("Task"));
    }
    
    @Test
    @DisplayName("Should throw exception when createdBy is null")
    void shouldThrowExceptionWhenCreatedByIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ServiceTask.createServiceTask(
                        "Task", "Description", "Address", Priority.HIGH, 120, null)
        );
        
        assertEquals("CreatedBy user ID is required", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should create ServiceTask with valid createdBy")
    void shouldCreateServiceTaskWithValidCreatedBy() {
        // Arrange
        Long createdBy = 42L;
        
        // Act
        ServiceTask task = ServiceTask.createServiceTask(
                "Task", "Description", "Address", Priority.HIGH, 120, createdBy);
        
        // Assert
        assertNotNull(task);
        assertEquals(createdBy, task.getCreatedBy());
    }
}
