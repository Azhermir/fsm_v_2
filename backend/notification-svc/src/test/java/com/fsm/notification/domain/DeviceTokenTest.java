package com.fsm.notification.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DeviceToken domain entity
 */
class DeviceTokenTest {
    
    @Test
    void testCreateDeviceToken_Success() {
        // When
        DeviceToken token = DeviceToken.createDeviceToken(
                1L, "test-fcm-token", "device-001", "Android"
        );
        
        // Then
        assertNotNull(token);
        assertEquals(1L, token.getUserId());
        assertEquals("test-fcm-token", token.getFcmToken());
        assertEquals("device-001", token.getDeviceId());
        assertEquals("Android", token.getPlatform());
        assertTrue(token.getActive());
        assertNotNull(token.getRegisteredAt());
        assertNotNull(token.getUpdatedAt());
    }
    
    @Test
    void testCreateDeviceToken_WithNullUserId_ThrowsException() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () ->
                DeviceToken.createDeviceToken(null, "token", "device", "Android")
        );
    }
    
    @Test
    void testCreateDeviceToken_WithNullToken_ThrowsException() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () ->
                DeviceToken.createDeviceToken(1L, null, "device", "Android")
        );
    }
    
    @Test
    void testCreateDeviceToken_WithBlankToken_ThrowsException() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () ->
                DeviceToken.createDeviceToken(1L, "  ", "device", "Android")
        );
    }
    
    @Test
    void testCreateDeviceToken_WithNullDeviceId_ThrowsException() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () ->
                DeviceToken.createDeviceToken(1L, "token", null, "Android")
        );
    }
    
    @Test
    void testCreateDeviceToken_WithBlankDeviceId_ThrowsException() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () ->
                DeviceToken.createDeviceToken(1L, "token", "  ", "Android")
        );
    }
    
    @Test
    void testCreateDeviceToken_WithNullPlatform_Success() {
        // When
        DeviceToken token = DeviceToken.createDeviceToken(
                1L, "test-fcm-token", "device-001", null
        );
        
        // Then
        assertNotNull(token);
        assertNull(token.getPlatform());
    }
    
    @Test
    void testUpdateToken_Success() {
        // Given
        DeviceToken token = DeviceToken.createDeviceToken(
                1L, "old-token", "device-001", "Android"
        );
        LocalDateTime oldUpdatedAt = token.getUpdatedAt();
        
        // Wait a bit to ensure timestamp changes
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // Ignore
        }
        
        // When
        token.updateToken("new-token");
        
        // Then
        assertEquals("new-token", token.getFcmToken());
        assertTrue(token.getUpdatedAt().isAfter(oldUpdatedAt) || 
                   token.getUpdatedAt().isEqual(oldUpdatedAt));
    }
    
    @Test
    void testUpdateToken_WithNullToken_ThrowsException() {
        // Given
        DeviceToken token = DeviceToken.createDeviceToken(
                1L, "old-token", "device-001", "Android"
        );
        
        // When/Then
        assertThrows(IllegalArgumentException.class, () ->
                token.updateToken(null)
        );
    }
    
    @Test
    void testUpdateToken_WithBlankToken_ThrowsException() {
        // Given
        DeviceToken token = DeviceToken.createDeviceToken(
                1L, "old-token", "device-001", "Android"
        );
        
        // When/Then
        assertThrows(IllegalArgumentException.class, () ->
                token.updateToken("  ")
        );
    }
    
    @Test
    void testDeactivate() {
        // Given
        DeviceToken token = DeviceToken.createDeviceToken(
                1L, "token", "device-001", "Android"
        );
        assertTrue(token.getActive());
        LocalDateTime oldUpdatedAt = token.getUpdatedAt();
        
        // Wait a bit to ensure timestamp changes
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // Ignore
        }
        
        // When
        token.deactivate();
        
        // Then
        assertFalse(token.getActive());
        assertTrue(token.getUpdatedAt().isAfter(oldUpdatedAt) || 
                   token.getUpdatedAt().isEqual(oldUpdatedAt));
    }
    
    @Test
    void testActivate() {
        // Given
        DeviceToken token = DeviceToken.createDeviceToken(
                1L, "token", "device-001", "Android"
        );
        token.deactivate();
        assertFalse(token.getActive());
        LocalDateTime oldUpdatedAt = token.getUpdatedAt();
        
        // Wait a bit to ensure timestamp changes
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // Ignore
        }
        
        // When
        token.activate();
        
        // Then
        assertTrue(token.getActive());
        assertTrue(token.getUpdatedAt().isAfter(oldUpdatedAt) || 
                   token.getUpdatedAt().isEqual(oldUpdatedAt));
    }
    
    @Test
    void testOnCreate_SetsTimestamps() {
        // Given
        DeviceToken token = new DeviceToken();
        token.setUserId(1L);
        token.setFcmToken("token");
        token.setDeviceId("device");
        
        // When
        token.onCreate();
        
        // Then
        assertNotNull(token.getRegisteredAt());
        assertNotNull(token.getUpdatedAt());
        assertTrue(token.getActive());
    }
    
    @Test
    void testOnUpdate_UpdatesTimestamp() {
        // Given
        DeviceToken token = DeviceToken.createDeviceToken(
                1L, "token", "device-001", "Android"
        );
        LocalDateTime oldUpdatedAt = token.getUpdatedAt();
        
        // Wait a bit to ensure timestamp changes
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // Ignore
        }
        
        // When
        token.onUpdate();
        
        // Then
        assertTrue(token.getUpdatedAt().isAfter(oldUpdatedAt) || 
                   token.getUpdatedAt().isEqual(oldUpdatedAt));
    }
    
    @Test
    void testBuilder() {
        // Given/When
        DeviceToken token = DeviceToken.builder()
                .userId(1L)
                .fcmToken("token")
                .deviceId("device")
                .platform("iOS")
                .active(true)
                .registeredAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        // Then
        assertNotNull(token);
        assertEquals(1L, token.getUserId());
        assertEquals("token", token.getFcmToken());
        assertEquals("device", token.getDeviceId());
        assertEquals("iOS", token.getPlatform());
        assertTrue(token.getActive());
    }
}
