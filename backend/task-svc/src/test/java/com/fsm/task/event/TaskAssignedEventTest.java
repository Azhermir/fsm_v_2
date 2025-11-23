package com.fsm.task.event;

import com.fsm.task.domain.Priority;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TaskAssignedEvent
 */
class TaskAssignedEventTest {
    
    @Test
    void testBuilder() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        // When
        TaskAssignedEvent event = TaskAssignedEvent.builder()
                .taskId(1L)
                .title("Fix plumbing")
                .description("Repair leaking pipe")
                .clientAddress("123 Main St")
                .latitude(40.7128)
                .longitude(-74.0060)
                .priority(Priority.HIGH)
                .estimatedDuration(60)
                .technicianId(100L)
                .customerId(200L)
                .assignedAt(now)
                .assignedBy("dispatcher")
                .build();
        
        // Then
        assertNotNull(event);
        assertEquals(1L, event.getTaskId());
        assertEquals("Fix plumbing", event.getTitle());
        assertEquals("Repair leaking pipe", event.getDescription());
        assertEquals("123 Main St", event.getClientAddress());
        assertEquals(40.7128, event.getLatitude());
        assertEquals(-74.0060, event.getLongitude());
        assertEquals(Priority.HIGH, event.getPriority());
        assertEquals(60, event.getEstimatedDuration());
        assertEquals(100L, event.getTechnicianId());
        assertEquals(200L, event.getCustomerId());
        assertEquals(now, event.getAssignedAt());
        assertEquals("dispatcher", event.getAssignedBy());
    }
    
    @Test
    void testNoArgsConstructor() {
        // When
        TaskAssignedEvent event = new TaskAssignedEvent();
        
        // Then
        assertNotNull(event);
    }
    
    @Test
    void testAllArgsConstructor() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        // When
        TaskAssignedEvent event = new TaskAssignedEvent(
                1L, "Title", "Description", "Address",
                40.0, -74.0, Priority.MEDIUM, 90,
                100L, 200L, now, "system"
        );
        
        // Then
        assertEquals(1L, event.getTaskId());
        assertEquals("Title", event.getTitle());
        assertEquals("Description", event.getDescription());
        assertEquals("Address", event.getClientAddress());
        assertEquals(40.0, event.getLatitude());
        assertEquals(-74.0, event.getLongitude());
        assertEquals(Priority.MEDIUM, event.getPriority());
        assertEquals(90, event.getEstimatedDuration());
        assertEquals(100L, event.getTechnicianId());
        assertEquals(200L, event.getCustomerId());
        assertEquals(now, event.getAssignedAt());
        assertEquals("system", event.getAssignedBy());
    }
    
    @Test
    void testSettersAndGetters() {
        // Given
        TaskAssignedEvent event = new TaskAssignedEvent();
        LocalDateTime now = LocalDateTime.now();
        
        // When
        event.setTaskId(1L);
        event.setTitle("Title");
        event.setDescription("Description");
        event.setClientAddress("Address");
        event.setLatitude(40.0);
        event.setLongitude(-74.0);
        event.setPriority(Priority.LOW);
        event.setEstimatedDuration(120);
        event.setTechnicianId(100L);
        event.setCustomerId(200L);
        event.setAssignedAt(now);
        event.setAssignedBy("admin");
        
        // Then
        assertEquals(1L, event.getTaskId());
        assertEquals("Title", event.getTitle());
        assertEquals("Description", event.getDescription());
        assertEquals("Address", event.getClientAddress());
        assertEquals(40.0, event.getLatitude());
        assertEquals(-74.0, event.getLongitude());
        assertEquals(Priority.LOW, event.getPriority());
        assertEquals(120, event.getEstimatedDuration());
        assertEquals(100L, event.getTechnicianId());
        assertEquals(200L, event.getCustomerId());
        assertEquals(now, event.getAssignedAt());
        assertEquals("admin", event.getAssignedBy());
    }
    
    @Test
    void testWithNullCoordinates() {
        // When
        TaskAssignedEvent event = TaskAssignedEvent.builder()
                .taskId(1L)
                .title("Task")
                .description("Desc")
                .clientAddress("Address")
                .latitude(null)
                .longitude(null)
                .priority(Priority.MEDIUM)
                .estimatedDuration(60)
                .technicianId(100L)
                .customerId(200L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("system")
                .build();
        
        // Then
        assertNotNull(event);
        assertNull(event.getLatitude());
        assertNull(event.getLongitude());
    }
}
