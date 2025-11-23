package com.fsm.notification.listener;

import com.fsm.notification.domain.NotificationChannel;
import com.fsm.notification.domain.NotificationType;
import com.fsm.notification.domain.Priority;
import com.fsm.notification.event.TaskAssignedEvent;
import com.fsm.notification.service.INotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaskAssignmentNotificationListener
 */
@ExtendWith(MockitoExtension.class)
class TaskAssignmentNotificationListenerTest {
    
    @Mock
    private INotificationService notificationService;
    
    @InjectMocks
    private TaskAssignmentNotificationListener listener;
    
    private TaskAssignedEvent testEvent;
    
    @BeforeEach
    void setUp() {
        testEvent = TaskAssignedEvent.builder()
                .taskId(1L)
                .title("Fix plumbing issue")
                .description("Repair leaking pipe under sink")
                .clientAddress("123 Main St")
                .latitude(40.7128)
                .longitude(-74.0060)
                .priority(Priority.HIGH)
                .estimatedDuration(60)
                .technicianId(100L)
                .customerId(200L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher")
                .build();
    }
    
    @Test
    void testHandleTaskAssignedEvent_SendsBothNotifications() {
        // When
        listener.handleTaskAssignedEvent(testEvent);
        
        // Then - verify both notifications are sent
        verify(notificationService, times(2)).sendNotification(
                anyLong(), any(NotificationType.class), any(NotificationChannel.class), anyString()
        );
        
        // Verify customer email notification
        verify(notificationService).sendNotification(
                eq(200L), eq(NotificationType.TASK_ASSIGNED), eq(NotificationChannel.EMAIL), anyString()
        );
        
        // Verify technician push notification
        verify(notificationService).sendNotification(
                eq(100L), eq(NotificationType.TASK_ASSIGNED), eq(NotificationChannel.PUSH), anyString()
        );
    }
    
    @Test
    void testHandleTaskAssignedEvent_CustomerNotificationContainsTaskDetails() {
        // Given
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        
        // When
        listener.handleTaskAssignedEvent(testEvent);
        
        // Then - capture customer email message
        verify(notificationService).sendNotification(
                eq(200L), eq(NotificationType.TASK_ASSIGNED), eq(NotificationChannel.EMAIL), messageCaptor.capture()
        );
        
        String message = messageCaptor.getValue();
        assertTrue(message.contains("Fix plumbing issue"));
        assertTrue(message.contains("Repair leaking pipe under sink"));
        assertTrue(message.contains("123 Main St"));
        assertTrue(message.contains("HIGH"));
        assertTrue(message.contains("60 minutes"));
    }
    
    @Test
    void testHandleTaskAssignedEvent_TechnicianNotificationContainsTaskDetails() {
        // Given
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        
        // When
        listener.handleTaskAssignedEvent(testEvent);
        
        // Then - capture technician push message
        verify(notificationService).sendNotification(
                eq(100L), eq(NotificationType.TASK_ASSIGNED), eq(NotificationChannel.PUSH), messageCaptor.capture()
        );
        
        String message = messageCaptor.getValue();
        assertTrue(message.contains("Fix plumbing issue"));
        assertTrue(message.contains("HIGH"));
        assertTrue(message.contains("123 Main St"));
        assertTrue(message.contains("60 min"));
        assertTrue(message.contains("Task ID: 1"));
    }
    
    @Test
    void testHandleTaskAssignedEvent_NotificationFailureDoesNotThrow() {
        // Given
        doThrow(new RuntimeException("Notification service error"))
                .when(notificationService)
                .sendNotification(anyLong(), any(), any(), anyString());
        
        // When/Then - should not throw exception
        assertDoesNotThrow(() -> listener.handleTaskAssignedEvent(testEvent));
    }
    
    @Test
    void testHandleTaskAssignedEvent_WithDifferentPriorities() {
        // Test with different priority levels
        Priority[] priorities = {Priority.LOW, Priority.MEDIUM, Priority.HIGH, Priority.CRITICAL};
        
        for (Priority priority : priorities) {
            reset(notificationService);
            testEvent.setPriority(priority);
            ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
            
            // When
            listener.handleTaskAssignedEvent(testEvent);
            
            // Then
            verify(notificationService, times(2)).sendNotification(
                    anyLong(), any(), any(), messageCaptor.capture()
            );
            
            // Both messages should contain the priority
            for (String message : messageCaptor.getAllValues()) {
                assertTrue(message.contains(priority.toString()));
            }
        }
    }
    
    @Test
    void testHandleTaskAssignedEvent_WithNullCoordinates() {
        // Given
        testEvent.setLatitude(null);
        testEvent.setLongitude(null);
        
        // When/Then - should not throw exception
        assertDoesNotThrow(() -> listener.handleTaskAssignedEvent(testEvent));
        
        verify(notificationService, times(2)).sendNotification(
                anyLong(), any(), any(), anyString()
        );
    }
    
    @Test
    void testHandleTaskAssignedEvent_MessageFormatting() {
        // Given
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        
        // When
        listener.handleTaskAssignedEvent(testEvent);
        
        // Then
        verify(notificationService, times(2)).sendNotification(
                anyLong(), any(), any(), messageCaptor.capture()
        );
        
        // Check message formatting (should have proper line breaks and sections)
        for (String message : messageCaptor.getAllValues()) {
            assertFalse(message.isEmpty());
            assertTrue(message.length() > 50); // Messages should be reasonably sized
        }
    }
}
