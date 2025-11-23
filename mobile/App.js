import React, { useEffect, useRef } from 'react';
import { StatusBar } from 'expo-status-bar';
import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';
import { AuthProvider, useAuth } from './src/context/AuthContext';
import LoginScreen from './src/screens/LoginScreen';
import TaskListScreen from './src/screens/TaskListScreen';
import TaskDetailScreen from './src/screens/TaskDetailScreen';
import { ActivityIndicator, View, StyleSheet } from 'react-native';
import { setupNotificationListeners, getTaskIdFromNotification } from './src/services/notificationService';

const Stack = createStackNavigator();

function AppNavigator() {
  const { user, loading } = useAuth();
  const navigationRef = useRef();

  useEffect(() => {
    if (!user) return;

    // Set up notification listeners
    const { removeListeners } = setupNotificationListeners(
      (notification) => {
        // Handle notification received while app is open
        console.log('Notification received:', notification);
      },
      (response) => {
        // Handle notification tapped - navigate to task detail
        const taskId = getTaskIdFromNotification(response.notification);
        if (taskId && navigationRef.current) {
          navigationRef.current.navigate('TaskDetail', { taskId });
        }
      }
    );

    // Clean up listeners on unmount
    return () => {
      removeListeners();
    };
  }, [user]);

  if (loading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color="#007bff" />
      </View>
    );
  }

  return (
    <NavigationContainer ref={navigationRef}>
      <Stack.Navigator screenOptions={{ headerShown: false }}>
        {user ? (
          <>
            <Stack.Screen name="TaskList" component={TaskListScreen} />
            <Stack.Screen name="TaskDetail" component={TaskDetailScreen} />
          </>
        ) : (
          <Stack.Screen name="Login" component={LoginScreen} />
        )}
      </Stack.Navigator>
    </NavigationContainer>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <StatusBar style="light" />
      <AppNavigator />
    </AuthProvider>
  );
}

const styles = StyleSheet.create({
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#f5f5f5',
  },
});
