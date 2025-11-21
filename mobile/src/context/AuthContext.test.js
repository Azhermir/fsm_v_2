import React from 'react';
import { renderHook, act, waitFor } from '@testing-library/react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { AuthProvider, useAuth } from './AuthContext';

describe('AuthContext', () => {
  beforeEach(() => {
    jest.clearAllMocks();
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
      AsyncStorage.setItem.mockResolvedValueOnce();

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
  });

  describe('logout', () => {
    it('should remove user data and clear state', async () => {
      const mockUser = { id: 'tech-001', name: 'John Smith' };
      AsyncStorage.getItem.mockResolvedValueOnce(JSON.stringify(mockUser));
      AsyncStorage.removeItem.mockResolvedValueOnce();

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
      expect(result.current.user).toBe(null);
    });

    it('should handle logout errors gracefully', async () => {
      const mockUser = { id: 'tech-001', name: 'John Smith' };
      AsyncStorage.getItem.mockResolvedValueOnce(JSON.stringify(mockUser));
      AsyncStorage.removeItem.mockRejectedValueOnce(new Error('Storage error'));

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      await waitFor(() => {
        expect(result.current.user).toEqual(mockUser);
      });

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
