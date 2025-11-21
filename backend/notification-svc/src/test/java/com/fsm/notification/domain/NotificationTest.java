package com.fsm.notification.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Notification entity
 */
class NotificationTest {
    
    @Test
    void testCreateNotification_Success() {
        // Given
        Long recipientId = 1L;
        NotificationType type = NotificationType.TASK_ASSIGNED;
        NotificationChannel channel = NotificationChannel.EMAIL;
        String message = "Task has been assigned to you";
        
        // When
        Notification notification = Notification.createNotification(recipientId, type, channel, message);
        
        // Then
        assertNotNull(notification);
        assertEquals(recipientId, notification.getRecipientId());
        assertEquals(type, notification.getType());
        assertEquals(channel, notification.getChannel());
        assertEquals(message, notification.getMessage());
        assertEquals(NotificationStatus.PENDING, notification.getStatus());
        assertNotNull(notification.getCreatedAt());
        assertNull(notification.getSentAt());
    }
    
    @Test
    void testCreateNotification_NullRecipientId_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> 
            Notification.createNotification(null, NotificationType.TASK_ASSIGNED, 
                NotificationChannel.EMAIL, "Test message")
        );
    }
    
    @Test
    void testCreateNotification_NullType_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> 
            Notification.createNotification(1L, null, NotificationChannel.EMAIL, "Test message")
        );
    }
    
    @Test
    void testCreateNotification_NullChannel_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> 
            Notification.createNotification(1L, NotificationType.TASK_ASSIGNED, null, "Test message")
        );
    }
    
    @Test
    void testCreateNotification_NullMessage_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> 
            Notification.createNotification(1L, NotificationType.TASK_ASSIGNED, 
                NotificationChannel.EMAIL, null)
        );
    }
    
    @Test
    void testCreateNotification_BlankMessage_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> 
            Notification.createNotification(1L, NotificationType.TASK_ASSIGNED, 
                NotificationChannel.EMAIL, "   ")
        );
    }
    
    @Test
    void testCreateNotification_MessageTooLong_ThrowsException() {
        String longMessage = "a".repeat(5001);
        assertThrows(IllegalArgumentException.class, () -> 
            Notification.createNotification(1L, NotificationType.TASK_ASSIGNED, 
                NotificationChannel.EMAIL, longMessage)
        );
    }
    
    @Test
    void testCreateNotification_MessageMaxLength_Success() {
        String maxMessage = "a".repeat(5000);
        Notification notification = Notification.createNotification(1L, NotificationType.TASK_ASSIGNED, 
            NotificationChannel.EMAIL, maxMessage);
        
        assertNotNull(notification);
        assertEquals(5000, notification.getMessage().length());
    }
    
    @Test
    void testMarkAsSent_Success() {
        // Given
        Notification notification = Notification.createNotification(1L, NotificationType.TASK_ASSIGNED, 
            NotificationChannel.EMAIL, "Test message");
        assertNull(notification.getSentAt());
        
        // When
        notification.markAsSent();
        
        // Then
        assertEquals(NotificationStatus.SENT, notification.getStatus());
        assertNotNull(notification.getSentAt());
    }
    
    @Test
    void testMarkAsSent_AlreadySent_ThrowsException() {
        // Given
        Notification notification = Notification.createNotification(1L, NotificationType.TASK_ASSIGNED, 
            NotificationChannel.EMAIL, "Test message");
        notification.markAsSent();
        
        // When/Then
        assertThrows(IllegalStateException.class, notification::markAsSent);
    }
    
    @Test
    void testMarkAsFailed_Success() {
        // Given
        Notification notification = Notification.createNotification(1L, NotificationType.TASK_ASSIGNED, 
            NotificationChannel.EMAIL, "Test message");
        
        // When
        notification.markAsFailed();
        
        // Then
        assertEquals(NotificationStatus.FAILED, notification.getStatus());
        assertNull(notification.getSentAt());
    }
    
    @Test
    void testMarkAsFailed_AlreadySent_ThrowsException() {
        // Given
        Notification notification = Notification.createNotification(1L, NotificationType.TASK_ASSIGNED, 
            NotificationChannel.EMAIL, "Test message");
        notification.markAsSent();
        
        // When/Then
        assertThrows(IllegalStateException.class, notification::markAsFailed);
    }
    
    @Test
    void testBuilder_Success() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        // When
        Notification notification = Notification.builder()
                .id(1L)
                .recipientId(100L)
                .type(NotificationType.TASK_COMPLETED)
                .channel(NotificationChannel.SMS)
                .message("Task completed successfully")
                .status(NotificationStatus.SENT)
                .sentAt(now)
                .createdAt(now)
                .build();
        
        // Then
        assertEquals(1L, notification.getId());
        assertEquals(100L, notification.getRecipientId());
        assertEquals(NotificationType.TASK_COMPLETED, notification.getType());
        assertEquals(NotificationChannel.SMS, notification.getChannel());
        assertEquals("Task completed successfully", notification.getMessage());
        assertEquals(NotificationStatus.SENT, notification.getStatus());
        assertEquals(now, notification.getSentAt());
        assertEquals(now, notification.getCreatedAt());
    }
    
    @Test
    void testOnCreate_SetsDefaults() {
        // Given
        Notification notification = new Notification();
        
        // When
        notification.onCreate();
        
        // Then
        assertNotNull(notification.getCreatedAt());
        assertEquals(NotificationStatus.PENDING, notification.getStatus());
    }
    
    @Test
    void testOnCreate_DoesNotOverrideExistingValues() {
        // Given
        LocalDateTime existingDate = LocalDateTime.of(2024, 1, 1, 10, 0);
        Notification notification = new Notification();
        notification.setCreatedAt(existingDate);
        notification.setStatus(NotificationStatus.SENT);
        
        // When
        notification.onCreate();
        
        // Then
        assertEquals(existingDate, notification.getCreatedAt());
        assertEquals(NotificationStatus.SENT, notification.getStatus());
    }
    
    @Test
    void testDifferentNotificationTypes() {
        Notification n1 = Notification.createNotification(1L, NotificationType.TASK_ASSIGNED, 
            NotificationChannel.EMAIL, "Assigned");
        Notification n2 = Notification.createNotification(2L, NotificationType.TASK_STATUS_UPDATE, 
            NotificationChannel.SMS, "Updated");
        Notification n3 = Notification.createNotification(3L, NotificationType.TASK_COMPLETED, 
            NotificationChannel.PUSH, "Completed");
        
        assertEquals(NotificationType.TASK_ASSIGNED, n1.getType());
        assertEquals(NotificationType.TASK_STATUS_UPDATE, n2.getType());
        assertEquals(NotificationType.TASK_COMPLETED, n3.getType());
    }
    
    @Test
    void testDifferentNotificationChannels() {
        Notification n1 = Notification.createNotification(1L, NotificationType.TASK_ASSIGNED, 
            NotificationChannel.EMAIL, "Email");
        Notification n2 = Notification.createNotification(2L, NotificationType.TASK_ASSIGNED, 
            NotificationChannel.SMS, "SMS");
        Notification n3 = Notification.createNotification(3L, NotificationType.TASK_ASSIGNED, 
            NotificationChannel.PUSH, "Push");
        
        assertEquals(NotificationChannel.EMAIL, n1.getChannel());
        assertEquals(NotificationChannel.SMS, n2.getChannel());
        assertEquals(NotificationChannel.PUSH, n3.getChannel());
    }
}
