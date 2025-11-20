package com.fsm.taskmanagement.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ServiceTask Entity Tests")
class ServiceTaskTest {

    @Test
    @DisplayName("Should create ServiceTask with all required properties")
    void shouldCreateServiceTaskWithAllProperties() {
        // Given
        UUID id = UUID.randomUUID();
        String title = "Fix HVAC System";
        String description = "Repair broken air conditioning unit";
        String clientAddress = "123 Main St, City, State";
        Priority priority = Priority.HIGH;
        Integer estimatedDuration = 120;
        Status status = Status.UNASSIGNED;
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

        // When
        ServiceTask task = ServiceTask.builder()
                .id(id)
                .title(title)
                .description(description)
                .clientAddress(clientAddress)
                .priority(priority)
                .estimatedDuration(estimatedDuration)
                .status(status)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        // Then
        assertNotNull(task);
        assertEquals(id, task.getId());
        assertEquals(title, task.getTitle());
        assertEquals(description, task.getDescription());
        assertEquals(clientAddress, task.getClientAddress());
        assertEquals(priority, task.getPriority());
        assertEquals(estimatedDuration, task.getEstimatedDuration());
        assertEquals(status, task.getStatus());
        assertEquals(createdAt, task.getCreatedAt());
        assertEquals(updatedAt, task.getUpdatedAt());
    }

    @Test
    @DisplayName("Should create ServiceTask with default priority MEDIUM")
    void shouldCreateServiceTaskWithDefaultPriority() {
        // When
        ServiceTask task = ServiceTask.builder()
                .title("Test Task")
                .description("Test Description")
                .clientAddress("Test Address")
                .build();

        // Then
        assertEquals(Priority.MEDIUM, task.getPriority());
    }

    @Test
    @DisplayName("Should create ServiceTask with default status UNASSIGNED")
    void shouldCreateServiceTaskWithDefaultStatus() {
        // When
        ServiceTask task = ServiceTask.builder()
                .title("Test Task")
                .description("Test Description")
                .clientAddress("Test Address")
                .build();

        // Then
        assertEquals(Status.UNASSIGNED, task.getStatus());
    }

    @Test
    @DisplayName("Should set timestamps on persist")
    void shouldSetTimestampsOnPersist() {
        // Given
        ServiceTask task = ServiceTask.builder()
                .title("Test Task")
                .description("Test Description")
                .clientAddress("Test Address")
                .build();

        // When
        task.onCreate();

        // Then
        assertNotNull(task.getCreatedAt());
        assertNotNull(task.getUpdatedAt());
        assertTrue(task.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(task.getUpdatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    @DisplayName("Should update timestamp on update")
    void shouldUpdateTimestampOnUpdate() throws InterruptedException {
        // Given
        ServiceTask task = ServiceTask.builder()
                .title("Test Task")
                .description("Test Description")
                .clientAddress("Test Address")
                .build();
        task.onCreate();
        LocalDateTime originalUpdatedAt = task.getUpdatedAt();

        // Wait a bit to ensure timestamp difference
        Thread.sleep(10);

        // When
        task.onUpdate();

        // Then
        assertNotNull(task.getUpdatedAt());
        assertTrue(task.getUpdatedAt().isAfter(originalUpdatedAt));
    }

    @Test
    @DisplayName("Should validate and pass with valid data")
    void shouldValidateWithValidData() {
        // Given
        ServiceTask task = ServiceTask.builder()
                .title("Valid Title")
                .description("Valid Description")
                .clientAddress("Valid Address")
                .priority(Priority.HIGH)
                .status(Status.ASSIGNED)
                .build();

        // When/Then
        assertDoesNotThrow(task::validate);
    }

    @Test
    @DisplayName("Should throw exception when title is null")
    void shouldThrowExceptionWhenTitleIsNull() {
        // Given
        ServiceTask task = ServiceTask.builder()
                .title(null)
                .description("Valid Description")
                .clientAddress("Valid Address")
                .priority(Priority.HIGH)
                .status(Status.ASSIGNED)
                .build();

        // When/Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                task::validate
        );
        assertEquals("Title must not be empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when title is empty")
    void shouldThrowExceptionWhenTitleIsEmpty() {
        // Given
        ServiceTask task = ServiceTask.builder()
                .title("")
                .description("Valid Description")
                .clientAddress("Valid Address")
                .priority(Priority.HIGH)
                .status(Status.ASSIGNED)
                .build();

        // When/Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                task::validate
        );
        assertEquals("Title must not be empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when title is blank")
    void shouldThrowExceptionWhenTitleIsBlank() {
        // Given
        ServiceTask task = ServiceTask.builder()
                .title("   ")
                .description("Valid Description")
                .clientAddress("Valid Address")
                .priority(Priority.HIGH)
                .status(Status.ASSIGNED)
                .build();

        // When/Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                task::validate
        );
        assertEquals("Title must not be empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when description is null")
    void shouldThrowExceptionWhenDescriptionIsNull() {
        // Given
        ServiceTask task = ServiceTask.builder()
                .title("Valid Title")
                .description(null)
                .clientAddress("Valid Address")
                .priority(Priority.HIGH)
                .status(Status.ASSIGNED)
                .build();

        // When/Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                task::validate
        );
        assertEquals("Description must not be empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when description is empty")
    void shouldThrowExceptionWhenDescriptionIsEmpty() {
        // Given
        ServiceTask task = ServiceTask.builder()
                .title("Valid Title")
                .description("")
                .clientAddress("Valid Address")
                .priority(Priority.HIGH)
                .status(Status.ASSIGNED)
                .build();

        // When/Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                task::validate
        );
        assertEquals("Description must not be empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when clientAddress is null")
    void shouldThrowExceptionWhenClientAddressIsNull() {
        // Given
        ServiceTask task = ServiceTask.builder()
                .title("Valid Title")
                .description("Valid Description")
                .clientAddress(null)
                .priority(Priority.HIGH)
                .status(Status.ASSIGNED)
                .build();

        // When/Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                task::validate
        );
        assertEquals("Client address must not be empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when clientAddress is empty")
    void shouldThrowExceptionWhenClientAddressIsEmpty() {
        // Given
        ServiceTask task = ServiceTask.builder()
                .title("Valid Title")
                .description("Valid Description")
                .clientAddress("")
                .priority(Priority.HIGH)
                .status(Status.ASSIGNED)
                .build();

        // When/Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                task::validate
        );
        assertEquals("Client address must not be empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when priority is null")
    void shouldThrowExceptionWhenPriorityIsNull() {
        // Given
        ServiceTask task = new ServiceTask();
        task.setTitle("Valid Title");
        task.setDescription("Valid Description");
        task.setClientAddress("Valid Address");
        task.setPriority(null);
        task.setStatus(Status.ASSIGNED);

        // When/Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                task::validate
        );
        assertEquals("Priority must be specified", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when status is null")
    void shouldThrowExceptionWhenStatusIsNull() {
        // Given
        ServiceTask task = new ServiceTask();
        task.setTitle("Valid Title");
        task.setDescription("Valid Description");
        task.setClientAddress("Valid Address");
        task.setPriority(Priority.HIGH);
        task.setStatus(null);

        // When/Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                task::validate
        );
        assertEquals("Status must be specified", exception.getMessage());
    }

    @Test
    @DisplayName("Should accept all Priority enum values")
    void shouldAcceptAllPriorityEnumValues() {
        // Test all priority values
        for (Priority priority : Priority.values()) {
            ServiceTask task = ServiceTask.builder()
                    .title("Test Task")
                    .description("Test Description")
                    .clientAddress("Test Address")
                    .priority(priority)
                    .status(Status.ASSIGNED)
                    .build();

            assertDoesNotThrow(task::validate);
            assertEquals(priority, task.getPriority());
        }
    }

    @Test
    @DisplayName("Should accept all Status enum values")
    void shouldAcceptAllStatusEnumValues() {
        // Test all status values
        for (Status status : Status.values()) {
            ServiceTask task = ServiceTask.builder()
                    .title("Test Task")
                    .description("Test Description")
                    .clientAddress("Test Address")
                    .priority(Priority.MEDIUM)
                    .status(status)
                    .build();

            assertDoesNotThrow(task::validate);
            assertEquals(status, task.getStatus());
        }
    }

    @Test
    @DisplayName("Should set default values on onCreate when null")
    void shouldSetDefaultValuesOnCreate() {
        // Given
        ServiceTask task = new ServiceTask();
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setClientAddress("Test Address");

        // When
        task.onCreate();

        // Then
        assertEquals(Status.UNASSIGNED, task.getStatus());
        assertEquals(Priority.MEDIUM, task.getPriority());
        assertNotNull(task.getCreatedAt());
        assertNotNull(task.getUpdatedAt());
    }

    @Test
    @DisplayName("Should use Lombok builder correctly")
    void shouldUseLombokBuilderCorrectly() {
        // When
        ServiceTask task = ServiceTask.builder()
                .title("Test Task")
                .description("Test Description")
                .clientAddress("Test Address")
                .priority(Priority.CRITICAL)
                .estimatedDuration(90)
                .status(Status.IN_PROGRESS)
                .build();

        // Then
        assertNotNull(task);
        assertEquals("Test Task", task.getTitle());
        assertEquals("Test Description", task.getDescription());
        assertEquals("Test Address", task.getClientAddress());
        assertEquals(Priority.CRITICAL, task.getPriority());
        assertEquals(90, task.getEstimatedDuration());
        assertEquals(Status.IN_PROGRESS, task.getStatus());
    }

    @Test
    @DisplayName("Should use Lombok setters correctly")
    void shouldUseLombokSettersCorrectly() {
        // Given
        ServiceTask task = new ServiceTask();

        // When
        task.setTitle("Updated Title");
        task.setDescription("Updated Description");
        task.setClientAddress("Updated Address");
        task.setPriority(Priority.LOW);
        task.setEstimatedDuration(60);
        task.setStatus(Status.COMPLETED);

        // Then
        assertEquals("Updated Title", task.getTitle());
        assertEquals("Updated Description", task.getDescription());
        assertEquals("Updated Address", task.getClientAddress());
        assertEquals(Priority.LOW, task.getPriority());
        assertEquals(60, task.getEstimatedDuration());
        assertEquals(Status.COMPLETED, task.getStatus());
    }

    @Test
    @DisplayName("Should create ServiceTask with estimatedDuration")
    void shouldCreateServiceTaskWithEstimatedDuration() {
        // Given
        Integer duration = 180;

        // When
        ServiceTask task = ServiceTask.builder()
                .title("Test Task")
                .description("Test Description")
                .clientAddress("Test Address")
                .estimatedDuration(duration)
                .build();

        // Then
        assertEquals(duration, task.getEstimatedDuration());
    }

    @Test
    @DisplayName("Should allow null estimatedDuration")
    void shouldAllowNullEstimatedDuration() {
        // When
        ServiceTask task = ServiceTask.builder()
                .title("Test Task")
                .description("Test Description")
                .clientAddress("Test Address")
                .estimatedDuration(null)
                .build();

        // Then
        assertNull(task.getEstimatedDuration());
    }
}
