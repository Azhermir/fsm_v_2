package com.fsm.task.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for TaskAssignment entity
 */
class TaskAssignmentTest {
    
    @Test
    void testCreateAssignment_WithValidData_CreatesAssignment() {
        // Arrange
        Long taskId = 1L;
        Long technicianId = 2L;
        String assignedBy = "dispatcher1";
        
        // Act
        TaskAssignment assignment = TaskAssignment.createAssignment(taskId, technicianId, assignedBy);
        
        // Assert
        assertThat(assignment).isNotNull();
        assertThat(assignment.getTaskId()).isEqualTo(taskId);
        assertThat(assignment.getTechnicianId()).isEqualTo(technicianId);
        assertThat(assignment.getAssignedBy()).isEqualTo(assignedBy);
        assertThat(assignment.getAssignedAt()).isNotNull();
        assertThat(assignment.getAssignedAt()).isBefore(LocalDateTime.now().plusSeconds(1));
    }
    
    @Test
    void testCreateAssignment_WithNullTaskId_ThrowsException() {
        // Arrange
        Long technicianId = 2L;
        String assignedBy = "dispatcher1";
        
        // Act & Assert
        assertThatThrownBy(() -> TaskAssignment.createAssignment(null, technicianId, assignedBy))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Task ID must not be null");
    }
    
    @Test
    void testCreateAssignment_WithNullTechnicianId_ThrowsException() {
        // Arrange
        Long taskId = 1L;
        String assignedBy = "dispatcher1";
        
        // Act & Assert
        assertThatThrownBy(() -> TaskAssignment.createAssignment(taskId, null, assignedBy))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Technician ID must not be null");
    }
    
    @Test
    void testCreateAssignment_WithNullAssignedBy_ThrowsException() {
        // Arrange
        Long taskId = 1L;
        Long technicianId = 2L;
        
        // Act & Assert
        assertThatThrownBy(() -> TaskAssignment.createAssignment(taskId, technicianId, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Assigned by must not be blank");
    }
    
    @Test
    void testCreateAssignment_WithBlankAssignedBy_ThrowsException() {
        // Arrange
        Long taskId = 1L;
        Long technicianId = 2L;
        
        // Act & Assert
        assertThatThrownBy(() -> TaskAssignment.createAssignment(taskId, technicianId, "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Assigned by must not be blank");
    }
    
    @Test
    void testBuilder_CreatesAssignmentWithAllFields() {
        // Arrange
        LocalDateTime assignedAt = LocalDateTime.now();
        
        // Act
        TaskAssignment assignment = TaskAssignment.builder()
                .id(1L)
                .taskId(10L)
                .technicianId(20L)
                .assignedAt(assignedAt)
                .assignedBy("dispatcher1")
                .build();
        
        // Assert
        assertThat(assignment.getId()).isEqualTo(1L);
        assertThat(assignment.getTaskId()).isEqualTo(10L);
        assertThat(assignment.getTechnicianId()).isEqualTo(20L);
        assertThat(assignment.getAssignedAt()).isEqualTo(assignedAt);
        assertThat(assignment.getAssignedBy()).isEqualTo("dispatcher1");
    }
    
    @Test
    void testSettersAndGetters() {
        // Arrange
        TaskAssignment assignment = new TaskAssignment();
        LocalDateTime assignedAt = LocalDateTime.now();
        
        // Act
        assignment.setId(1L);
        assignment.setTaskId(10L);
        assignment.setTechnicianId(20L);
        assignment.setAssignedAt(assignedAt);
        assignment.setAssignedBy("dispatcher1");
        
        // Assert
        assertThat(assignment.getId()).isEqualTo(1L);
        assertThat(assignment.getTaskId()).isEqualTo(10L);
        assertThat(assignment.getTechnicianId()).isEqualTo(20L);
        assertThat(assignment.getAssignedAt()).isEqualTo(assignedAt);
        assertThat(assignment.getAssignedBy()).isEqualTo("dispatcher1");
    }
    
    @Test
    void testOnCreate_SetsAssignedAtIfNull() {
        // Arrange
        TaskAssignment assignment = TaskAssignment.builder()
                .taskId(1L)
                .technicianId(2L)
                .assignedBy("dispatcher1")
                .build();
        
        // Act
        assignment.onCreate();
        
        // Assert
        assertThat(assignment.getAssignedAt()).isNotNull();
        assertThat(assignment.getAssignedAt()).isBefore(LocalDateTime.now().plusSeconds(1));
    }
    
    @Test
    void testOnCreate_DoesNotOverrideExistingAssignedAt() {
        // Arrange
        LocalDateTime existingTime = LocalDateTime.of(2025, 1, 1, 10, 0);
        TaskAssignment assignment = TaskAssignment.builder()
                .taskId(1L)
                .technicianId(2L)
                .assignedBy("dispatcher1")
                .assignedAt(existingTime)
                .build();
        
        // Act
        assignment.onCreate();
        
        // Assert
        assertThat(assignment.getAssignedAt()).isEqualTo(existingTime);
    }
}
