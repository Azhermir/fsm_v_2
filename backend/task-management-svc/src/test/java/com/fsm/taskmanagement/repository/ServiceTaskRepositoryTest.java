package com.fsm.taskmanagement.repository;

import com.fsm.taskmanagement.entity.Priority;
import com.fsm.taskmanagement.entity.ServiceTask;
import com.fsm.taskmanagement.entity.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayName("ServiceTaskRepository Integration Tests")
class ServiceTaskRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ServiceTaskRepository repository;

    private ServiceTask testTask;

    @BeforeEach
    void setUp() {
        testTask = ServiceTask.builder()
                .title("Test Task")
                .description("Test Description")
                .clientAddress("123 Test Street")
                .priority(Priority.HIGH)
                .estimatedDuration(120)
                .status(Status.UNASSIGNED)
                .build();
    }

    @Test
    @DisplayName("Should save ServiceTask to database")
    void shouldSaveServiceTask() {
        // When
        ServiceTask savedTask = repository.save(testTask);

        // Then
        assertNotNull(savedTask);
        assertNotNull(savedTask.getId());
        assertEquals(testTask.getTitle(), savedTask.getTitle());
        assertEquals(testTask.getDescription(), savedTask.getDescription());
        assertEquals(testTask.getClientAddress(), savedTask.getClientAddress());
        assertEquals(testTask.getPriority(), savedTask.getPriority());
        assertEquals(testTask.getEstimatedDuration(), savedTask.getEstimatedDuration());
        assertEquals(testTask.getStatus(), savedTask.getStatus());
        assertNotNull(savedTask.getCreatedAt());
        assertNotNull(savedTask.getUpdatedAt());
    }

    @Test
    @DisplayName("Should find ServiceTask by ID")
    void shouldFindServiceTaskById() {
        // Given
        ServiceTask savedTask = entityManager.persistAndFlush(testTask);
        UUID taskId = savedTask.getId();

        // When
        Optional<ServiceTask> foundTask = repository.findById(taskId);

        // Then
        assertTrue(foundTask.isPresent());
        assertEquals(savedTask.getId(), foundTask.get().getId());
        assertEquals(savedTask.getTitle(), foundTask.get().getTitle());
        assertEquals(savedTask.getDescription(), foundTask.get().getDescription());
    }

    @Test
    @DisplayName("Should return empty Optional when ServiceTask not found by ID")
    void shouldReturnEmptyWhenTaskNotFoundById() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        Optional<ServiceTask> foundTask = repository.findById(nonExistentId);

        // Then
        assertFalse(foundTask.isPresent());
    }

    @Test
    @DisplayName("Should find all ServiceTasks")
    void shouldFindAllServiceTasks() {
        // Given
        ServiceTask task1 = ServiceTask.builder()
                .title("Task 1")
                .description("Description 1")
                .clientAddress("Address 1")
                .priority(Priority.LOW)
                .build();

        ServiceTask task2 = ServiceTask.builder()
                .title("Task 2")
                .description("Description 2")
                .clientAddress("Address 2")
                .priority(Priority.MEDIUM)
                .build();

        ServiceTask task3 = ServiceTask.builder()
                .title("Task 3")
                .description("Description 3")
                .clientAddress("Address 3")
                .priority(Priority.CRITICAL)
                .build();

        entityManager.persist(task1);
        entityManager.persist(task2);
        entityManager.persist(task3);
        entityManager.flush();

        // When
        List<ServiceTask> allTasks = repository.findAll();

        // Then
        assertNotNull(allTasks);
        assertEquals(3, allTasks.size());
        assertThat(allTasks)
                .extracting(ServiceTask::getTitle)
                .containsExactlyInAnyOrder("Task 1", "Task 2", "Task 3");
    }

    @Test
    @DisplayName("Should return empty list when no ServiceTasks exist")
    void shouldReturnEmptyListWhenNoTasksExist() {
        // When
        List<ServiceTask> allTasks = repository.findAll();

        // Then
        assertNotNull(allTasks);
        assertTrue(allTasks.isEmpty());
    }

    @Test
    @DisplayName("Should update existing ServiceTask")
    void shouldUpdateExistingServiceTask() {
        // Given
        ServiceTask savedTask = entityManager.persistAndFlush(testTask);
        UUID taskId = savedTask.getId();

        // When
        savedTask.setTitle("Updated Title");
        savedTask.setDescription("Updated Description");
        savedTask.setPriority(Priority.CRITICAL);
        savedTask.setStatus(Status.IN_PROGRESS);
        ServiceTask updatedTask = repository.save(savedTask);
        entityManager.flush();

        // Then
        ServiceTask foundTask = entityManager.find(ServiceTask.class, taskId);
        assertNotNull(foundTask);
        assertEquals("Updated Title", foundTask.getTitle());
        assertEquals("Updated Description", foundTask.getDescription());
        assertEquals(Priority.CRITICAL, foundTask.getPriority());
        assertEquals(Status.IN_PROGRESS, foundTask.getStatus());
    }

    @Test
    @DisplayName("Should delete ServiceTask by ID")
    void shouldDeleteServiceTaskById() {
        // Given
        ServiceTask savedTask = entityManager.persistAndFlush(testTask);
        UUID taskId = savedTask.getId();

        // When
        repository.deleteById(taskId);
        entityManager.flush();

        // Then
        ServiceTask deletedTask = entityManager.find(ServiceTask.class, taskId);
        assertNull(deletedTask);
    }

    @Test
    @DisplayName("Should delete ServiceTask by entity")
    void shouldDeleteServiceTaskByEntity() {
        // Given
        ServiceTask savedTask = entityManager.persistAndFlush(testTask);
        UUID taskId = savedTask.getId();

        // When
        repository.delete(savedTask);
        entityManager.flush();

        // Then
        ServiceTask deletedTask = entityManager.find(ServiceTask.class, taskId);
        assertNull(deletedTask);
    }

    @Test
    @DisplayName("Should count ServiceTasks")
    void shouldCountServiceTasks() {
        // Given
        ServiceTask task1 = ServiceTask.builder()
                .title("Task 1")
                .description("Description 1")
                .clientAddress("Address 1")
                .build();

        ServiceTask task2 = ServiceTask.builder()
                .title("Task 2")
                .description("Description 2")
                .clientAddress("Address 2")
                .build();

        entityManager.persist(task1);
        entityManager.persist(task2);
        entityManager.flush();

        // When
        long count = repository.count();

        // Then
        assertEquals(2, count);
    }

    @Test
    @DisplayName("Should check if ServiceTask exists by ID")
    void shouldCheckIfServiceTaskExistsById() {
        // Given
        ServiceTask savedTask = entityManager.persistAndFlush(testTask);
        UUID taskId = savedTask.getId();

        // When
        boolean exists = repository.existsById(taskId);

        // Then
        assertTrue(exists);
    }

    @Test
    @DisplayName("Should return false when checking non-existent ServiceTask")
    void shouldReturnFalseForNonExistentTask() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        boolean exists = repository.existsById(nonExistentId);

        // Then
        assertFalse(exists);
    }

    @Test
    @DisplayName("Should persist ServiceTask with all enum values")
    void shouldPersistServiceTaskWithAllEnumValues() {
        // Test all combinations of Priority and Status
        for (Priority priority : Priority.values()) {
            for (Status status : Status.values()) {
                ServiceTask task = ServiceTask.builder()
                        .title("Task " + priority + " " + status)
                        .description("Description")
                        .clientAddress("Address")
                        .priority(priority)
                        .status(status)
                        .build();

                ServiceTask savedTask = repository.save(task);
                entityManager.flush();

                assertNotNull(savedTask.getId());
                assertEquals(priority, savedTask.getPriority());
                assertEquals(status, savedTask.getStatus());
            }
        }
    }

    @Test
    @DisplayName("Should persist ServiceTask with null estimatedDuration")
    void shouldPersistServiceTaskWithNullEstimatedDuration() {
        // Given
        ServiceTask task = ServiceTask.builder()
                .title("Task without duration")
                .description("Description")
                .clientAddress("Address")
                .priority(Priority.LOW)
                .estimatedDuration(null)
                .build();

        // When
        ServiceTask savedTask = repository.save(task);
        entityManager.flush();

        // Then
        assertNotNull(savedTask.getId());
        assertNull(savedTask.getEstimatedDuration());
    }

    @Test
    @DisplayName("Should maintain data consistency across save and retrieve operations")
    void shouldMaintainDataConsistency() {
        // Given
        ServiceTask originalTask = ServiceTask.builder()
                .title("Original Task")
                .description("Original Description")
                .clientAddress("Original Address")
                .priority(Priority.HIGH)
                .estimatedDuration(180)
                .status(Status.ASSIGNED)
                .build();

        // When
        ServiceTask savedTask = repository.save(originalTask);
        entityManager.flush();
        entityManager.clear(); // Clear persistence context to force reload from DB

        Optional<ServiceTask> retrievedTask = repository.findById(savedTask.getId());

        // Then
        assertTrue(retrievedTask.isPresent());
        ServiceTask task = retrievedTask.get();
        assertEquals(originalTask.getTitle(), task.getTitle());
        assertEquals(originalTask.getDescription(), task.getDescription());
        assertEquals(originalTask.getClientAddress(), task.getClientAddress());
        assertEquals(originalTask.getPriority(), task.getPriority());
        assertEquals(originalTask.getEstimatedDuration(), task.getEstimatedDuration());
        assertEquals(originalTask.getStatus(), task.getStatus());
    }

    @Test
    @DisplayName("Should handle multiple save operations on same entity")
    void shouldHandleMultipleSaveOperations() {
        // Given
        ServiceTask task = repository.save(testTask);
        UUID originalId = task.getId();

        // When - Multiple updates
        task.setTitle("First Update");
        repository.save(task);

        task.setTitle("Second Update");
        repository.save(task);

        task.setTitle("Third Update");
        ServiceTask finalTask = repository.save(task);
        entityManager.flush();

        // Then
        assertEquals(originalId, finalTask.getId());
        assertEquals("Third Update", finalTask.getTitle());

        // Verify in database
        Optional<ServiceTask> dbTask = repository.findById(originalId);
        assertTrue(dbTask.isPresent());
        assertEquals("Third Update", dbTask.get().getTitle());
    }

    @Test
    @DisplayName("Should save and retrieve ServiceTask with default values")
    void shouldSaveAndRetrieveServiceTaskWithDefaults() {
        // Given
        ServiceTask task = ServiceTask.builder()
                .title("Task with defaults")
                .description("Description")
                .clientAddress("Address")
                .build(); // Using default priority and status

        // When
        ServiceTask savedTask = repository.save(task);
        entityManager.flush();
        entityManager.clear();

        Optional<ServiceTask> retrievedTask = repository.findById(savedTask.getId());

        // Then
        assertTrue(retrievedTask.isPresent());
        assertEquals(Priority.MEDIUM, retrievedTask.get().getPriority());
        assertEquals(Status.UNASSIGNED, retrievedTask.get().getStatus());
    }
}
