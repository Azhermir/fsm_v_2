package com.fsm.notification.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Notification aggregate domain model representing a notification to be sent
 * This is the root entity of the Notification aggregate
 * 
 * Domain invariants:
 * - Notification must have recipient, type, and channel
 * - Sent notifications should be immutable (status cannot be changed from SENT to PENDING)
 */
@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * ID of the recipient (user/customer/technician)
     * Domain invariant: Must not be null
     */
    @NotNull(message = "Recipient ID is required")
    @Column(nullable = false)
    private Long recipientId;
    
    /**
     * Type of notification
     * Domain invariant: Must not be null
     */
    @NotNull(message = "Notification type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;
    
    /**
     * Channel through which the notification will be sent
     * Domain invariant: Must not be null
     */
    @NotNull(message = "Notification channel is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;
    
    /**
     * Message content of the notification
     * Must not be blank and should be reasonably sized
     */
    @NotBlank(message = "Notification message is required")
    @Size(max = 5000, message = "Message must not exceed 5000 characters")
    @Column(nullable = false, length = 5000)
    private String message;
    
    /**
     * Current status of the notification
     */
    @NotNull(message = "Notification status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.PENDING;
    
    /**
     * Timestamp when the notification was sent
     * Null if the notification has not been sent yet
     */
    @Column
    private LocalDateTime sentAt;
    
    /**
     * Timestamp when the notification was created
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Pre-persist callback to set createdAt timestamp
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = NotificationStatus.PENDING;
        }
    }
    
    /**
     * Factory method to create a Notification with required fields and domain invariant validation
     * 
     * @param recipientId the ID of the recipient (must not be null)
     * @param type the notification type (must not be null)
     * @param channel the notification channel (must not be null)
     * @param message the message content (must not be blank)
     * @return a new Notification instance
     * @throws IllegalArgumentException if domain invariants are violated
     */
    public static Notification createNotification(
            Long recipientId,
            NotificationType type,
            NotificationChannel channel,
            String message) {
        
        // Validate domain invariants
        if (recipientId == null) {
            throw new IllegalArgumentException("Recipient ID is required");
        }
        
        if (type == null) {
            throw new IllegalArgumentException("Notification type is required");
        }
        
        if (channel == null) {
            throw new IllegalArgumentException("Notification channel is required");
        }
        
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Notification message is required");
        }
        
        if (message.length() > 5000) {
            throw new IllegalArgumentException("Message must not exceed 5000 characters");
        }
        
        return Notification.builder()
                .recipientId(recipientId)
                .type(type)
                .channel(channel)
                .message(message)
                .status(NotificationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * Mark the notification as sent
     * Domain invariant: Cannot change status from SENT back to PENDING
     * 
     * @throws IllegalStateException if notification is already marked as sent
     */
    public void markAsSent() {
        if (this.status == NotificationStatus.SENT) {
            throw new IllegalStateException("Notification has already been sent and cannot be modified");
        }
        this.status = NotificationStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }
    
    /**
     * Mark the notification as failed
     * 
     * @throws IllegalStateException if notification is already marked as sent
     */
    public void markAsFailed() {
        if (this.status == NotificationStatus.SENT) {
            throw new IllegalStateException("Notification has already been sent and cannot be modified");
        }
        this.status = NotificationStatus.FAILED;
    }
}
