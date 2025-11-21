import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  Linking,
  Platform,
  ActivityIndicator,
  Alert,
} from 'react-native';
import MapView, { Marker } from 'react-native-maps';
import * as Location from 'expo-location';
import NetInfo from '@react-native-community/netinfo';
import {
  getPriorityColor,
  getStatusColor,
  formatDuration,
  parseAddressCoordinates,
  calculateDistance,
  formatDistance,
  updateTaskStatus,
} from '../services/taskService';
import { addToQueue } from '../services/offlineQueueService';
import CompletionModal from '../components/CompletionModal';

const TaskDetailScreen = ({ route, navigation }) => {
  const { task: initialTask } = route.params;
  const [task, setTask] = useState(initialTask);
  const [currentLocation, setCurrentLocation] = useState(null);
  const [distance, setDistance] = useState(null);
  const [locationPermission, setLocationPermission] = useState(null);
  const [loadingLocation, setLoadingLocation] = useState(true);
  const [isUpdatingStatus, setIsUpdatingStatus] = useState(false);
  const [showCompletionModal, setShowCompletionModal] = useState(false);

  // Get task coordinates
  const taskCoordinates = parseAddressCoordinates(task.clientAddress);

  useEffect(() => {
    requestLocationPermission();
  }, []);

  const requestLocationPermission = async () => {
    try {
      const { status } = await Location.requestForegroundPermissionsAsync();
      setLocationPermission(status === 'granted');
      
      if (status === 'granted') {
        const location = await Location.getCurrentPositionAsync({
          accuracy: Location.Accuracy.Balanced,
        });
        
        const userLocation = {
          latitude: location.coords.latitude,
          longitude: location.coords.longitude,
        };
        
        setCurrentLocation(userLocation);
        
        // Calculate distance
        const dist = calculateDistance(
          userLocation.latitude,
          userLocation.longitude,
          taskCoordinates.latitude,
          taskCoordinates.longitude
        );
        setDistance(dist);
      }
    } catch (error) {
      console.error('Error getting location:', error);
      Alert.alert(
        'Location Error',
        'Unable to get your current location. Navigation features may be limited.'
      );
    } finally {
      setLoadingLocation(false);
    }
  };

  const handleNavigate = () => {
    const scheme = Platform.select({
      ios: 'maps:',
      android: 'geo:',
    });
    
    const latLng = `${taskCoordinates.latitude},${taskCoordinates.longitude}`;
    const label = encodeURIComponent(task.title);
    
    let url;
    if (Platform.OS === 'ios') {
      url = `${scheme}?q=${label}&ll=${latLng}`;
    } else {
      url = `${scheme}${latLng}?q=${label}`;
    }

    Linking.canOpenURL(url)
      .then((supported) => {
        if (supported) {
          return Linking.openURL(url);
        } else {
          // Fallback to Google Maps web
          const webUrl = `https://www.google.com/maps/search/?api=1&query=${latLng}`;
          return Linking.openURL(webUrl);
        }
      })
      .catch((err) => {
        console.error('Error opening maps:', err);
        Alert.alert('Error', 'Unable to open maps application');
      });
  };

  const handleStartTask = async () => {
    Alert.alert(
      'Start Task',
      'Are you sure you want to start this task?',
      [
        {
          text: 'Cancel',
          style: 'cancel',
        },
        {
          text: 'Start',
          onPress: async () => {
            setIsUpdatingStatus(true);
            try {
              // Check network connectivity
              const networkState = await NetInfo.fetch();
              
              if (networkState.isConnected && networkState.isInternetReachable) {
                // Online: Update immediately
                const updatedTask = await updateTaskStatus(task.id, 'IN_PROGRESS');
                setTask(updatedTask);
                Alert.alert('Success', 'Task started successfully');
              } else {
                // Offline: Add to queue
                await addToQueue(task.id, 'IN_PROGRESS');
                const updatedTask = { ...task, status: 'IN_PROGRESS' };
                setTask(updatedTask);
                Alert.alert(
                  'Queued',
                  'You are offline. The status update will be sent when you are back online.'
                );
              }
            } catch (error) {
              console.error('Error starting task:', error);
              Alert.alert('Error', 'Failed to start task. Please try again.');
            } finally {
              setIsUpdatingStatus(false);
            }
          },
        },
      ]
    );
  };

  const handleCompleteTask = async (workSummary) => {
    setIsUpdatingStatus(true);
    try {
      // Check network connectivity
      const networkState = await NetInfo.fetch();
      
      if (networkState.isConnected && networkState.isInternetReachable) {
        // Online: Update immediately
        const updatedTask = await updateTaskStatus(task.id, 'COMPLETED', workSummary);
        setTask(updatedTask);
        setShowCompletionModal(false);
        Alert.alert('Success', 'Task completed successfully', [
          {
            text: 'OK',
            onPress: () => navigation.goBack(),
          },
        ]);
      } else {
        // Offline: Add to queue
        await addToQueue(task.id, 'COMPLETED', workSummary);
        const updatedTask = { ...task, status: 'COMPLETED' };
        setTask(updatedTask);
        setShowCompletionModal(false);
        Alert.alert(
          'Queued',
          'You are offline. The status update will be sent when you are back online.',
          [
            {
              text: 'OK',
              onPress: () => navigation.goBack(),
            },
          ]
        );
      }
    } catch (error) {
      console.error('Error completing task:', error);
      Alert.alert('Error', 'Failed to complete task. Please try again.');
    } finally {
      setIsUpdatingStatus(false);
    }
  };

  return (
    <View style={styles.container}>
      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity
          style={styles.backButton}
          onPress={() => navigation.goBack()}
        >
          <Text style={styles.backButtonText}>← Back</Text>
        </TouchableOpacity>
        <Text style={styles.headerTitle}>Task Details</Text>
      </View>

      <ScrollView style={styles.content}>
        {/* Priority Badge */}
        <View style={styles.section}>
          <View
            style={[
              styles.priorityBadge,
              { backgroundColor: getPriorityColor(task.priority) },
            ]}
          >
            <Text style={styles.priorityText}>{task.priority} PRIORITY</Text>
          </View>
        </View>

        {/* Task Title */}
        <View style={styles.section}>
          <Text 
            style={styles.title}
            accessibilityRole="header"
            accessibilityLevel={1}
          >
            {task.title}
          </Text>
        </View>

        {/* Task Description */}
        {task.description && (
          <View style={styles.section}>
            <Text style={styles.sectionLabel}>Description</Text>
            <Text style={styles.description}>{task.description}</Text>
          </View>
        )}

        {/* Address */}
        <View style={styles.section}>
          <Text style={styles.sectionLabel}>Location</Text>
          <Text style={styles.address}>📍 {task.clientAddress}</Text>
          {distance !== null && (
            <Text style={styles.distance}>
              Distance: {formatDistance(distance)} from your location
            </Text>
          )}
        </View>

        {/* Map */}
        <View style={styles.section}>
          <View style={styles.mapContainer}>
            <MapView
              style={styles.map}
              initialRegion={{
                latitude: taskCoordinates.latitude,
                longitude: taskCoordinates.longitude,
                latitudeDelta: 0.02,
                longitudeDelta: 0.02,
              }}
            >
              {/* Task Location Marker */}
              <Marker
                coordinate={taskCoordinates}
                title={task.title}
                description={task.clientAddress}
                pinColor={getPriorityColor(task.priority)}
              />
              
              {/* Current Location Marker */}
              {currentLocation && (
                <Marker
                  coordinate={currentLocation}
                  title="Your Location"
                  pinColor="blue"
                />
              )}
            </MapView>
            
            {loadingLocation && (
              <View style={styles.mapLoading}>
                <ActivityIndicator size="small" color="#007bff" />
                <Text style={styles.mapLoadingText}>Getting your location...</Text>
              </View>
            )}
          </View>
        </View>

        {/* Navigate Button */}
        <View style={styles.section}>
          <TouchableOpacity
            style={styles.navigateButton}
            onPress={handleNavigate}
            accessibilityRole="button"
            accessibilityLabel="Navigate to task location"
            accessibilityHint="Opens your device's maps app with directions to the task location"
          >
            <Text style={styles.navigateButtonText}>🗺️ Navigate to Location</Text>
          </TouchableOpacity>
        </View>

        {/* Task Details */}
        <View style={styles.section}>
          <Text style={styles.sectionLabel}>Task Information</Text>
          
          <View style={styles.detailRow}>
            <Text style={styles.detailLabel}>Duration:</Text>
            <Text style={styles.detailValue}>
              ⏱️ {formatDuration(task.estimatedDuration)}
            </Text>
          </View>

          <View style={styles.detailRow}>
            <Text style={styles.detailLabel}>Status:</Text>
            <View
              style={[
                styles.statusBadge,
                { backgroundColor: getStatusColor(task.status) },
              ]}
            >
              <Text style={styles.statusText}>
                {task.status.replace('_', ' ')}
              </Text>
            </View>
          </View>

          {task.createdAt && (
            <View style={styles.detailRow}>
              <Text style={styles.detailLabel}>Created:</Text>
              <Text style={styles.detailValue}>
                {new Date(task.createdAt).toLocaleString()}
              </Text>
            </View>
          )}
        </View>

        {/* Special Instructions */}
        {task.instructions && (
          <View style={styles.section}>
            <Text style={styles.sectionLabel}>Special Instructions</Text>
            <View style={styles.instructionsBox}>
              <Text style={styles.instructions}>{task.instructions}</Text>
            </View>
          </View>
        )}

        {/* Status Action Buttons */}
        {task.status === 'ASSIGNED' && (
          <View style={styles.section}>
            <TouchableOpacity
              style={[styles.actionButton, styles.startButton, isUpdatingStatus && styles.buttonDisabled]}
              onPress={handleStartTask}
              disabled={isUpdatingStatus}
              accessibilityRole="button"
              accessibilityLabel="Start task"
              accessibilityHint="Changes task status to In Progress"
            >
              {isUpdatingStatus ? (
                <ActivityIndicator color="#fff" />
              ) : (
                <Text style={styles.actionButtonText}>▶️ Start Task</Text>
              )}
            </TouchableOpacity>
          </View>
        )}

        {task.status === 'IN_PROGRESS' && (
          <View style={styles.section}>
            <TouchableOpacity
              style={[styles.actionButton, styles.completeButton, isUpdatingStatus && styles.buttonDisabled]}
              onPress={() => setShowCompletionModal(true)}
              disabled={isUpdatingStatus}
              accessibilityRole="button"
              accessibilityLabel="Complete task"
              accessibilityHint="Opens completion form to mark task as completed"
            >
              {isUpdatingStatus ? (
                <ActivityIndicator color="#fff" />
              ) : (
                <Text style={styles.actionButtonText}>✓ Complete Task</Text>
              )}
            </TouchableOpacity>
          </View>
        )}
      </ScrollView>

      {/* Completion Modal */}
      <CompletionModal
        visible={showCompletionModal}
        onClose={() => setShowCompletionModal(false)}
        onSubmit={handleCompleteTask}
        taskTitle={task.title}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  header: {
    backgroundColor: '#007bff',
    paddingTop: 50,
    paddingBottom: 16,
    paddingHorizontal: 16,
    flexDirection: 'row',
    alignItems: 'center',
  },
  backButton: {
    marginRight: 16,
  },
  backButtonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '600',
  },
  headerTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#fff',
  },
  content: {
    flex: 1,
  },
  section: {
    backgroundColor: '#fff',
    padding: 16,
    marginBottom: 8,
  },
  priorityBadge: {
    alignSelf: 'flex-start',
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 16,
  },
  priorityText: {
    color: '#fff',
    fontSize: 14,
    fontWeight: 'bold',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#333',
  },
  sectionLabel: {
    fontSize: 14,
    fontWeight: 'bold',
    color: '#666',
    marginBottom: 8,
    textTransform: 'uppercase',
  },
  description: {
    fontSize: 16,
    color: '#333',
    lineHeight: 24,
  },
  address: {
    fontSize: 16,
    color: '#333',
    marginBottom: 8,
  },
  distance: {
    fontSize: 14,
    color: '#007bff',
    fontWeight: '600',
  },
  mapContainer: {
    height: 250,
    borderRadius: 8,
    overflow: 'hidden',
    backgroundColor: '#e0e0e0',
  },
  map: {
    flex: 1,
  },
  mapLoading: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: 'rgba(255, 255, 255, 0.8)',
  },
  mapLoadingText: {
    marginTop: 8,
    fontSize: 14,
    color: '#666',
  },
  navigateButton: {
    backgroundColor: '#28a745',
    paddingVertical: 16,
    paddingHorizontal: 24,
    borderRadius: 8,
    alignItems: 'center',
  },
  navigateButtonText: {
    color: '#fff',
    fontSize: 18,
    fontWeight: 'bold',
  },
  detailRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#e0e0e0',
  },
  detailLabel: {
    fontSize: 16,
    color: '#666',
    fontWeight: '600',
  },
  detailValue: {
    fontSize: 16,
    color: '#333',
  },
  statusBadge: {
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 12,
  },
  statusText: {
    color: '#fff',
    fontSize: 14,
    fontWeight: '600',
  },
  instructionsBox: {
    backgroundColor: '#fff3cd',
    padding: 12,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#ffc107',
  },
  instructions: {
    fontSize: 16,
    color: '#856404',
    lineHeight: 24,
  },
  actionButton: {
    paddingVertical: 16,
    paddingHorizontal: 24,
    borderRadius: 8,
    alignItems: 'center',
    justifyContent: 'center',
    minHeight: 56,
  },
  startButton: {
    backgroundColor: '#ffc107',
  },
  completeButton: {
    backgroundColor: '#28a745',
  },
  actionButtonText: {
    color: '#fff',
    fontSize: 18,
    fontWeight: 'bold',
  },
  buttonDisabled: {
    opacity: 0.6,
  },
});

export default TaskDetailScreen;
