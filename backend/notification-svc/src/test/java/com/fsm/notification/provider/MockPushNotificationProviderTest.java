package com.fsm.notification.provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MockPushNotificationProvider
 */
class MockPushNotificationProviderTest {
    
    private MockPushNotificationProvider provider;
    
    @BeforeEach
    void setUp() {
        provider = new MockPushNotificationProvider();
    }
    
    @Test
    void testSendPushNotification_Success() {
        // Given
        String token = "test-fcm-token-12345678";
        String title = "Test Notification";
        String body = "Test notification body";
        Map<String, String> data = new HashMap<>();
        data.put("key1", "value1");
        data.put("key2", "value2");
        
        // When
        boolean result = provider.sendPushNotification(token, title, body, data);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void testSendPushNotification_WithoutData() {
        // Given
        String token = "test-fcm-token-12345678";
        String title = "Test Notification";
        String body = "Test notification body";
        
        // When
        boolean result = provider.sendPushNotification(token, title, body, null);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void testSendPushNotification_WithEmptyData() {
        // Given
        String token = "test-fcm-token-12345678";
        String title = "Test Notification";
        String body = "Test notification body";
        Map<String, String> data = new HashMap<>();
        
        // When
        boolean result = provider.sendPushNotification(token, title, body, data);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void testSendPushNotificationToMultiple_Success() {
        // Given
        List<String> tokens = Arrays.asList(
                "token1-12345678",
                "token2-12345678",
                "token3-12345678"
        );
        String title = "Test Notification";
        String body = "Test notification body";
        Map<String, String> data = new HashMap<>();
        data.put("taskId", "123");
        
        // When
        int successCount = provider.sendPushNotificationToMultiple(tokens, title, body, data);
        
        // Then
        assertEquals(3, successCount);
    }
    
    @Test
    void testSendPushNotificationToMultiple_EmptyList() {
        // Given
        List<String> tokens = Arrays.asList();
        String title = "Test Notification";
        String body = "Test notification body";
        
        // When
        int successCount = provider.sendPushNotificationToMultiple(tokens, title, body, null);
        
        // Then
        assertEquals(0, successCount);
    }
    
    @Test
    void testSendPushNotificationToMultiple_SingleToken() {
        // Given
        List<String> tokens = Arrays.asList("token1-12345678");
        String title = "Test Notification";
        String body = "Test notification body";
        
        // When
        int successCount = provider.sendPushNotificationToMultiple(tokens, title, body, null);
        
        // Then
        assertEquals(1, successCount);
    }
    
    @Test
    void testSendPushNotification_WithLongToken() {
        // Given
        String longToken = "a".repeat(200);
        String title = "Test";
        String body = "Body";
        
        // When
        boolean result = provider.sendPushNotification(longToken, title, body, null);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void testSendPushNotification_WithShortToken() {
        // Given
        String shortToken = "abc";
        String title = "Test";
        String body = "Body";
        
        // When
        boolean result = provider.sendPushNotification(shortToken, title, body, null);
        
        // Then
        assertTrue(result);
    }
}
