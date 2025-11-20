package com.fsm.task.notification;

import com.fsm.task.event.TaskCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * Service for sending notifications
 * Currently simulates email sending - will be replaced with actual email provider
 */
@Service
@Slf4j
public class NotificationService {
    
    @Value("${notification.feedback.url:http://localhost:8080/feedback}")
    private String feedbackUrl;
    
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Send task completion notification to customer
     * 
     * @param event the TaskCompletedEvent containing task details
     */
    public void sendTaskCompletionNotification(TaskCompletedEvent event) {
        log.info("Preparing to send task completion notification for task {}", event.getTaskId());
        
        try {
            // Build email content
            String emailSubject = "Service Task Completed: " + event.getTitle();
            String emailBody = buildCompletionEmailBody(event);
            
            // Log the notification (simulating email send)
            log.info("Sending completion notification email:");
            log.info("To: Customer for task at {}", event.getClientAddress());
            log.info("Subject: {}", emailSubject);
            log.info("Body:\n{}", emailBody);
            
            // Simulate email sending
            // In TASK-124, this will be replaced with actual email provider integration
            simulateEmailSend(emailSubject, emailBody, event.getClientAddress());
            
            log.info("Task completion notification logged successfully for task {}", event.getTaskId());
            
        } catch (Exception e) {
            log.error("Error sending task completion notification for task {}: {}", 
                     event.getTaskId(), e.getMessage(), e);
            throw new NotificationException("Failed to send notification", e);
        }
    }
    
    /**
     * Build the email body for task completion notification
     * 
     * @param event the TaskCompletedEvent
     * @return formatted email body
     */
    private String buildCompletionEmailBody(TaskCompletedEvent event) {
        StringBuilder body = new StringBuilder();
        
        body.append("Dear Customer,\n\n");
        body.append("Your service task has been completed!\n\n");
        body.append("Task Details:\n");
        body.append("- Task: ").append(event.getTitle()).append("\n");
        body.append("- Location: ").append(event.getClientAddress()).append("\n");
        body.append("- Completion Time: ")
            .append(event.getCompletionTime().format(DATE_FORMATTER))
            .append("\n\n");
        
        if (event.getWorkSummary() != null && !event.getWorkSummary().isBlank()) {
            body.append("Work Summary:\n");
            body.append(event.getWorkSummary()).append("\n\n");
        }
        
        body.append("We would love to hear your feedback!\n");
        body.append("Please click the link below to provide feedback:\n");
        body.append(feedbackUrl).append("?taskId=").append(event.getTaskId()).append("\n\n");
        body.append("Thank you for choosing our service!\n\n");
        body.append("Best regards,\n");
        body.append("Field Service Management Team");
        
        return body.toString();
    }
    
    /**
     * Simulate email sending
     * This is a placeholder that logs the email details
     * 
     * @param subject email subject
     * @param body email body
     * @param clientAddress customer address
     */
    private void simulateEmailSend(String subject, String body, String clientAddress) {
        // Simulate email provider call
        log.debug("EMAIL SENT SIMULATION:");
        log.debug("Subject: {}", subject);
        log.debug("Recipient context: {}", clientAddress);
        log.debug("Content length: {} characters", body.length());
        
        // In production, this would call an actual email service
        // For now, just log success
        log.info("Email notification simulated successfully");
    }
}
