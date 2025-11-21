package com.fsm.task.repository;

import com.fsm.task.domain.Priority;
import com.fsm.task.domain.ServiceTask;
import com.fsm.task.domain.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for DatabaseServiceTaskRepository
 * Tests database persistence operations using H2 in-memory database
 */
@DataJpaTest
@Import(DatabaseServiceTaskRepository.class)
@TestPropertySource(locations = "classpath:application.properties")
class DatabaseServiceTaskRepositoryTest {
    
    @Autowired
    private DatabaseServiceTaskRepository repository;
    
    @Autowired
    private TestEntityManager entityManager;
    
    @BeforeEach
    void setUp() {
        // Clear database before each test
        entityManager.clear();
    }
    
    @Test
    @DisplayName("Should save new ServiceTask to database")
    void shouldSaveNewServiceTask() {
        // Arrange
        ServiceTask task = ServiceTask.createServiceTask(
                "Fix HVAC System",
                "Air conditioning not working",
                "123 Main St, Springfield",
                Priority.HIGH,
                120
        );
        
        // Act
        ServiceTask saved = repository.save(task);
        entityManager.flush();
        
        // Assert
        assertNotNull(saved.getId());
        assertEquals("Fix HVAC System", saved.getTitle());
        assertEquals("Air conditioning not working", saved.getDescription());
        assertEquals("123 Main St, Springfield", saved.getClientAddress());
        assertEquals(Priority.HIGH, saved.getPriority());
        assertEquals(120, saved.getEstimatedDuration());
        assertEquals(TaskStatus.UNASSIGNED, saved.getStatus());
        assertNotNull(saved.getCreatedAt());
    }
    
    @Test
    @DisplayName("Should generate ID when saving new ServiceTask")
    void shouldGenerateIdWhenSavingNewTask() {
        // Arrange
        ServiceTask task = ServiceTask.createServiceTask(
                "Test Task",
                "Description",
                "Address",
                Priority.LOW,
                60
        );
        
        // Act
        ServiceTask saved = repository.save(task);
        entityManager.flush();
        
        // Assert
        assertNotNull(saved.getId());
        assertTrue(saved.getId() > 0);
    }
    
    @Test
    @DisplayName("Should update existing ServiceTask")
    void shouldUpdateExistingServiceTask() {
        // Arrange - Create and save initial task
        ServiceTask original = ServiceTask.createServiceTask(
                "Original Title",
                "Original Description",
                "Original Address",
                Priority.LOW,
                60
        );
        ServiceTask saved = repository.save(original);
        entityManager.flush();
        entityManager.clear();
        
        // Act - Update the task
        ServiceTask toUpdate = ServiceTask.builder()
                .id(saved.getId())
                .title("Updated Title")
                .description("Updated Description")
                .clientAddress("Updated Address")
                .priority(Priority.HIGH)
                .estimatedDuration(120)
                .status(TaskStatus.IN_PROGRESS)
                .createdAt(saved.getCreatedAt())
                .build();
        
        ServiceTask updated = repository.save(toUpdate);
        entityManager.flush();
        
        // Assert
        assertEquals(saved.getId(), updated.getId());
        assertEquals("Updated Title", updated.getTitle());
        assertEquals("Updated Description", updated.getDescription());
        assertEquals("Updated Address", updated.getClientAddress());
        assertEquals(Priority.HIGH, updated.getPriority());
        assertEquals(120, updated.getEstimatedDuration());
        assertEquals(TaskStatus.IN_PROGRESS, updated.getStatus());
    }
    
    @Test
    @DisplayName("Should find ServiceTask by ID")
    void shouldFindServiceTaskById() {
        // Arrange
        ServiceTask task = ServiceTask.createServiceTask(
                "Find Me",
                "Test finding task by ID",
                "Test Address",
                Priority.MEDIUM,
                90
        );
        ServiceTask saved = repository.save(task);
        entityManager.flush();
        entityManager.clear();
        
        // Act
        Optional<ServiceTask> found = repository.findById(saved.getId());
        
        // Assert
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals("Find Me", found.get().getTitle());
    }
    
    @Test
    @DisplayName("Should return empty Optional when task not found by ID")
    void shouldReturnEmptyWhenTaskNotFoundById() {
        // Act
        Optional<ServiceTask> found = repository.findById(999L);
        
        // Assert
        assertFalse(found.isPresent());
    }
    
    @Test
    @DisplayName("Should return empty Optional when ID is null")
    void shouldReturnEmptyWhenIdIsNull() {
        // Act
        Optional<ServiceTask> found = repository.findById(null);
        
        // Assert
        assertFalse(found.isPresent());
    }
    
    @Test
    @DisplayName("Should find all ServiceTasks")
    void shouldFindAllServiceTasks() {
        // Arrange - Create multiple tasks
        ServiceTask task1 = ServiceTask.createServiceTask(
                "Task 1", "Description 1", "Address 1", Priority.LOW, 60);
        ServiceTask task2 = ServiceTask.createServiceTask(
                "Task 2", "Description 2", "Address 2", Priority.MEDIUM, 90);
        ServiceTask task3 = ServiceTask.createServiceTask(
                "Task 3", "Description 3", "Address 3", Priority.HIGH, 120);
        
        repository.save(task1);
        repository.save(task2);
        repository.save(task3);
        entityManager.flush();
        
        // Act
        List<ServiceTask> all = repository.findAll();
        
        // Assert
        assertEquals(3, all.size());
    }
    
    @Test
    @DisplayName("Should return empty list when no tasks exist")
    void shouldReturnEmptyListWhenNoTasksExist() {
        // Act
        List<ServiceTask> all = repository.findAll();
        
        // Assert
        assertNotNull(all);
        assertTrue(all.isEmpty());
    }
    
    @Test
    @DisplayName("Should delete ServiceTask by ID")
    void shouldDeleteServiceTaskById() {
        // Arrange
        ServiceTask task = ServiceTask.createServiceTask(
                "Delete Me", "Test deletion", "Address", Priority.LOW, 60);
        ServiceTask saved = repository.save(task);
        entityManager.flush();
        Long taskId = saved.getId();
        
        // Act
        boolean deleted = repository.delete(taskId);
        entityManager.flush();
        
        // Assert
        assertTrue(deleted);
        assertFalse(repository.findById(taskId).isPresent());
    }
    
    @Test
    @DisplayName("Should return false when deleting non-existent task")
    void shouldReturnFalseWhenDeletingNonExistentTask() {
        // Act
        boolean deleted = repository.delete(999L);
        
        // Assert
        assertFalse(deleted);
    }
    
    @Test
    @DisplayName("Should return false when deleting with null ID")
    void shouldReturnFalseWhenDeletingWithNullId() {
        // Act
        boolean deleted = repository.delete(null);
        
        // Assert
        assertFalse(deleted);
    }
    
    @Test
    @DisplayName("Should throw exception when saving null task")
    void shouldThrowExceptionWhenSavingNullTask() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> repository.save(null)
        );
        assertEquals("ServiceTask cannot be null", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should persist createdAt timestamp")
    void shouldPersistCreatedAtTimestamp() {
        // Arrange
        LocalDateTime beforeSave = LocalDateTime.now().minusSeconds(1);
        ServiceTask task = ServiceTask.createServiceTask(
                "Test Task", "Description", "Address", Priority.LOW, 60);
        
        // Act
        ServiceTask saved = repository.save(task);
        entityManager.flush();
        entityManager.clear();
        
        LocalDateTime afterSave = LocalDateTime.now().plusSeconds(1);
        
        // Assert
        Optional<ServiceTask> found = repository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertNotNull(found.get().getCreatedAt());
        assertTrue(found.get().getCreatedAt().isAfter(beforeSave));
        assertTrue(found.get().getCreatedAt().isBefore(afterSave));
    }
    
    @Test
    @DisplayName("Should maintain createdAt as immutable on update")
    void shouldMaintainCreatedAtAsImmutableOnUpdate() {
        // Arrange
        ServiceTask task = ServiceTask.createServiceTask(
                "Original", "Description", "Address", Priority.LOW, 60);
        ServiceTask saved = repository.save(task);
        entityManager.flush();
        entityManager.clear();
        
        LocalDateTime originalCreatedAt = saved.getCreatedAt();
        
        // Act - Update task
        ServiceTask toUpdate = ServiceTask.builder()
                .id(saved.getId())
                .title("Updated")
                .description("Updated Description")
                .clientAddress("Updated Address")
                .priority(Priority.HIGH)
                .estimatedDuration(120)
                .status(TaskStatus.ASSIGNED)
                .createdAt(originalCreatedAt)
                .build();
        
        repository.save(toUpdate);
        entityManager.flush();
        entityManager.clear();
        
        // Assert
        Optional<ServiceTask> found = repository.findById(saved.getId());
        assertTrue(found.isPresent());
        // Compare timestamps are within acceptable range (database precision limits)
        LocalDateTime foundCreatedAt = found.get().getCreatedAt();
        long millisDiff = Math.abs(java.time.Duration.between(originalCreatedAt, foundCreatedAt).toMillis());
        assertTrue(millisDiff < 1, "CreatedAt timestamps should be within 1 millisecond");
    }
    
    @Test
    @DisplayName("Should persist all priority enum values")
    void shouldPersistAllPriorityEnumValues() {
        // Test each priority level
        for (Priority priority : Priority.values()) {
            // Arrange
            ServiceTask task = ServiceTask.createServiceTask(
                    "Task " + priority, "Description", "Address", priority, 60);
            
            // Act
            ServiceTask saved = repository.save(task);
            entityManager.flush();
            entityManager.clear();
            
            // Assert
            Optional<ServiceTask> found = repository.findById(saved.getId());
            assertTrue(found.isPresent());
            assertEquals(priority, found.get().getPriority());
        }
    }
    
    @Test
    @DisplayName("Should persist all status enum values")
    void shouldPersistAllStatusEnumValues() {
        // Test each status value
        for (TaskStatus status : TaskStatus.values()) {
            // Arrange
            ServiceTask task = ServiceTask.builder()
                    .title("Task " + status)
                    .description("Description")
                    .clientAddress("Address")
                    .priority(Priority.LOW)
                    .estimatedDuration(60)
                    .status(status)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            // Act
            ServiceTask saved = repository.save(task);
            entityManager.flush();
            entityManager.clear();
            
            // Assert
            Optional<ServiceTask> found = repository.findById(saved.getId());
            assertTrue(found.isPresent());
            assertEquals(status, found.get().getStatus());
        }
    }
    
    @Test
    @DisplayName("Should handle tasks with valid description")
    void shouldHandleTasksWithValidDescription() {
        // Arrange
        ServiceTask task = ServiceTask.createServiceTask(
                "Task", "Valid description", "Address", Priority.LOW, 60);
        
        // Act
        ServiceTask saved = repository.save(task);
        entityManager.flush();
        entityManager.clear();
        
        // Assert
        Optional<ServiceTask> found = repository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Valid description", found.get().getDescription());
    }
    
    @Test
    @DisplayName("Should reject tasks with null description")
    void shouldRejectTasksWithNullDescription() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                ServiceTask.createServiceTask("Task", null, "Address", Priority.LOW, 60));
    }
    
    @Test
    @DisplayName("Should handle long descriptions")
    void shouldHandleLongDescriptions() {
        // Arrange - Create description near the 1000 character limit
        String longDescription = "A".repeat(950);
        ServiceTask task = ServiceTask.createServiceTask(
                "Task", longDescription, "Address", Priority.LOW, 60);
        
        // Act
        ServiceTask saved = repository.save(task);
        entityManager.flush();
        entityManager.clear();
        
        // Assert
        Optional<ServiceTask> found = repository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(longDescription, found.get().getDescription());
    }
}
