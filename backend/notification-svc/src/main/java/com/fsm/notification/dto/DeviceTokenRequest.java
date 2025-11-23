package com.fsm.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for registering a device token for push notifications
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceTokenRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotBlank(message = "FCM token is required")
    private String fcmToken;
    
    @NotBlank(message = "Device ID is required")
    private String deviceId;
    
    private String platform;
}
