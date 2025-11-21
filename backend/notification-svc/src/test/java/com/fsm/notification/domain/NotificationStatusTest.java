package com.fsm.notification.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NotificationStatus enum
 */
class NotificationStatusTest {
    
    @Test
    void testEnumValues() {
        // Verify all expected enum values exist
        assertEquals(3, NotificationStatus.values().length);
        assertNotNull(NotificationStatus.valueOf("PENDING"));
        assertNotNull(NotificationStatus.valueOf("SENT"));
        assertNotNull(NotificationStatus.valueOf("FAILED"));
    }
    
    @Test
    void testEnumToString() {
        assertEquals("PENDING", NotificationStatus.PENDING.toString());
        assertEquals("SENT", NotificationStatus.SENT.toString());
        assertEquals("FAILED", NotificationStatus.FAILED.toString());
    }
    
    @Test
    void testInvalidEnumValue() {
        assertThrows(IllegalArgumentException.class, () -> 
            NotificationStatus.valueOf("INVALID_STATUS")
        );
    }
}
