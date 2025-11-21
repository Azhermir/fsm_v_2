package com.fsm.notification.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NotificationChannel enum
 */
class NotificationChannelTest {
    
    @Test
    void testEnumValues() {
        // Verify all expected enum values exist
        assertEquals(3, NotificationChannel.values().length);
        assertNotNull(NotificationChannel.valueOf("EMAIL"));
        assertNotNull(NotificationChannel.valueOf("SMS"));
        assertNotNull(NotificationChannel.valueOf("PUSH"));
    }
    
    @Test
    void testEnumToString() {
        assertEquals("EMAIL", NotificationChannel.EMAIL.toString());
        assertEquals("SMS", NotificationChannel.SMS.toString());
        assertEquals("PUSH", NotificationChannel.PUSH.toString());
    }
    
    @Test
    void testInvalidEnumValue() {
        assertThrows(IllegalArgumentException.class, () -> 
            NotificationChannel.valueOf("INVALID_CHANNEL")
        );
    }
}
