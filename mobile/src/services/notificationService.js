import * as Notifications from 'expo-notifications';
import * as Device from 'expo-device';
import { Platform } from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';

// API Configuration
const API_BASE_URL = process.env.EXPO_PUBLIC_API_BASE_URL || 'http://localhost:8080/api';

// Configure notification handler for foreground notifications
Notifications.setNotificationHandler({
  handleNotification: async () => ({
    shouldShowAlert: true,
    shouldPlaySound: true,
    shouldSetBadge: true,
  }),
});

/**
 * Request notification permissions from the user
 * @returns {Promise<boolean>} True if permission granted, false otherwise
 */
export const requestNotificationPermissions = async () => {
  try {
    if (!Device.isDevice) {
      console.log('Push notifications require a physical device');
      return false;
    }

    const { status: existingStatus } = await Notifications.getPermissionsAsync();
    let finalStatus = existingStatus;
    
    if (existingStatus !== 'granted') {
      const { status } = await Notifications.requestPermissionsAsync();
      finalStatus = status;
    }
    
    if (finalStatus !== 'granted') {
      console.log('Notification permission not granted');
      return false;
    }
    
    return true;
  } catch (error) {
    console.error('Error requesting notification permissions:', error);
    return false;
  }
};

/**
 * Get the Expo push token for this device
 * @returns {Promise<string|null>} The push token or null if unavailable
 */
export const getExpoPushToken = async () => {
  try {
    if (!Device.isDevice) {
      console.log('Push notifications require a physical device');
      return null;
    }

    const token = await Notifications.getExpoPushTokenAsync();
    return token.data;
  } catch (error) {
    console.error('Error getting push token:', error);
    return null;
  }
};

/**
 * Get a unique device identifier
 * This generates a consistent ID for the device based on platform and model
 * @returns {Promise<string>} Device identifier
 */
export const getDeviceId = async () => {
  try {
    // Try to get existing device ID from storage
    let deviceId = await AsyncStorage.getItem('device_unique_id');
    
    if (!deviceId) {
      // Generate a new device ID if none exists
      deviceId = `${Platform.OS}-${Device.modelName || 'unknown'}-${Date.now()}`;
      await AsyncStorage.setItem('device_unique_id', deviceId);
    }
    
    return deviceId;
  } catch (error) {
    console.error('Error getting device ID:', error);
    // Fallback to a basic identifier if storage fails
    return `${Platform.OS}-${Device.modelName || 'unknown'}-${Date.now()}`;
  }
};

/**
 * Register device token with the backend
 * @param {number} userId - The user ID
 * @param {string} fcmToken - The FCM/Expo push token
 * @param {string} deviceId - The device identifier
 * @returns {Promise<Object>} Registration response
 */
export const registerDeviceToken = async (userId, fcmToken, deviceId) => {
  try {
    const url = `${API_BASE_URL}/device-tokens`;
    
    const body = {
      userId,
      fcmToken,
      deviceId,
      platform: Platform.OS,
    };

    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error('Error registering device token:', error);
    throw error;
  }
};

/**
 * Unregister device token from the backend
 * @param {number} userId - The user ID
 * @param {string} deviceId - The device identifier
 * @returns {Promise<void>}
 */
export const unregisterDeviceToken = async (userId, deviceId) => {
  try {
    const url = `${API_BASE_URL}/device-tokens/${userId}/${deviceId}`;

    const response = await fetch(url, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok && response.status !== 404) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
  } catch (error) {
    console.error('Error unregistering device token:', error);
    throw error;
  }
};

/**
 * Initialize push notifications for a user
 * Requests permissions, gets token, and registers with backend
 * @param {number} userId - The user ID
 * @returns {Promise<Object|null>} Registration result or null if failed
 */
export const initializePushNotifications = async (userId) => {
  try {
    // Request permissions
    const hasPermission = await requestNotificationPermissions();
    if (!hasPermission) {
      console.log('Notification permissions not granted');
      return null;
    }

    // Get push token
    const pushToken = await getExpoPushToken();
    if (!pushToken) {
      console.log('Could not get push token');
      return null;
    }

    // Get device ID (now async)
    const deviceId = await getDeviceId();

    // Register with backend
    const result = await registerDeviceToken(userId, pushToken, deviceId);
    
    return {
      deviceId,
      pushToken,
      registrationData: result,
    };
  } catch (error) {
    console.error('Error initializing push notifications:', error);
    return null;
  }
};

/**
 * Set up notification listeners
 * @param {Function} onNotificationReceived - Callback for received notifications
 * @param {Function} onNotificationTapped - Callback for tapped notifications
 * @returns {Object} Object with removeListeners function
 */
export const setupNotificationListeners = (onNotificationReceived, onNotificationTapped) => {
  // Listener for notifications received while app is foregrounded
  const receivedSubscription = Notifications.addNotificationReceivedListener(notification => {
    if (onNotificationReceived) {
      onNotificationReceived(notification);
    }
  });

  // Listener for when a notification is tapped
  const responseSubscription = Notifications.addNotificationResponseReceivedListener(response => {
    if (onNotificationTapped) {
      onNotificationTapped(response);
    }
  });

  return {
    removeListeners: () => {
      receivedSubscription.remove();
      responseSubscription.remove();
    },
  };
};

/**
 * Extract task ID from notification data
 * @param {Object} notification - The notification object
 * @returns {string|null} Task ID or null
 */
export const getTaskIdFromNotification = (notification) => {
  try {
    const data = notification?.request?.content?.data;
    return data?.taskId || null;
  } catch (error) {
    console.error('Error extracting task ID from notification:', error);
    return null;
  }
};
