package com.fsm.notification.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NotificationType enum
 */
class NotificationTypeTest {
    
    @Test
    void testEnumValues() {
        // Verify all expected enum values exist
        assertEquals(3, NotificationType.values().length);
        assertNotNull(NotificationType.valueOf("TASK_ASSIGNED"));
        assertNotNull(NotificationType.valueOf("TASK_STATUS_UPDATE"));
        assertNotNull(NotificationType.valueOf("TASK_COMPLETED"));
    }
    
    @Test
    void testEnumToString() {
        assertEquals("TASK_ASSIGNED", NotificationType.TASK_ASSIGNED.toString());
        assertEquals("TASK_STATUS_UPDATE", NotificationType.TASK_STATUS_UPDATE.toString());
        assertEquals("TASK_COMPLETED", NotificationType.TASK_COMPLETED.toString());
    }
    
    @Test
    void testInvalidEnumValue() {
        assertThrows(IllegalArgumentException.class, () -> 
            NotificationType.valueOf("INVALID_TYPE")
        );
    }
}
