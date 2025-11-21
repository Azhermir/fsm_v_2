import React, { useState, useEffect, useCallback } from 'react';
import {
  View,
  Text,
  FlatList,
  StyleSheet,
  RefreshControl,
  ActivityIndicator,
} from 'react-native';
import { useAuth } from '../context/AuthContext';
import TaskCard from '../components/TaskCard';
import { fetchTechnicianTasks, sortTasksByPriority } from '../services/taskService';

const TaskListScreen = ({ navigation }) => {
  const { user } = useAuth();
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (user) {
      loadTasks();
    }
  }, [user]);

  const loadTasks = async () => {
    try {
      setError(null);
      const data = await fetchTechnicianTasks(user.id);
      const sortedTasks = sortTasksByPriority(data);
      setTasks(sortedTasks);
    } catch (err) {
      console.error('Error loading tasks:', err);
      setError('Failed to load tasks. Please try again.');
      // Set mock data for development/testing when API is not available
      const mockTasks = [
        {
          id: 1,
          title: 'Fix HVAC System',
          description: 'Air conditioning not working properly',
          clientAddress: '123 Main St, Springfield, IL 62701',
          priority: 'CRITICAL',
          estimatedDuration: 120,
          status: 'ASSIGNED',
          createdAt: '2025-11-21T10:00:00',
        },
        {
          id: 2,
          title: 'Replace Water Heater',
          description: 'Water heater needs replacement',
          clientAddress: '456 Oak Ave, Springfield, IL 62702',
          priority: 'HIGH',
          estimatedDuration: 180,
          status: 'ASSIGNED',
          createdAt: '2025-11-21T09:30:00',
        },
        {
          id: 3,
          title: 'Install New Thermostat',
          description: 'Install smart thermostat system',
          clientAddress: '789 Pine Rd, Springfield, IL 62703',
          priority: 'MEDIUM',
          estimatedDuration: 60,
          status: 'IN_PROGRESS',
          createdAt: '2025-11-21T09:00:00',
        },
        {
          id: 4,
          title: 'Routine Maintenance Check',
          description: 'Annual HVAC system maintenance',
          clientAddress: '321 Elm St, Springfield, IL 62704',
          priority: 'LOW',
          estimatedDuration: 45,
          status: 'ASSIGNED',
          createdAt: '2025-11-21T08:30:00',
        },
      ];
      const sortedMockTasks = sortTasksByPriority(mockTasks);
      setTasks(sortedMockTasks);
    } finally {
      setLoading(false);
    }
  };

  const onRefresh = useCallback(async () => {
    setRefreshing(true);
    await loadTasks();
    setRefreshing(false);
  }, [user]);

  const handleTaskPress = (task) => {
    // Navigate to task detail screen (to be implemented)
    console.log('Task pressed:', task.id);
    // navigation.navigate('TaskDetail', { taskId: task.id });
  };

  const renderTaskCard = ({ item }) => (
    <TaskCard task={item} onPress={handleTaskPress} />
  );

  const renderEmptyState = () => (
    <View style={styles.emptyContainer}>
      <Text style={styles.emptyIcon}>📋</Text>
      <Text style={styles.emptyTitle}>No Tasks Found</Text>
      <Text style={styles.emptyText}>
        You don't have any assigned tasks at the moment.
      </Text>
    </View>
  );

  const renderErrorState = () => (
    <View style={styles.emptyContainer}>
      <Text style={styles.emptyIcon}>⚠️</Text>
      <Text style={styles.emptyTitle}>Connection Error</Text>
      <Text style={styles.emptyText}>{error}</Text>
      <Text style={styles.emptySubtext}>
        Pull down to refresh or check your connection.
      </Text>
      {tasks.length > 0 && (
        <Text style={styles.emptySubtext}>
          Showing cached data.
        </Text>
      )}
    </View>
  );

  if (loading && !refreshing) {
    return (
      <View style={styles.centerContainer}>
        <ActivityIndicator size="large" color="#007bff" />
        <Text style={styles.loadingText}>Loading tasks...</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.headerTitle}>My Tasks</Text>
        <Text style={styles.headerSubtitle}>
          Welcome, {user?.name || 'Technician'}
        </Text>
      </View>

      {error && tasks.length === 0 ? (
        <View style={styles.content}>
          {renderErrorState()}
        </View>
      ) : (
        <FlatList
          data={tasks}
          renderItem={renderTaskCard}
          keyExtractor={(item) => item.id.toString()}
          contentContainerStyle={styles.listContent}
          refreshControl={
            <RefreshControl
              refreshing={refreshing}
              onRefresh={onRefresh}
              colors={['#007bff']}
              tintColor="#007bff"
            />
          }
          ListEmptyComponent={renderEmptyState}
          ListHeaderComponent={
            error && tasks.length > 0 ? (
              <View style={styles.errorBanner}>
                <Text style={styles.errorBannerText}>
                  ⚠️ Showing cached data. Pull to refresh.
                </Text>
              </View>
            ) : null
          }
        />
      )}
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
    paddingBottom: 20,
    paddingHorizontal: 16,
  },
  headerTitle: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#fff',
    marginBottom: 4,
  },
  headerSubtitle: {
    fontSize: 16,
    color: '#e0e0e0',
  },
  listContent: {
    paddingVertical: 8,
  },
  centerContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#f5f5f5',
  },
  loadingText: {
    marginTop: 12,
    fontSize: 16,
    color: '#666',
  },
  content: {
    flex: 1,
  },
  emptyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: 32,
    paddingTop: 80,
  },
  emptyIcon: {
    fontSize: 64,
    marginBottom: 16,
  },
  emptyTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 8,
  },
  emptyText: {
    fontSize: 16,
    color: '#666',
    textAlign: 'center',
    marginBottom: 8,
  },
  emptySubtext: {
    fontSize: 14,
    color: '#999',
    textAlign: 'center',
    marginTop: 4,
  },
  errorBanner: {
    backgroundColor: '#fff3cd',
    paddingVertical: 12,
    paddingHorizontal: 16,
    marginHorizontal: 16,
    marginTop: 8,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#ffc107',
  },
  errorBannerText: {
    color: '#856404',
    fontSize: 14,
    textAlign: 'center',
  },
});

export default TaskListScreen;
