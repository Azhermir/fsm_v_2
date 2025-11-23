package com.fsm.notification.service;

import com.fsm.notification.domain.DeviceToken;
import com.fsm.notification.dto.DeviceTokenRequest;
import com.fsm.notification.dto.DeviceTokenResponse;
import com.fsm.notification.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing device tokens for push notifications
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DeviceTokenService {
    
    private final DeviceTokenRepository deviceTokenRepository;
    
    /**
     * Register or update a device token for push notifications
     * If device already registered for user, update the token
     * 
     * @param request the device token request
     * @return the device token response
     */
    @Transactional
    public DeviceTokenResponse registerDeviceToken(DeviceTokenRequest request) {
        log.info("Registering device token for user {} and device {}", 
                 request.getUserId(), request.getDeviceId());
        
        // Check if device token already exists for this user and device
        DeviceToken deviceToken = deviceTokenRepository
                .findByUserIdAndDeviceId(request.getUserId(), request.getDeviceId())
                .orElse(null);
        
        if (deviceToken != null) {
            // Update existing token
            log.info("Device token already exists for user {} and device {}, updating token", 
                     request.getUserId(), request.getDeviceId());
            deviceToken.updateToken(request.getFcmToken());
            if (request.getPlatform() != null) {
                deviceToken.setPlatform(request.getPlatform());
            }
            deviceToken.activate(); // Ensure it's active
        } else {
            // Create new token
            log.info("Creating new device token for user {} and device {}", 
                     request.getUserId(), request.getDeviceId());
            deviceToken = DeviceToken.createDeviceToken(
                    request.getUserId(),
                    request.getFcmToken(),
                    request.getDeviceId(),
                    request.getPlatform()
            );
        }
        
        DeviceToken savedToken = deviceTokenRepository.save(deviceToken);
        
        log.info("Device token registered successfully for user {} and device {}", 
                 request.getUserId(), request.getDeviceId());
        
        return convertToResponse(savedToken);
    }
    
    /**
     * Unregister a device token
     * 
     * @param userId the user ID
     * @param deviceId the device ID
     */
    @Transactional
    public void unregisterDeviceToken(Long userId, String deviceId) {
        log.info("Unregistering device token for user {} and device {}", userId, deviceId);
        
        DeviceToken deviceToken = deviceTokenRepository
                .findByUserIdAndDeviceId(userId, deviceId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Device token not found for user " + userId + " and device " + deviceId));
        
        deviceToken.deactivate();
        deviceTokenRepository.save(deviceToken);
        
        log.info("Device token unregistered successfully for user {} and device {}", userId, deviceId);
    }
    
    /**
     * Get all active device tokens for a user
     * 
     * @param userId the user ID
     * @return list of device token responses
     */
    @Transactional(readOnly = true)
    public List<DeviceTokenResponse> getActiveDeviceTokens(Long userId) {
        log.info("Retrieving active device tokens for user {}", userId);
        
        List<DeviceToken> tokens = deviceTokenRepository.findByUserIdAndActiveTrue(userId);
        
        log.info("Found {} active device tokens for user {}", tokens.size(), userId);
        
        return tokens.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all active FCM tokens for a user (for sending push notifications)
     * 
     * @param userId the user ID
     * @return list of FCM tokens
     */
    @Transactional(readOnly = true)
    public List<String> getActiveFcmTokens(Long userId) {
        log.info("Retrieving active FCM tokens for user {}", userId);
        
        List<DeviceToken> tokens = deviceTokenRepository.findByUserIdAndActiveTrue(userId);
        
        return tokens.stream()
                .map(DeviceToken::getFcmToken)
                .collect(Collectors.toList());
    }
    
    /**
     * Convert DeviceToken entity to DeviceTokenResponse DTO
     * 
     * @param deviceToken the device token entity
     * @return the device token response DTO
     */
    private DeviceTokenResponse convertToResponse(DeviceToken deviceToken) {
        return DeviceTokenResponse.builder()
                .id(deviceToken.getId())
                .userId(deviceToken.getUserId())
                .deviceId(deviceToken.getDeviceId())
                .platform(deviceToken.getPlatform())
                .registeredAt(deviceToken.getRegisteredAt())
                .updatedAt(deviceToken.getUpdatedAt())
                .active(deviceToken.getActive())
                .build();
    }
}
