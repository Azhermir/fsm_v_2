import React from 'react';
import { renderHook, act, waitFor } from '@testing-library/react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { AuthProvider, useAuth } from './AuthContext';
import * as notificationService from '../services/notificationService';

// Mock notification service
jest.mock('../services/notificationService', () => ({
  initializePushNotifications: jest.fn(),
  unregisterDeviceToken: jest.fn(),
}));

describe('AuthContext', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    notificationService.initializePushNotifications.mockResolvedValue({
      deviceId: 'test-device-id',
      pushToken: 'test-push-token',
      registrationData: { id: 1 },
    });
    notificationService.unregisterDeviceToken.mockResolvedValue();
  });

  describe('AuthProvider', () => {
    it('should provide initial auth state', async () => {
      AsyncStorage.getItem.mockResolvedValueOnce(null);

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      expect(result.current.user).toBe(null);
    });

    it('should load user from AsyncStorage on mount', async () => {
      const mockUser = { id: 'tech-001', name: 'John Smith' };
      AsyncStorage.getItem.mockResolvedValueOnce(JSON.stringify(mockUser));

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      expect(result.current.user).toEqual(mockUser);
      expect(AsyncStorage.getItem).toHaveBeenCalledWith('user');
      expect(notificationService.initializePushNotifications).toHaveBeenCalledWith('tech-001');
    });

    it('should handle AsyncStorage errors gracefully', async () => {
      AsyncStorage.getItem.mockRejectedValueOnce(new Error('Storage error'));

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      expect(result.current.user).toBe(null);
    });
  });

  describe('login', () => {
    it('should save user data and update state', async () => {
      AsyncStorage.getItem.mockResolvedValueOnce(null);
      AsyncStorage.setItem.mockResolvedValue();

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      await act(async () => {
        await result.current.login('tech-001', 'John Smith');
      });

      expect(AsyncStorage.setItem).toHaveBeenCalledWith(
        'user',
        JSON.stringify({ id: 'tech-001', name: 'John Smith' })
      );
      expect(result.current.user).toEqual({ id: 'tech-001', name: 'John Smith' });
      expect(notificationService.initializePushNotifications).toHaveBeenCalledWith('tech-001');
    });

    it('should throw error if AsyncStorage fails', async () => {
      AsyncStorage.getItem.mockResolvedValueOnce(null);
      AsyncStorage.setItem.mockRejectedValueOnce(new Error('Storage error'));

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      await expect(
        act(async () => {
          await result.current.login('tech-001', 'John Smith');
        })
      ).rejects.toThrow();
    });

    it('should handle notification initialization errors gracefully', async () => {
      AsyncStorage.getItem.mockResolvedValueOnce(null);
      AsyncStorage.setItem.mockResolvedValue();
      notificationService.initializePushNotifications.mockRejectedValueOnce(
        new Error('Notification error')
      );

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      // Should not throw, login should succeed even if notifications fail
      await act(async () => {
        await result.current.login('tech-001', 'John Smith');
      });

      expect(result.current.user).toEqual({ id: 'tech-001', name: 'John Smith' });
    });
  });

  describe('logout', () => {
    it('should remove user data and clear state', async () => {
      const mockUser = { id: 'tech-001', name: 'John Smith' };
      AsyncStorage.getItem
        .mockResolvedValueOnce(JSON.stringify(mockUser))
        .mockResolvedValueOnce('test-device-id'); // for deviceId retrieval
      AsyncStorage.removeItem.mockResolvedValue();
      AsyncStorage.setItem.mockResolvedValue();

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      await waitFor(() => {
        expect(result.current.user).toEqual(mockUser);
      });

      await act(async () => {
        await result.current.logout();
      });

      expect(AsyncStorage.removeItem).toHaveBeenCalledWith('user');
      expect(AsyncStorage.removeItem).toHaveBeenCalledWith('deviceId');
      expect(result.current.user).toBe(null);
      expect(notificationService.unregisterDeviceToken).toHaveBeenCalledWith('tech-001', 'test-device-id');
    });

    it('should handle logout errors gracefully', async () => {
      // Start with no user
      AsyncStorage.getItem.mockResolvedValueOnce(null);

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      // Login to create a user
      AsyncStorage.setItem.mockResolvedValue();
      await act(async () => {
        await result.current.login('tech-001', 'John Smith');
      });

      expect(result.current.user).toEqual({ id: 'tech-001', name: 'John Smith' });

      // Now make removeItem fail for logout
      AsyncStorage.removeItem.mockRejectedValue(new Error('Storage error'));

      await act(async () => {
        await result.current.logout();
      });

      // Should not throw, just log error
      expect(console.error).toHaveBeenCalled();
    });
  });

  describe('useAuth hook', () => {
    it('should throw error when used outside AuthProvider', () => {
      // Suppress error output for this test
      const originalError = console.error;
      console.error = jest.fn();

      expect(() => {
        renderHook(() => useAuth());
      }).toThrow('useAuth must be used within an AuthProvider');

      console.error = originalError;
    });
  });
});
