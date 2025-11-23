import React, { createContext, useState, useContext, useEffect } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { initializePushNotifications, unregisterDeviceToken } from '../services/notificationService';

const AuthContext = createContext();

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [deviceId, setDeviceId] = useState(null);

  useEffect(() => {
    loadUser();
  }, []);

  const loadUser = async () => {
    try {
      const userData = await AsyncStorage.getItem('user');
      if (userData) {
        const user = JSON.parse(userData);
        setUser(user);
        
        // Initialize push notifications if user is logged in
        await initializeNotifications(user.id);
      }
    } catch (error) {
      console.error('Error loading user:', error);
    } finally {
      setLoading(false);
    }
  };

  const initializeNotifications = async (userId) => {
    try {
      const result = await initializePushNotifications(userId);
      if (result) {
        setDeviceId(result.deviceId);
        // Store device ID for cleanup on logout
        await AsyncStorage.setItem('deviceId', result.deviceId);
      }
    } catch (error) {
      console.error('Error initializing push notifications:', error);
    }
  };

  const login = async (technicianId, name) => {
    try {
      const userData = { id: technicianId, name };
      await AsyncStorage.setItem('user', JSON.stringify(userData));
      setUser(userData);
      
      // Initialize push notifications after login
      await initializeNotifications(technicianId);
    } catch (error) {
      console.error('Error saving user:', error);
      throw error;
    }
  };

  const logout = async () => {
    try {
      // Retrieve device ID for cleanup
      const storedDeviceId = deviceId || await AsyncStorage.getItem('deviceId');
      
      // Unregister push notifications before logout
      if (user && storedDeviceId) {
        await unregisterDeviceToken(user.id, storedDeviceId);
      }
      
      await AsyncStorage.removeItem('user');
      await AsyncStorage.removeItem('deviceId');
      setUser(null);
      setDeviceId(null);
    } catch (error) {
      console.error('Error removing user:', error);
    }
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};
