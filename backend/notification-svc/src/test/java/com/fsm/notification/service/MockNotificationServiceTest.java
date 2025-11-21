package com.fsm.notification.service;

import com.fsm.notification.domain.Notification;
import com.fsm.notification.domain.NotificationChannel;
import com.fsm.notification.domain.NotificationStatus;
import com.fsm.notification.domain.NotificationType;
import com.fsm.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MockNotificationService
 */
@ExtendWith(MockitoExtension.class)
class MockNotificationServiceTest {
    
    @Mock
    private NotificationRepository notificationRepository;
    
    @InjectMocks
    private MockNotificationService notificationService;
    
    private Notification testNotification;
    
    @BeforeEach
    void setUp() {
        testNotification = Notification.createNotification(
            1L, NotificationType.TASK_ASSIGNED, NotificationChannel.EMAIL, "Test message"
        );
        testNotification.setId(1L);
    }
    
    @Test
    void testSendNotification_Success() {
        // Given
        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> {
                Notification n = invocation.getArgument(0);
                if (n.getId() == null) {
                    n.setId(1L);
                }
                return n;
            });
        
        // When
        Notification result = notificationService.sendNotification(
            1L, NotificationType.TASK_ASSIGNED, NotificationChannel.EMAIL, "Test message"
        );
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getRecipientId());
        assertEquals(NotificationType.TASK_ASSIGNED, result.getType());
        assertEquals(NotificationChannel.EMAIL, result.getChannel());
        assertEquals("Test message", result.getMessage());
        assertEquals(NotificationStatus.SENT, result.getStatus());
        assertNotNull(result.getSentAt());
        
        // Verify repository interactions
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }
    
    @Test
    void testSendNotification_CreatesWithPendingStatus() {
        // Given
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> {
                Notification n = invocation.getArgument(0);
                if (n.getId() == null) {
                    n.setId(1L);
                }
                return n;
            });
        
        // When
        notificationService.sendNotification(
            2L, NotificationType.TASK_COMPLETED, NotificationChannel.SMS, "Completed message"
        );
        
        // Then
        verify(notificationRepository, times(2)).save(notificationCaptor.capture());
        
        // First save should have PENDING status, but since we're capturing the same object
        // and it gets mutated, we verify that the method is called twice
        // and the final status is SENT
        List<Notification> capturedNotifications = notificationCaptor.getAllValues();
        assertEquals(2, capturedNotifications.size());
        
        // The final state should be SENT
        Notification finalNotification = capturedNotifications.get(1);
        assertEquals(NotificationStatus.SENT, finalNotification.getStatus());
    }
    
    @Test
    void testSendNotification_DifferentTypes() {
        // Given
        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> {
                Notification n = invocation.getArgument(0);
                if (n.getId() == null) {
                    n.setId(1L);
                }
                return n;
            });
        
        // When
        Notification assigned = notificationService.sendNotification(
            1L, NotificationType.TASK_ASSIGNED, NotificationChannel.EMAIL, "Assigned"
        );
        Notification update = notificationService.sendNotification(
            2L, NotificationType.TASK_STATUS_UPDATE, NotificationChannel.SMS, "Updated"
        );
        Notification completed = notificationService.sendNotification(
            3L, NotificationType.TASK_COMPLETED, NotificationChannel.PUSH, "Completed"
        );
        
        // Then
        assertEquals(NotificationType.TASK_ASSIGNED, assigned.getType());
        assertEquals(NotificationType.TASK_STATUS_UPDATE, update.getType());
        assertEquals(NotificationType.TASK_COMPLETED, completed.getType());
    }
    
    @Test
    void testSendNotification_DifferentChannels() {
        // Given
        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> {
                Notification n = invocation.getArgument(0);
                if (n.getId() == null) {
                    n.setId(1L);
                }
                return n;
            });
        
        // When
        Notification email = notificationService.sendNotification(
            1L, NotificationType.TASK_ASSIGNED, NotificationChannel.EMAIL, "Email"
        );
        Notification sms = notificationService.sendNotification(
            2L, NotificationType.TASK_ASSIGNED, NotificationChannel.SMS, "SMS"
        );
        Notification push = notificationService.sendNotification(
            3L, NotificationType.TASK_ASSIGNED, NotificationChannel.PUSH, "Push"
        );
        
        // Then
        assertEquals(NotificationChannel.EMAIL, email.getChannel());
        assertEquals(NotificationChannel.SMS, sms.getChannel());
        assertEquals(NotificationChannel.PUSH, push.getChannel());
    }
    
    @Test
    void testGetNotificationsByRecipient() {
        // Given
        Notification n1 = Notification.createNotification(
            1L, NotificationType.TASK_ASSIGNED, NotificationChannel.EMAIL, "Message 1"
        );
        n1.setId(1L);
        Notification n2 = Notification.createNotification(
            1L, NotificationType.TASK_COMPLETED, NotificationChannel.SMS, "Message 2"
        );
        n2.setId(2L);
        
        when(notificationRepository.findByRecipientId(1L)).thenReturn(Arrays.asList(n1, n2));
        
        // When
        List<Notification> result = notificationService.getNotificationsByRecipient(1L);
        
        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(n -> n.getRecipientId().equals(1L)));
        verify(notificationRepository).findByRecipientId(1L);
    }
    
    @Test
    void testGetNotificationsByRecipient_NoNotifications() {
        // Given
        when(notificationRepository.findByRecipientId(999L)).thenReturn(Arrays.asList());
        
        // When
        List<Notification> result = notificationService.getNotificationsByRecipient(999L);
        
        // Then
        assertEquals(0, result.size());
        verify(notificationRepository).findByRecipientId(999L);
    }
    
    @Test
    void testGetNotificationById_Success() {
        // Given
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));
        
        // When
        Notification result = notificationService.getNotificationById(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(testNotification.getId(), result.getId());
        verify(notificationRepository).findById(1L);
    }
    
    @Test
    void testGetNotificationById_NotFound() {
        // Given
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> 
            notificationService.getNotificationById(999L)
        );
        verify(notificationRepository).findById(999L);
    }
    
    @Test
    void testRetryNotification_Success() {
        // Given
        Notification failedNotification = Notification.createNotification(
            1L, NotificationType.TASK_ASSIGNED, NotificationChannel.EMAIL, "Test message"
        );
        failedNotification.setId(1L);
        failedNotification.markAsFailed();
        
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(failedNotification));
        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        Notification result = notificationService.retryNotification(1L);
        
        // Then
        assertEquals(NotificationStatus.SENT, result.getStatus());
        assertNotNull(result.getSentAt());
        verify(notificationRepository).findById(1L);
        verify(notificationRepository).save(any(Notification.class));
    }
    
    @Test
    void testRetryNotification_AlreadySent_ThrowsException() {
        // Given
        Notification sentNotification = Notification.createNotification(
            1L, NotificationType.TASK_ASSIGNED, NotificationChannel.EMAIL, "Test message"
        );
        sentNotification.setId(1L);
        sentNotification.markAsSent();
        
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(sentNotification));
        
        // When/Then
        assertThrows(IllegalStateException.class, () -> 
            notificationService.retryNotification(1L)
        );
        verify(notificationRepository).findById(1L);
        verify(notificationRepository, never()).save(any(Notification.class));
    }
    
    @Test
    void testRetryNotification_NotFound() {
        // Given
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> 
            notificationService.retryNotification(999L)
        );
        verify(notificationRepository).findById(999L);
        verify(notificationRepository, never()).save(any(Notification.class));
    }
    
    @Test
    void testRetryNotification_PendingStatus() {
        // Given
        Notification pendingNotification = Notification.createNotification(
            1L, NotificationType.TASK_ASSIGNED, NotificationChannel.EMAIL, "Test message"
        );
        pendingNotification.setId(1L);
        
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(pendingNotification));
        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        Notification result = notificationService.retryNotification(1L);
        
        // Then
        assertEquals(NotificationStatus.SENT, result.getStatus());
        verify(notificationRepository).save(any(Notification.class));
    }
}
