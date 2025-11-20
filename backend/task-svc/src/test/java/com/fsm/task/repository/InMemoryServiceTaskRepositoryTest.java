package com.fsm.task.repository;

import com.fsm.task.domain.Priority;
import com.fsm.task.domain.ServiceTask;
import com.fsm.task.domain.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InMemoryServiceTaskRepository
 */
class InMemoryServiceTaskRepositoryTest {
    
    private InMemoryServiceTaskRepository repository;
    
    @BeforeEach
    void setUp() {
        repository = new InMemoryServiceTaskRepository();
    }
    
    @Test
    @DisplayName("Should initialize repository with mock data")
    void shouldInitializeWithMockData() {
        // Assert
        List<ServiceTask> tasks = repository.findAll();
        assertNotNull(tasks);
        assertEquals(3, tasks.size());
        assertTrue(repository.size() > 0);
    }
    
    @Test
    @DisplayName("Should save new ServiceTask and generate ID")
    void shouldSaveNewServiceTaskAndGenerateId() {
        // Arrange
        repository.clear();
        ServiceTask newTask = ServiceTask.createServiceTask(
                "New Task", 
                "Description", 
                "Address", 
                Priority.LOW, 
                60);
        
        // Act
        ServiceTask saved = repository.save(newTask);
        
        // Assert
        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("New Task", saved.getTitle());
        assertEquals(1, repository.size());
    }
    
    @Test
    @DisplayName("Should save and return ServiceTask with same properties")
    void shouldSaveAndReturnServiceTaskWithSameProperties() {
        // Arrange
        repository.clear();
        ServiceTask task = ServiceTask.createServiceTask(
                "Test Task", 
                "Test Description", 
                "123 Test St", 
                Priority.HIGH, 
                120);
        
        // Act
        ServiceTask saved = repository.save(task);
        
        // Assert
        assertEquals("Test Task", saved.getTitle());
        assertEquals("Test Description", saved.getDescription());
        assertEquals("123 Test St", saved.getClientAddress());
        assertEquals(Priority.HIGH, saved.getPriority());
        assertEquals(120, saved.getEstimatedDuration());
        assertEquals(TaskStatus.UNASSIGNED, saved.getStatus());
    }
    
    @Test
    @DisplayName("Should update existing ServiceTask (idempotent operation)")
    void shouldUpdateExistingServiceTask() {
        // Arrange
        repository.clear();
        ServiceTask task = ServiceTask.createServiceTask(
                "Original Title", 
                "Original Description", 
                "Original Address", 
                Priority.LOW, 
                60);
        ServiceTask saved = repository.save(task);
        Long taskId = saved.getId();
        
        // Act - update the task
        ServiceTask updated = ServiceTask.builder()
                .id(taskId)
                .title("Updated Title")
                .description("Updated Description")
                .clientAddress("Updated Address")
                .priority(Priority.CRITICAL)
                .estimatedDuration(180)
                .status(TaskStatus.COMPLETED)
                .createdAt(saved.getCreatedAt())
                .build();
        ServiceTask result = repository.save(updated);
        
        // Assert
        assertEquals(taskId, result.getId());
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Description", result.getDescription());
        assertEquals("Updated Address", result.getClientAddress());
        assertEquals(Priority.CRITICAL, result.getPriority());
        assertEquals(180, result.getEstimatedDuration());
        assertEquals(TaskStatus.COMPLETED, result.getStatus());
        assertEquals(1, repository.size()); // Still only 1 task
    }
    
    @Test
    @DisplayName("Should throw exception when saving null ServiceTask")
    void shouldThrowExceptionWhenSavingNull() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> repository.save(null));
    }
    
    @Test
    @DisplayName("Should find ServiceTask by ID")
    void shouldFindServiceTaskById() {
        // Arrange
        repository.clear();
        ServiceTask task = ServiceTask.createServiceTask(
                "Find Me", 
                "Description", 
                "Address", 
                Priority.MEDIUM, 
                90);
        ServiceTask saved = repository.save(task);
        
        // Act
        Optional<ServiceTask> found = repository.findById(saved.getId());
        
        // Assert
        assertTrue(found.isPresent());
        assertEquals("Find Me", found.get().getTitle());
        assertEquals(saved.getId(), found.get().getId());
    }
    
    @Test
    @DisplayName("Should return empty Optional when ServiceTask not found by ID")
    void shouldReturnEmptyWhenNotFoundById() {
        // Arrange
        repository.clear();
        
        // Act
        Optional<ServiceTask> found = repository.findById(999L);
        
        // Assert
        assertFalse(found.isPresent());
    }
    
    @Test
    @DisplayName("Should return empty Optional when finding by null ID")
    void shouldReturnEmptyWhenFindingByNullId() {
        // Act
        Optional<ServiceTask> found = repository.findById(null);
        
        // Assert
        assertFalse(found.isPresent());
    }
    
    @Test
    @DisplayName("Should find all ServiceTasks")
    void shouldFindAllServiceTasks() {
        // Arrange
        repository.clear();
        ServiceTask task1 = ServiceTask.createServiceTask(
                "Task 1", "Desc 1", "Addr 1", Priority.LOW, 60);
        ServiceTask task2 = ServiceTask.createServiceTask(
                "Task 2", "Desc 2", "Addr 2", Priority.MEDIUM, 90);
        ServiceTask task3 = ServiceTask.createServiceTask(
                "Task 3", "Desc 3", "Addr 3", Priority.HIGH, 120);
        
        repository.save(task1);
        repository.save(task2);
        repository.save(task3);
        
        // Act
        List<ServiceTask> all = repository.findAll();
        
        // Assert
        assertNotNull(all);
        assertEquals(3, all.size());
    }
    
    @Test
    @DisplayName("Should return empty list when no ServiceTasks exist")
    void shouldReturnEmptyListWhenNoTasks() {
        // Arrange
        repository.clear();
        
        // Act
        List<ServiceTask> all = repository.findAll();
        
        // Assert
        assertNotNull(all);
        assertEquals(0, all.size());
        assertTrue(all.isEmpty());
    }
    
    @Test
    @DisplayName("Should delete ServiceTask by ID")
    void shouldDeleteServiceTaskById() {
        // Arrange
        repository.clear();
        ServiceTask task = ServiceTask.createServiceTask(
                "Delete Me", "Description", "Address", Priority.LOW, 60);
        ServiceTask saved = repository.save(task);
        Long taskId = saved.getId();
        
        // Act
        boolean deleted = repository.delete(taskId);
        
        // Assert
        assertTrue(deleted);
        assertEquals(0, repository.size());
        assertFalse(repository.findById(taskId).isPresent());
    }
    
    @Test
    @DisplayName("Should return false when deleting non-existent ServiceTask")
    void shouldReturnFalseWhenDeletingNonExistent() {
        // Arrange
        repository.clear();
        
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
    @DisplayName("Should clear all data from repository")
    void shouldClearAllData() {
        // Arrange - repository has mock data
        assertTrue(repository.size() > 0);
        
        // Act
        repository.clear();
        
        // Assert
        assertEquals(0, repository.size());
        assertTrue(repository.findAll().isEmpty());
    }
    
    @Test
    @DisplayName("Should maintain aggregate integrity when saving")
    void shouldMaintainAggregateIntegrity() {
        // Arrange
        repository.clear();
        LocalDateTime createdTime = LocalDateTime.now();
        ServiceTask task = ServiceTask.builder()
                .title("Integrity Test")
                .description("Testing aggregate integrity")
                .clientAddress("123 Test St")
                .priority(Priority.HIGH)
                .estimatedDuration(120)
                .status(TaskStatus.ASSIGNED)
                .createdAt(createdTime)
                .build();
        
        // Act
        ServiceTask saved = repository.save(task);
        Optional<ServiceTask> retrieved = repository.findById(saved.getId());
        
        // Assert
        assertTrue(retrieved.isPresent());
        ServiceTask retrievedTask = retrieved.get();
        assertEquals("Integrity Test", retrievedTask.getTitle());
        assertEquals("Testing aggregate integrity", retrievedTask.getDescription());
        assertEquals("123 Test St", retrievedTask.getClientAddress());
        assertEquals(Priority.HIGH, retrievedTask.getPriority());
        assertEquals(120, retrievedTask.getEstimatedDuration());
        assertEquals(TaskStatus.ASSIGNED, retrievedTask.getStatus());
        assertEquals(createdTime, retrievedTask.getCreatedAt());
    }
    
    @Test
    @DisplayName("Should generate sequential IDs for new ServiceTasks")
    void shouldGenerateSequentialIds() {
        // Arrange
        repository.clear();
        ServiceTask task1 = ServiceTask.createServiceTask(
                "Task 1", "Desc", "Addr", Priority.LOW, 60);
        ServiceTask task2 = ServiceTask.createServiceTask(
                "Task 2", "Desc", "Addr", Priority.LOW, 60);
        ServiceTask task3 = ServiceTask.createServiceTask(
                "Task 3", "Desc", "Addr", Priority.LOW, 60);
        
        // Act
        ServiceTask saved1 = repository.save(task1);
        ServiceTask saved2 = repository.save(task2);
        ServiceTask saved3 = repository.save(task3);
        
        // Assert
        assertNotNull(saved1.getId());
        assertNotNull(saved2.getId());
        assertNotNull(saved3.getId());
        assertEquals(saved1.getId() + 1, saved2.getId());
        assertEquals(saved2.getId() + 1, saved3.getId());
    }
    
    @Test
    @DisplayName("Should handle multiple saves and deletes correctly")
    void shouldHandleMultipleSavesAndDeletes() {
        // Arrange
        repository.clear();
        ServiceTask task1 = ServiceTask.createServiceTask(
                "Task 1", "Desc", "Addr", Priority.LOW, 60);
        ServiceTask task2 = ServiceTask.createServiceTask(
                "Task 2", "Desc", "Addr", Priority.LOW, 60);
        
        // Act
        ServiceTask saved1 = repository.save(task1);
        ServiceTask saved2 = repository.save(task2);
        assertEquals(2, repository.size());
        
        repository.delete(saved1.getId());
        assertEquals(1, repository.size());
        
        ServiceTask task3 = ServiceTask.createServiceTask(
                "Task 3", "Desc", "Addr", Priority.LOW, 60);
        repository.save(task3);
        assertEquals(2, repository.size());
        
        // Assert
        List<ServiceTask> all = repository.findAll();
        assertEquals(2, all.size());
        assertFalse(repository.findById(saved1.getId()).isPresent());
        assertTrue(repository.findById(saved2.getId()).isPresent());
    }
    
    @Test
    @DisplayName("Should preserve createdAt when saving new task without it")
    void shouldSetCreatedAtWhenSavingNewTask() {
        // Arrange
        repository.clear();
        ServiceTask task = ServiceTask.builder()
                .title("Task without createdAt")
                .description("Description")
                .clientAddress("Address")
                .priority(Priority.MEDIUM)
                .estimatedDuration(90)
                .status(TaskStatus.UNASSIGNED)
                .build();
        
        // Act
        ServiceTask saved = repository.save(task);
        
        // Assert
        assertNotNull(saved.getCreatedAt());
    }
    
    @Test
    @DisplayName("Should handle saving task with all priority levels")
    void shouldHandleSavingTaskWithAllPriorityLevels() {
        // Arrange
        repository.clear();
        
        // Act & Assert
        for (Priority priority : Priority.values()) {
            ServiceTask task = ServiceTask.createServiceTask(
                    "Task " + priority, "Desc", "Addr", priority, 60);
            ServiceTask saved = repository.save(task);
            assertNotNull(saved.getId());
            assertEquals(priority, saved.getPriority());
        }
        
        assertEquals(4, repository.size());
    }
    
    @Test
    @DisplayName("Should handle saving task with all status values")
    void shouldHandleSavingTaskWithAllStatusValues() {
        // Arrange
        repository.clear();
        
        // Act & Assert
        for (TaskStatus status : TaskStatus.values()) {
            ServiceTask task = ServiceTask.builder()
                    .title("Task " + status)
                    .description("Desc")
                    .clientAddress("Addr")
                    .priority(Priority.LOW)
                    .estimatedDuration(60)
                    .status(status)
                    .createdAt(LocalDateTime.now())
                    .build();
            ServiceTask saved = repository.save(task);
            assertNotNull(saved.getId());
            assertEquals(status, saved.getStatus());
        }
        
        assertEquals(4, repository.size());
    }
}
