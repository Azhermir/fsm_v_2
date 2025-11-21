import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { getPriorityColor, getStatusColor, formatDuration } from '../services/taskService';

const TaskCard = ({ task, onPress }) => {
  return (
    <TouchableOpacity 
      style={styles.card} 
      onPress={() => onPress && onPress(task)}
      activeOpacity={0.7}
    >
      {/* Priority Badge */}
      <View style={[styles.priorityBadge, { backgroundColor: getPriorityColor(task.priority) }]}>
        <Text style={styles.priorityText}>{task.priority}</Text>
      </View>

      {/* Task Title */}
      <Text style={styles.title} numberOfLines={2}>
        {task.title}
      </Text>

      {/* Task Address */}
      <View style={styles.row}>
        <Text style={styles.label}>📍</Text>
        <Text style={styles.address} numberOfLines={2}>
          {task.clientAddress}
        </Text>
      </View>

      {/* Duration and Status */}
      <View style={styles.footer}>
        <View style={styles.row}>
          <Text style={styles.label}>⏱️</Text>
          <Text style={styles.duration}>{formatDuration(task.estimatedDuration)}</Text>
        </View>
        <View style={[styles.statusBadge, { backgroundColor: getStatusColor(task.status) }]}>
          <Text style={styles.statusText}>{task.status.replace('_', ' ')}</Text>
        </View>
      </View>
    </TouchableOpacity>
  );
};

const styles = StyleSheet.create({
  card: {
    backgroundColor: '#fff',
    borderRadius: 8,
    padding: 16,
    marginHorizontal: 16,
    marginVertical: 8,
    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 2,
    },
    shadowOpacity: 0.1,
    shadowRadius: 3,
    elevation: 3,
  },
  priorityBadge: {
    alignSelf: 'flex-start',
    paddingHorizontal: 12,
    paddingVertical: 4,
    borderRadius: 12,
    marginBottom: 8,
  },
  priorityText: {
    color: '#fff',
    fontSize: 12,
    fontWeight: 'bold',
  },
  title: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 8,
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 8,
  },
  label: {
    fontSize: 14,
    marginRight: 4,
  },
  address: {
    fontSize: 14,
    color: '#666',
    flex: 1,
  },
  footer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: 4,
  },
  duration: {
    fontSize: 14,
    color: '#666',
  },
  statusBadge: {
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 12,
  },
  statusText: {
    color: '#fff',
    fontSize: 12,
    fontWeight: '600',
  },
});

export default TaskCard;
