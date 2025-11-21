package com.fsm.notification.repository;

import com.fsm.notification.domain.Notification;
import com.fsm.notification.domain.NotificationChannel;
import com.fsm.notification.domain.NotificationStatus;
import com.fsm.notification.domain.NotificationType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for NotificationRepository
 */
@DataJpaTest
class NotificationRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Test
    void testSaveAndFindById() {
        // Given
        Notification notification = Notification.createNotification(
            1L, NotificationType.TASK_ASSIGNED, NotificationChannel.EMAIL, "Test message"
        );
        
        // When
        Notification saved = notificationRepository.save(notification);
        entityManager.flush();
        Notification found = notificationRepository.findById(saved.getId()).orElse(null);
        
        // Then
        assertNotNull(found);
        assertEquals(saved.getId(), found.getId());
        assertEquals(1L, found.getRecipientId());
        assertEquals(NotificationType.TASK_ASSIGNED, found.getType());
        assertEquals(NotificationChannel.EMAIL, found.getChannel());
        assertEquals("Test message", found.getMessage());
        assertEquals(NotificationStatus.PENDING, found.getStatus());
    }
    
    @Test
    void testFindByRecipientId() {
        // Given
        Notification n1 = Notification.createNotification(
            1L, NotificationType.TASK_ASSIGNED, NotificationChannel.EMAIL, "Message 1"
        );
        Notification n2 = Notification.createNotification(
            1L, NotificationType.TASK_COMPLETED, NotificationChannel.SMS, "Message 2"
        );
        Notification n3 = Notification.createNotification(
            2L, NotificationType.TASK_ASSIGNED, NotificationChannel.PUSH, "Message 3"
        );
        
        notificationRepository.save(n1);
        notificationRepository.save(n2);
        notificationRepository.save(n3);
        entityManager.flush();
        
        // When
        List<Notification> recipient1Notifications = notificationRepository.findByRecipientId(1L);
        List<Notification> recipient2Notifications = notificationRepository.findByRecipientId(2L);
        
        // Then
        assertEquals(2, recipient1Notifications.size());
        assertEquals(1, recipient2Notifications.size());
        assertTrue(recipient1Notifications.stream().allMatch(n -> n.getRecipientId().equals(1L)));
        assertTrue(recipient2Notifications.stream().allMatch(n -> n.getRecipientId().equals(2L)));
    }
    
    @Test
    void testFindByStatus() {
        // Given
        Notification n1 = Notification.createNotification(
            1L, NotificationType.TASK_ASSIGNED, NotificationChannel.EMAIL, "Message 1"
        );
        Notification n2 = Notification.createNotification(
            2L, NotificationType.TASK_COMPLETED, NotificationChannel.SMS, "Message 2"
        );
        n2.markAsSent();
        
        Notification n3 = Notification.createNotification(
            3L, NotificationType.TASK_ASSIGNED, NotificationChannel.PUSH, "Message 3"
        );
        n3.markAsFailed();
        
        notificationRepository.save(n1);
        notificationRepository.save(n2);
        notificationRepository.save(n3);
        entityManager.flush();
        
        // When
        List<Notification> pendingNotifications = notificationRepository.findByStatus(NotificationStatus.PENDING);
        List<Notification> sentNotifications = notificationRepository.findByStatus(NotificationStatus.SENT);
        List<Notification> failedNotifications = notificationRepository.findByStatus(NotificationStatus.FAILED);
        
        // Then
        assertEquals(1, pendingNotifications.size());
        assertEquals(1, sentNotifications.size());
        assertEquals(1, failedNotifications.size());
        assertEquals(NotificationStatus.PENDING, pendingNotifications.get(0).getStatus());
        assertEquals(NotificationStatus.SENT, sentNotifications.get(0).getStatus());
        assertEquals(NotificationStatus.FAILED, failedNotifications.get(0).getStatus());
    }
    
    @Test
    void testFindByType() {
        // Given
        Notification n1 = Notification.createNotification(
            1L, NotificationType.TASK_ASSIGNED, NotificationChannel.EMAIL, "Message 1"
        );
        Notification n2 = Notification.createNotification(
            2L, NotificationType.TASK_ASSIGNED, NotificationChannel.SMS, "Message 2"
        );
        Notification n3 = Notification.createNotification(
            3L, NotificationType.TASK_COMPLETED, NotificationChannel.PUSH, "Message 3"
        );
        
        notificationRepository.save(n1);
        notificationRepository.save(n2);
        notificationRepository.save(n3);
        entityManager.flush();
        
        // When
        List<Notification> assignedNotifications = notificationRepository.findByType(NotificationType.TASK_ASSIGNED);
        List<Notification> completedNotifications = notificationRepository.findByType(NotificationType.TASK_COMPLETED);
        List<Notification> updateNotifications = notificationRepository.findByType(NotificationType.TASK_STATUS_UPDATE);
        
        // Then
        assertEquals(2, assignedNotifications.size());
        assertEquals(1, completedNotifications.size());
        assertEquals(0, updateNotifications.size());
        assertTrue(assignedNotifications.stream().allMatch(n -> n.getType() == NotificationType.TASK_ASSIGNED));
    }
    
    @Test
    void testFindByRecipientIdAndStatus() {
        // Given
        Notification n1 = Notification.createNotification(
            1L, NotificationType.TASK_ASSIGNED, NotificationChannel.EMAIL, "Message 1"
        );
        Notification n2 = Notification.createNotification(
            1L, NotificationType.TASK_COMPLETED, NotificationChannel.SMS, "Message 2"
        );
        n2.markAsSent();
        
        Notification n3 = Notification.createNotification(
            2L, NotificationType.TASK_ASSIGNED, NotificationChannel.PUSH, "Message 3"
        );
        
        notificationRepository.save(n1);
        notificationRepository.save(n2);
        notificationRepository.save(n3);
        entityManager.flush();
        
        // When
        List<Notification> recipient1Pending = notificationRepository.findByRecipientIdAndStatus(1L, NotificationStatus.PENDING);
        List<Notification> recipient1Sent = notificationRepository.findByRecipientIdAndStatus(1L, NotificationStatus.SENT);
        List<Notification> recipient2Pending = notificationRepository.findByRecipientIdAndStatus(2L, NotificationStatus.PENDING);
        
        // Then
        assertEquals(1, recipient1Pending.size());
        assertEquals(1, recipient1Sent.size());
        assertEquals(1, recipient2Pending.size());
        assertEquals(1L, recipient1Pending.get(0).getRecipientId());
        assertEquals(NotificationStatus.PENDING, recipient1Pending.get(0).getStatus());
    }
    
    @Test
    void testDeleteNotification() {
        // Given
        Notification notification = Notification.createNotification(
            1L, NotificationType.TASK_ASSIGNED, NotificationChannel.EMAIL, "Test message"
        );
        Notification saved = notificationRepository.save(notification);
        entityManager.flush();
        Long id = saved.getId();
        
        // When
        notificationRepository.deleteById(id);
        entityManager.flush();
        
        // Then
        assertFalse(notificationRepository.findById(id).isPresent());
    }
    
    @Test
    void testUpdateNotificationStatus() {
        // Given
        Notification notification = Notification.createNotification(
            1L, NotificationType.TASK_ASSIGNED, NotificationChannel.EMAIL, "Test message"
        );
        Notification saved = notificationRepository.save(notification);
        entityManager.flush();
        
        // When
        saved.markAsSent();
        notificationRepository.save(saved);
        entityManager.flush();
        
        Notification updated = notificationRepository.findById(saved.getId()).orElse(null);
        
        // Then
        assertNotNull(updated);
        assertEquals(NotificationStatus.SENT, updated.getStatus());
        assertNotNull(updated.getSentAt());
    }
}
