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
                        null, "Description", "Address", Priority.MEDIUM, 90)
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
                        "   ", "Description", "Address", Priority.MEDIUM, 90)
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
                        "", "Description", "Address", Priority.MEDIUM, 90)
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
                        "Title", "Description", "Address", null, 90)
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
                        "Title", "Description", "Address", Priority.MEDIUM, 0)
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
                        "Title", "Description", "Address", Priority.MEDIUM, -10)
        );
        assertEquals("EstimatedDuration must be positive", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should create task with all priority levels")
    void shouldCreateTaskWithAllPriorityLevels() {
        // Test all priority enum values
        for (Priority priority : Priority.values()) {
            ServiceTask task = ServiceTask.createServiceTask(
                    "Task", "Description", "Address", priority, 60);
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
    @DisplayName("Should allow null description")
    void shouldAllowNullDescription() {
        // Act
        ServiceTask task = ServiceTask.createServiceTask(
                "Title", null, "Address", Priority.LOW, 60);
        
        // Assert
        assertNotNull(task);
        assertNull(task.getDescription());
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
                "Task", "Description", "Address", Priority.HIGH, 120);
        
        // Act
        String toString = task.toString();
        
        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("ServiceTask"));
        assertTrue(toString.contains("Task"));
    }
}
