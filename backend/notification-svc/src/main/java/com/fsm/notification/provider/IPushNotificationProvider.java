package com.fsm.notification.provider;

/**
 * Interface for push notification providers
 * Abstracts the underlying push notification service (FCM, APNs, etc.)
 */
public interface IPushNotificationProvider {
    
    /**
     * Send a push notification to a specific device token
     * 
     * @param token the FCM token
     * @param title the notification title
     * @param body the notification body
     * @param data optional data payload for deep linking
     * @return true if successful, false otherwise
     */
    boolean sendPushNotification(String token, String title, String body, java.util.Map<String, String> data);
    
    /**
     * Send a push notification to multiple device tokens
     * 
     * @param tokens list of FCM tokens
     * @param title the notification title
     * @param body the notification body
     * @param data optional data payload for deep linking
     * @return number of successful sends
     */
    int sendPushNotificationToMultiple(java.util.List<String> tokens, String title, String body, java.util.Map<String, String> data);
}
