package com.fsm.notification.controller;

import com.fsm.notification.dto.DeviceTokenRequest;
import com.fsm.notification.dto.DeviceTokenResponse;
import com.fsm.notification.service.DeviceTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for device token management
 * Handles device registration for push notifications
 */
@RestController
@RequestMapping("/api/device-tokens")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Device Tokens", description = "Device token management for push notifications")
public class DeviceTokenController {
    
    private final DeviceTokenService deviceTokenService;
    
    /**
     * Register a device token for push notifications
     * 
     * @param request the device token registration request
     * @return the registered device token response
     */
    @PostMapping
    @Operation(summary = "Register device token", 
               description = "Register or update a device token for push notifications")
    public ResponseEntity<DeviceTokenResponse> registerDeviceToken(
            @Valid @RequestBody DeviceTokenRequest request) {
        
        log.info("Received device token registration request for user {}", request.getUserId());
        
        DeviceTokenResponse response = deviceTokenService.registerDeviceToken(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Unregister a device token
     * 
     * @param userId the user ID
     * @param deviceId the device ID
     * @return no content response
     */
    @DeleteMapping("/{userId}/{deviceId}")
    @Operation(summary = "Unregister device token", 
               description = "Unregister a device token for push notifications")
    public ResponseEntity<Void> unregisterDeviceToken(
            @PathVariable Long userId,
            @PathVariable String deviceId) {
        
        log.info("Received device token unregistration request for user {} and device {}", 
                 userId, deviceId);
        
        deviceTokenService.unregisterDeviceToken(userId, deviceId);
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get all active device tokens for a user
     * 
     * @param userId the user ID
     * @return list of active device tokens
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Get active device tokens", 
               description = "Get all active device tokens for a user")
    public ResponseEntity<List<DeviceTokenResponse>> getActiveDeviceTokens(
            @PathVariable Long userId) {
        
        log.info("Received request to get active device tokens for user {}", userId);
        
        List<DeviceTokenResponse> tokens = deviceTokenService.getActiveDeviceTokens(userId);
        
        return ResponseEntity.ok(tokens);
    }
}
