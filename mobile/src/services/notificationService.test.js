import * as Notifications from 'expo-notifications';
import * as Device from 'expo-device';
import { Platform } from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import {
  requestNotificationPermissions,
  getExpoPushToken,
  getDeviceId,
  registerDeviceToken,
  unregisterDeviceToken,
  initializePushNotifications,
  setupNotificationListeners,
  getTaskIdFromNotification,
} from './notificationService';

// Mock AsyncStorage
jest.mock('@react-native-async-storage/async-storage', () => ({
  __esModule: true,
  default: {
    getItem: jest.fn(),
    setItem: jest.fn(),
  },
}));

// Mock expo-notifications
jest.mock('expo-notifications', () => ({
  setNotificationHandler: jest.fn(),
  getPermissionsAsync: jest.fn(),
  requestPermissionsAsync: jest.fn(),
  getExpoPushTokenAsync: jest.fn(),
  addNotificationReceivedListener: jest.fn(),
  addNotificationResponseReceivedListener: jest.fn(),
}));

// Mock expo-device
jest.mock('expo-device', () => ({
  isDevice: true,
  modelName: 'MockDevice',
}));

// Mock global fetch
global.fetch = jest.fn();

describe('notificationService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    AsyncStorage.getItem.mockResolvedValue(null);
    AsyncStorage.setItem.mockResolvedValue();
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  describe('requestNotificationPermissions', () => {
    it('should return true when permission is already granted', async () => {
      Notifications.getPermissionsAsync.mockResolvedValue({ status: 'granted' });

      const result = await requestNotificationPermissions();

      expect(result).toBe(true);
      expect(Notifications.getPermissionsAsync).toHaveBeenCalled();
      expect(Notifications.requestPermissionsAsync).not.toHaveBeenCalled();
    });

    it('should request permission when not already granted', async () => {
      Notifications.getPermissionsAsync.mockResolvedValue({ status: 'undetermined' });
      Notifications.requestPermissionsAsync.mockResolvedValue({ status: 'granted' });

      const result = await requestNotificationPermissions();

      expect(result).toBe(true);
      expect(Notifications.getPermissionsAsync).toHaveBeenCalled();
      expect(Notifications.requestPermissionsAsync).toHaveBeenCalled();
    });

    it('should return false when permission is denied', async () => {
      Notifications.getPermissionsAsync.mockResolvedValue({ status: 'denied' });
      Notifications.requestPermissionsAsync.mockResolvedValue({ status: 'denied' });

      const result = await requestNotificationPermissions();

      expect(result).toBe(false);
    });

    it('should return false when not on a physical device', async () => {
      Device.isDevice = false;

      const result = await requestNotificationPermissions();

      expect(result).toBe(false);
      
      // Reset
      Device.isDevice = true;
    });

    it('should handle errors gracefully', async () => {
      Notifications.getPermissionsAsync.mockRejectedValue(new Error('Permission error'));

      const result = await requestNotificationPermissions();

      expect(result).toBe(false);
    });
  });

  describe('getExpoPushToken', () => {
    it('should return push token on success', async () => {
      const mockToken = 'ExponentPushToken[xxxxxxxxxxxxxxxxxxxxxx]';
      Notifications.getExpoPushTokenAsync.mockResolvedValue({ data: mockToken });

      const result = await getExpoPushToken();

      expect(result).toBe(mockToken);
      expect(Notifications.getExpoPushTokenAsync).toHaveBeenCalled();
    });

    it('should return null when not on a physical device', async () => {
      Device.isDevice = false;

      const result = await getExpoPushToken();

      expect(result).toBeNull();
      
      // Reset
      Device.isDevice = true;
    });

    it('should return null on error', async () => {
      Notifications.getExpoPushTokenAsync.mockRejectedValue(new Error('Token error'));

      const result = await getExpoPushToken();

      expect(result).toBeNull();
    });
  });

  describe('getDeviceId', () => {
    it('should generate and store a new device ID if none exists', async () => {
      Platform.OS = 'ios';
      AsyncStorage.getItem.mockResolvedValue(null);
      jest.spyOn(Date, 'now').mockReturnValue(1234567890);

      const deviceId = await getDeviceId();

      expect(deviceId).toContain('ios');
      expect(deviceId).toContain('MockDevice');
      expect(deviceId).toContain('1234567890');
      expect(AsyncStorage.setItem).toHaveBeenCalledWith('device_unique_id', deviceId);
      
      jest.restoreAllMocks();
    });

    it('should return existing device ID from storage', async () => {
      const existingId = 'ios-MockDevice-9876543210';
      AsyncStorage.getItem.mockResolvedValue(existingId);

      const deviceId = await getDeviceId();

      expect(deviceId).toBe(existingId);
      expect(AsyncStorage.setItem).not.toHaveBeenCalled();
    });

    it('should handle unknown model name', async () => {
      Device.modelName = null;
      AsyncStorage.getItem.mockResolvedValue(null);
      jest.spyOn(Date, 'now').mockReturnValue(1234567890);

      const deviceId = await getDeviceId();

      expect(deviceId).toContain('unknown');
      
      // Reset
      Device.modelName = 'MockDevice';
      jest.restoreAllMocks();
    });

    it('should handle storage errors gracefully', async () => {
      AsyncStorage.getItem.mockRejectedValue(new Error('Storage error'));
      jest.spyOn(Date, 'now').mockReturnValue(1234567890);

      const deviceId = await getDeviceId();

      expect(deviceId).toBeTruthy();
      expect(deviceId).toContain(Platform.OS);
      
      jest.restoreAllMocks();
    });
  });

  describe('registerDeviceToken', () => {
    const mockUserId = 123;
    const mockToken = 'ExponentPushToken[test]';
    const mockDeviceId = 'ios-MockDevice-1234567890';

    it('should register device token successfully', async () => {
      const mockResponse = {
        id: 1,
        userId: mockUserId,
        deviceId: mockDeviceId,
        platform: 'ios',
        registeredAt: '2025-11-23T12:00:00',
        active: true,
      };

      global.fetch.mockResolvedValue({
        ok: true,
        json: async () => mockResponse,
      });

      const result = await registerDeviceToken(mockUserId, mockToken, mockDeviceId);

      expect(result).toEqual(mockResponse);
      expect(global.fetch).toHaveBeenCalledWith(
        expect.stringContaining('/device-tokens'),
        expect.objectContaining({
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            userId: mockUserId,
            fcmToken: mockToken,
            deviceId: mockDeviceId,
            platform: Platform.OS,
          }),
        })
      );
    });

    it('should throw error on failed registration', async () => {
      global.fetch.mockResolvedValue({
        ok: false,
        status: 400,
        json: async () => ({ message: 'Invalid request' }),
      });

      await expect(registerDeviceToken(mockUserId, mockToken, mockDeviceId)).rejects.toThrow();
    });

    it('should handle network errors', async () => {
      global.fetch.mockRejectedValue(new Error('Network error'));

      await expect(registerDeviceToken(mockUserId, mockToken, mockDeviceId)).rejects.toThrow();
    });
  });

  describe('unregisterDeviceToken', () => {
    const mockUserId = 123;
    const mockDeviceId = 'ios-MockDevice-1234567890';

    it('should unregister device token successfully', async () => {
      global.fetch.mockResolvedValue({
        ok: true,
        status: 204,
      });

      await expect(unregisterDeviceToken(mockUserId, mockDeviceId)).resolves.not.toThrow();

      expect(global.fetch).toHaveBeenCalledWith(
        expect.stringContaining(`/device-tokens/${mockUserId}/${mockDeviceId}`),
        expect.objectContaining({
          method: 'DELETE',
        })
      );
    });

    it('should handle 404 gracefully', async () => {
      global.fetch.mockResolvedValue({
        ok: false,
        status: 404,
      });

      await expect(unregisterDeviceToken(mockUserId, mockDeviceId)).resolves.not.toThrow();
    });

    it('should throw error on other failed responses', async () => {
      global.fetch.mockResolvedValue({
        ok: false,
        status: 500,
      });

      await expect(unregisterDeviceToken(mockUserId, mockDeviceId)).rejects.toThrow();
    });
  });

  describe('initializePushNotifications', () => {
    const mockUserId = 123;
    const mockToken = 'ExponentPushToken[test]';

    beforeEach(() => {
      Notifications.getPermissionsAsync.mockResolvedValue({ status: 'granted' });
      Notifications.getExpoPushTokenAsync.mockResolvedValue({ data: mockToken });
      global.fetch.mockResolvedValue({
        ok: true,
        json: async () => ({ id: 1, userId: mockUserId }),
      });
    });

    it('should initialize push notifications successfully', async () => {
      const result = await initializePushNotifications(mockUserId);

      expect(result).not.toBeNull();
      expect(result.pushToken).toBe(mockToken);
      expect(result.deviceId).toBeTruthy();
      expect(result.registrationData).toBeTruthy();
    });

    it('should return null when permissions are denied', async () => {
      Notifications.getPermissionsAsync.mockResolvedValue({ status: 'denied' });
      Notifications.requestPermissionsAsync.mockResolvedValue({ status: 'denied' });

      const result = await initializePushNotifications(mockUserId);

      expect(result).toBeNull();
    });

    it('should return null when push token is unavailable', async () => {
      Notifications.getExpoPushTokenAsync.mockResolvedValue(null);

      const result = await initializePushNotifications(mockUserId);

      expect(result).toBeNull();
    });

    it('should return null when registration fails', async () => {
      global.fetch.mockResolvedValue({
        ok: false,
        status: 500,
      });

      const result = await initializePushNotifications(mockUserId);

      expect(result).toBeNull();
    });
  });

  describe('setupNotificationListeners', () => {
    it('should set up notification listeners', () => {
      const mockReceivedSubscription = { remove: jest.fn() };
      const mockResponseSubscription = { remove: jest.fn() };
      const onReceived = jest.fn();
      const onTapped = jest.fn();

      Notifications.addNotificationReceivedListener.mockReturnValue(mockReceivedSubscription);
      Notifications.addNotificationResponseReceivedListener.mockReturnValue(mockResponseSubscription);

      const { removeListeners } = setupNotificationListeners(onReceived, onTapped);

      expect(Notifications.addNotificationReceivedListener).toHaveBeenCalled();
      expect(Notifications.addNotificationResponseReceivedListener).toHaveBeenCalled();

      removeListeners();

      expect(mockReceivedSubscription.remove).toHaveBeenCalled();
      expect(mockResponseSubscription.remove).toHaveBeenCalled();
    });

    it('should handle notifications when listeners are called', () => {
      const onReceived = jest.fn();
      const onTapped = jest.fn();
      let receivedHandler;
      let tappedHandler;

      Notifications.addNotificationReceivedListener.mockImplementation(handler => {
        receivedHandler = handler;
        return { remove: jest.fn() };
      });
      Notifications.addNotificationResponseReceivedListener.mockImplementation(handler => {
        tappedHandler = handler;
        return { remove: jest.fn() };
      });

      setupNotificationListeners(onReceived, onTapped);

      const mockNotification = { request: { content: { title: 'Test' } } };
      const mockResponse = { notification: mockNotification };

      receivedHandler(mockNotification);
      expect(onReceived).toHaveBeenCalledWith(mockNotification);

      tappedHandler(mockResponse);
      expect(onTapped).toHaveBeenCalledWith(mockResponse);
    });

    it('should work when callbacks are not provided', () => {
      Notifications.addNotificationReceivedListener.mockReturnValue({ remove: jest.fn() });
      Notifications.addNotificationResponseReceivedListener.mockReturnValue({ remove: jest.fn() });

      expect(() => setupNotificationListeners()).not.toThrow();
    });
  });

  describe('getTaskIdFromNotification', () => {
    it('should extract task ID from notification data', () => {
      const notification = {
        request: {
          content: {
            data: {
              taskId: '123',
            },
          },
        },
      };

      const taskId = getTaskIdFromNotification(notification);

      expect(taskId).toBe('123');
    });

    it('should return null when task ID is not present', () => {
      const notification = {
        request: {
          content: {
            data: {},
          },
        },
      };

      const taskId = getTaskIdFromNotification(notification);

      expect(taskId).toBeNull();
    });

    it('should return null when notification structure is invalid', () => {
      const notification = null;

      const taskId = getTaskIdFromNotification(notification);

      expect(taskId).toBeNull();
    });

    it('should handle missing nested properties', () => {
      const notification = {
        request: {},
      };

      const taskId = getTaskIdFromNotification(notification);

      expect(taskId).toBeNull();
    });
  });
});
