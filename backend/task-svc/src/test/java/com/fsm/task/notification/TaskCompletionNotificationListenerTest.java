package com.fsm.task.notification;

import com.fsm.task.event.TaskCompletedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaskCompletionNotificationListener
 */
@ExtendWith(MockitoExtension.class)
class TaskCompletionNotificationListenerTest {
    
    @Mock
    private NotificationService notificationService;
    
    @InjectMocks
    private TaskCompletionNotificationListener listener;
    
    @Test
    void shouldHandleTaskCompletedEventSuccessfully() {
        // Given
        TaskCompletedEvent event = TaskCompletedEvent.builder()
                .taskId(1L)
                .title("Fix plumbing issue")
                .clientAddress("123 Main St")
                .workSummary("Replaced faulty pipe")
                .completionTime(LocalDateTime.now())
                .build();
        
        doNothing().when(notificationService).sendTaskCompletionNotification(any(TaskCompletedEvent.class));
        
        // When
        listener.handleTaskCompletedEvent(event);
        
        // Then
        verify(notificationService, times(1)).sendTaskCompletionNotification(event);
    }
    
    @Test
    void shouldHandleNotificationServiceException() {
        // Given
        TaskCompletedEvent event = TaskCompletedEvent.builder()
                .taskId(2L)
                .title("Electrical repair")
                .clientAddress("456 Oak Ave")
                .workSummary("Fixed wiring")
                .completionTime(LocalDateTime.now())
                .build();
        
        doThrow(new NotificationException("Email service unavailable"))
                .when(notificationService).sendTaskCompletionNotification(any(TaskCompletedEvent.class));
        
        // When & Then - should not throw exception
        listener.handleTaskCompletedEvent(event);
        
        verify(notificationService, times(1)).sendTaskCompletionNotification(event);
    }
    
    @Test
    void shouldHandleGenericException() {
        // Given
        TaskCompletedEvent event = TaskCompletedEvent.builder()
                .taskId(3L)
                .title("HVAC maintenance")
                .clientAddress("789 Pine Rd")
                .completionTime(LocalDateTime.now())
                .build();
        
        doThrow(new RuntimeException("Unexpected error"))
                .when(notificationService).sendTaskCompletionNotification(any(TaskCompletedEvent.class));
        
        // When & Then - should not throw exception
        listener.handleTaskCompletedEvent(event);
        
        verify(notificationService, times(1)).sendTaskCompletionNotification(event);
    }
}
