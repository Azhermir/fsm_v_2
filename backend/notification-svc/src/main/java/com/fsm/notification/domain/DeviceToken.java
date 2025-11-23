package com.fsm.notification.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DeviceToken entity representing a registered device for push notifications
 * This is part of the Notification bounded context
 * 
 * Domain invariants:
 * - Each device token must be associated with a user (technician)
 * - Device tokens must be unique
 * - Tokens can be updated if device re-registers
 */
@Entity
@Table(name = "device_tokens", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "deviceId"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * ID of the user (technician) who owns this device
     * Domain invariant: Must not be null
     */
    @NotNull(message = "User ID is required")
    @Column(nullable = false)
    private Long userId;
    
    /**
     * FCM (Firebase Cloud Messaging) token for push notifications
     * Domain invariant: Must not be blank
     */
    @NotBlank(message = "FCM token is required")
    @Column(nullable = false, length = 500)
    private String fcmToken;
    
    /**
     * Unique device identifier
     * Domain invariant: Must not be blank
     */
    @NotBlank(message = "Device ID is required")
    @Column(nullable = false, length = 200)
    private String deviceId;
    
    /**
     * Device platform (e.g., iOS, Android)
     */
    @Column(length = 50)
    private String platform;
    
    /**
     * Timestamp when the token was registered
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime registeredAt;
    
    /**
     * Timestamp when the token was last updated
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Whether this device token is active
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
    
    /**
     * Pre-persist callback to set timestamps
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (registeredAt == null) {
            registeredAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (active == null) {
            active = true;
        }
    }
    
    /**
     * Pre-update callback to update timestamp
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Factory method to create a DeviceToken with domain invariant validation
     * 
     * @param userId the user ID (must not be null)
     * @param fcmToken the FCM token (must not be blank)
     * @param deviceId the device ID (must not be blank)
     * @param platform the device platform (optional)
     * @return a new DeviceToken instance
     * @throws IllegalArgumentException if domain invariants are violated
     */
    public static DeviceToken createDeviceToken(
            Long userId,
            String fcmToken,
            String deviceId,
            String platform) {
        
        // Validate domain invariants
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        
        if (fcmToken == null || fcmToken.isBlank()) {
            throw new IllegalArgumentException("FCM token is required");
        }
        
        if (deviceId == null || deviceId.isBlank()) {
            throw new IllegalArgumentException("Device ID is required");
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        return DeviceToken.builder()
                .userId(userId)
                .fcmToken(fcmToken)
                .deviceId(deviceId)
                .platform(platform)
                .registeredAt(now)
                .updatedAt(now)
                .active(true)
                .build();
    }
    
    /**
     * Update the FCM token for this device
     * 
     * @param newToken the new FCM token
     */
    public void updateToken(String newToken) {
        if (newToken == null || newToken.isBlank()) {
            throw new IllegalArgumentException("FCM token cannot be blank");
        }
        this.fcmToken = newToken;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Deactivate this device token
     */
    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Activate this device token
     */
    public void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }
}
