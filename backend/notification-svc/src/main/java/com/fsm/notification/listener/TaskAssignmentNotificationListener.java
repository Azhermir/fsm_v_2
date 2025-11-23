package com.fsm.notification.listener;

import com.fsm.notification.domain.NotificationChannel;
import com.fsm.notification.domain.NotificationType;
import com.fsm.notification.event.TaskAssignedEvent;
import com.fsm.notification.service.INotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * Event listener for TaskAssigned events
 * Triggers notifications when tasks are assigned to technicians
 * 
 * Domain invariants:
 * - Notifications are sent asynchronously to avoid blocking task assignment
 * - Both customer and technician must receive notifications
 */
@Component
@Slf4j
public class TaskAssignmentNotificationListener {
    
    private final INotificationService notificationService;
    
    private static final DateTimeFormatter TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    @Autowired
    public TaskAssignmentNotificationListener(INotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    /**
     * Listens for TaskAssignedEvent and sends notifications
     * Runs asynchronously to avoid blocking the main transaction
     * 
     * Sends:
     * 1. Email notification to customer with technician name and ETA
     * 2. Push notification to technician with task details
     * 
     * @param event the TaskAssignedEvent
     */
    @EventListener
    @Async
    public void handleTaskAssignedEvent(TaskAssignedEvent event) {
        log.info("Received TaskAssignedEvent for task {}", event.getTaskId());
        
        try {
            // Send email notification to customer
            sendCustomerNotification(event);
            
            // Send push notification to technician
            sendTechnicianNotification(event);
            
            log.info("Task assignment notifications sent successfully for task {}", event.getTaskId());
        } catch (Exception e) {
            log.error("Failed to send task assignment notifications for task {}: {}", 
                     event.getTaskId(), e.getMessage(), e);
            // Don't rethrow - notification failures should not affect the task assignment
        }
    }
    
    /**
     * Send email notification to customer
     * 
     * @param event the TaskAssignedEvent
     */
    private void sendCustomerNotification(TaskAssignedEvent event) {
        log.info("Sending email notification to customer {} for task {}", 
                 event.getCustomerId(), event.getTaskId());
        
        String message = buildCustomerEmailMessage(event);
        
        notificationService.sendNotification(
                event.getCustomerId(),
                NotificationType.TASK_ASSIGNED,
                NotificationChannel.EMAIL,
                message
        );
        
        log.info("Customer email notification sent for task {}", event.getTaskId());
    }
    
    /**
     * Send push notification to technician
     * 
     * @param event the TaskAssignedEvent
     */
    private void sendTechnicianNotification(TaskAssignedEvent event) {
        log.info("Sending push notification to technician {} for task {}", 
                 event.getTechnicianId(), event.getTaskId());
        
        String message = buildTechnicianPushMessage(event);
        
        notificationService.sendNotification(
                event.getTechnicianId(),
                NotificationType.TASK_ASSIGNED,
                NotificationChannel.PUSH,
                message
        );
        
        log.info("Technician push notification sent for task {}", event.getTaskId());
    }
    
    /**
     * Build email message for customer notification
     * 
     * @param event the TaskAssignedEvent
     * @return formatted email message
     */
    private String buildCustomerEmailMessage(TaskAssignedEvent event) {
        StringBuilder message = new StringBuilder();
        
        message.append("Dear Customer,\n\n");
        message.append("Your service task has been assigned to a technician!\n\n");
        message.append("Task Details:\n");
        message.append("- Task: ").append(event.getTitle()).append("\n");
        message.append("- Description: ").append(event.getDescription()).append("\n");
        message.append("- Location: ").append(event.getClientAddress()).append("\n");
        message.append("- Priority: ").append(event.getPriority()).append("\n");
        message.append("- Estimated Duration: ").append(event.getEstimatedDuration()).append(" minutes\n");
        message.append("- Assigned At: ").append(event.getAssignedAt().format(TIME_FORMATTER)).append("\n\n");
        
        message.append("Our technician will arrive shortly to complete your service.\n\n");
        message.append("Thank you for choosing our service!\n\n");
        message.append("Best regards,\n");
        message.append("Field Service Management Team");
        
        return message.toString();
    }
    
    /**
     * Build push notification message for technician
     * Includes task priority and location for mobile app
     * 
     * @param event the TaskAssignedEvent
     * @return formatted push notification message
     */
    private String buildTechnicianPushMessage(TaskAssignedEvent event) {
        StringBuilder message = new StringBuilder();
        
        message.append("New Task Assigned: ").append(event.getTitle()).append("\n\n");
        message.append("Priority: ").append(event.getPriority()).append("\n");
        message.append("Location: ").append(event.getClientAddress()).append("\n");
        message.append("Estimated Duration: ").append(event.getEstimatedDuration()).append(" min\n\n");
        message.append("Description: ").append(event.getDescription()).append("\n\n");
        message.append("Task ID: ").append(event.getTaskId());
        
        return message.toString();
    }
}
