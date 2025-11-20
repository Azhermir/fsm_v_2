package com.fsm.task.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Priority enum
 */
class PriorityTest {
    
    @Test
    @DisplayName("Should have all expected priority values")
    void shouldHaveAllExpectedPriorityValues() {
        Priority[] priorities = Priority.values();
        
        assertEquals(4, priorities.length);
        assertEquals(Priority.LOW, priorities[0]);
        assertEquals(Priority.MEDIUM, priorities[1]);
        assertEquals(Priority.HIGH, priorities[2]);
        assertEquals(Priority.CRITICAL, priorities[3]);
    }
    
    @Test
    @DisplayName("Should convert string to priority enum")
    void shouldConvertStringToPriorityEnum() {
        assertEquals(Priority.LOW, Priority.valueOf("LOW"));
        assertEquals(Priority.MEDIUM, Priority.valueOf("MEDIUM"));
        assertEquals(Priority.HIGH, Priority.valueOf("HIGH"));
        assertEquals(Priority.CRITICAL, Priority.valueOf("CRITICAL"));
    }
    
    @Test
    @DisplayName("Should throw exception for invalid priority value")
    void shouldThrowExceptionForInvalidPriorityValue() {
        assertThrows(IllegalArgumentException.class, () -> Priority.valueOf("INVALID"));
    }
}
