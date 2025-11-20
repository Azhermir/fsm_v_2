package com.fsm.task.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TaskStatus enum
 */
class TaskStatusTest {
    
    @Test
    @DisplayName("Should have all expected status values")
    void shouldHaveAllExpectedStatusValues() {
        TaskStatus[] statuses = TaskStatus.values();
        
        assertEquals(4, statuses.length);
        assertEquals(TaskStatus.UNASSIGNED, statuses[0]);
        assertEquals(TaskStatus.ASSIGNED, statuses[1]);
        assertEquals(TaskStatus.IN_PROGRESS, statuses[2]);
        assertEquals(TaskStatus.COMPLETED, statuses[3]);
    }
    
    @Test
    @DisplayName("Should convert string to status enum")
    void shouldConvertStringToStatusEnum() {
        assertEquals(TaskStatus.UNASSIGNED, TaskStatus.valueOf("UNASSIGNED"));
        assertEquals(TaskStatus.ASSIGNED, TaskStatus.valueOf("ASSIGNED"));
        assertEquals(TaskStatus.IN_PROGRESS, TaskStatus.valueOf("IN_PROGRESS"));
        assertEquals(TaskStatus.COMPLETED, TaskStatus.valueOf("COMPLETED"));
    }
    
    @Test
    @DisplayName("Should throw exception for invalid status value")
    void shouldThrowExceptionForInvalidStatusValue() {
        assertThrows(IllegalArgumentException.class, () -> TaskStatus.valueOf("INVALID"));
    }
}
