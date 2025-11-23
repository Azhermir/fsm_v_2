package com.fsm.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for device token registration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceTokenResponse {
    
    private Long id;
    private Long userId;
    private String deviceId;
    private String platform;
    private LocalDateTime registeredAt;
    private LocalDateTime updatedAt;
    private Boolean active;
}
