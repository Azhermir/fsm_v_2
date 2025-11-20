package com.fsm.task.notification;

import com.fsm.task.event.TaskCompletedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NotificationService
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    
    private NotificationService notificationService;
    
    @BeforeEach
    void setUp() {
        notificationService = new NotificationService();
        ReflectionTestUtils.setField(notificationService, "feedbackUrl", "http://localhost:8080/feedback");
    }
    
    @Test
    void shouldSendTaskCompletionNotificationSuccessfully() {
        // Given
        TaskCompletedEvent event = TaskCompletedEvent.builder()
                .taskId(1L)
                .title("Fix plumbing issue")
                .clientAddress("123 Main St")
                .workSummary("Replaced faulty pipe and tested water pressure")
                .completionTime(LocalDateTime.now())
                .build();
        
        // When & Then
        assertDoesNotThrow(() -> notificationService.sendTaskCompletionNotification(event));
    }
    
    @Test
    void shouldSendNotificationWithoutWorkSummary() {
        // Given
        TaskCompletedEvent event = TaskCompletedEvent.builder()
                .taskId(2L)
                .title("Electrical repair")
                .clientAddress("456 Oak Ave")
                .workSummary(null)
                .completionTime(LocalDateTime.now())
                .build();
        
        // When & Then
        assertDoesNotThrow(() -> notificationService.sendTaskCompletionNotification(event));
    }
    
    @Test
    void shouldSendNotificationWithBlankWorkSummary() {
        // Given
        TaskCompletedEvent event = TaskCompletedEvent.builder()
                .taskId(3L)
                .title("HVAC maintenance")
                .clientAddress("789 Pine Rd")
                .workSummary("   ")
                .completionTime(LocalDateTime.now())
                .build();
        
        // When & Then
        assertDoesNotThrow(() -> notificationService.sendTaskCompletionNotification(event));
    }
    
    @Test
    void shouldHandleNotificationWithAllFields() {
        // Given
        TaskCompletedEvent event = TaskCompletedEvent.builder()
                .taskId(4L)
                .title("Roof inspection")
                .clientAddress("321 Elm St")
                .workSummary("Inspected roof, found minor wear on shingles. Recommended replacement in 6 months.")
                .completionTime(LocalDateTime.of(2024, 11, 20, 15, 30))
                .build();
        
        // When & Then
        assertDoesNotThrow(() -> notificationService.sendTaskCompletionNotification(event));
    }
}
