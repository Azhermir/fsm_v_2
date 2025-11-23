package com.fsm.notification.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Mock implementation of push notification provider
 * Logs push notifications to console instead of sending through actual FCM
 * Ready for actual FCM integration when Firebase credentials are configured
 */
@Service
@Slf4j
public class MockPushNotificationProvider implements IPushNotificationProvider {
    
    @Override
    public boolean sendPushNotification(String token, String title, String body, Map<String, String> data) {
        try {
            log.info("=== PUSH NOTIFICATION MOCK SEND ===");
            log.info("Token: {}", maskToken(token));
            log.info("Title: {}", title);
            log.info("Body: {}", body);
            
            if (data != null && !data.isEmpty()) {
                log.info("Data payload:");
                data.forEach((key, value) -> log.info("  {}: {}", key, value));
            }
            
            log.info("===================================");
            
            // Simulate successful send
            return true;
        } catch (Exception e) {
            log.error("Error sending mock push notification: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public int sendPushNotificationToMultiple(List<String> tokens, String title, String body, Map<String, String> data) {
        int successCount = 0;
        
        log.info("Sending push notification to {} devices", tokens.size());
        
        for (String token : tokens) {
            if (sendPushNotification(token, title, body, data)) {
                successCount++;
            }
        }
        
        log.info("Successfully sent {} out of {} push notifications", successCount, tokens.size());
        
        return successCount;
    }
    
    /**
     * Mask token for logging (show only first and last 4 characters)
     * 
     * @param token the token to mask
     * @return masked token
     */
    private String maskToken(String token) {
        if (token == null || token.length() <= 8) {
            return "****";
        }
        return token.substring(0, 4) + "..." + token.substring(token.length() - 4);
    }
}
