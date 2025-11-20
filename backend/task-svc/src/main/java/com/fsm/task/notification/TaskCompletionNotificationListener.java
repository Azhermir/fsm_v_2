package com.fsm.task.notification;

import com.fsm.task.event.TaskCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Event listener for TaskCompleted events
 * Triggers notification sending when tasks are completed
 */
@Component
@Slf4j
public class TaskCompletionNotificationListener {
    
    private final NotificationService notificationService;
    
    @Autowired
    public TaskCompletionNotificationListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    /**
     * Listens for TaskCompletedEvent and sends notification
     * Runs asynchronously to avoid blocking the main transaction
     * 
     * @param event the TaskCompletedEvent
     */
    @EventListener
    @Async
    public void handleTaskCompletedEvent(TaskCompletedEvent event) {
        log.info("Received TaskCompletedEvent for task {}", event.getTaskId());
        
        try {
            notificationService.sendTaskCompletionNotification(event);
            log.info("Task completion notification sent successfully for task {}", event.getTaskId());
        } catch (Exception e) {
            log.error("Failed to send task completion notification for task {}: {}", 
                     event.getTaskId(), e.getMessage(), e);
            // Don't rethrow - notification failures should not affect the task update
        }
    }
}
