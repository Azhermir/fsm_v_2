// API Configuration
// For physical devices, replace localhost with your computer's IP address
// Example: const API_BASE_URL = 'http://192.168.1.100:8080/api';
const API_BASE_URL = process.env.EXPO_PUBLIC_API_BASE_URL || 'http://localhost:8080/api';

/**
 * Fetch tasks assigned to a specific technician
 * @param {string} technicianId - The ID of the technician
 * @param {string} status - Optional status filter (ASSIGNED, IN_PROGRESS, COMPLETED)
 * @returns {Promise<Array>} Array of tasks
 */
export const fetchTechnicianTasks = async (technicianId, status = null) => {
  try {
    let url = `${API_BASE_URL}/technicians/${technicianId}/tasks`;
    if (status) {
      url += `?status=${status}`;
    }

    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error('Error fetching tasks:', error);
    throw error;
  }
};

/**
 * Update task status
 * @param {number} taskId - The ID of the task
 * @param {string} status - The new status (IN_PROGRESS, COMPLETED)
 * @param {string} workSummary - Optional work summary (required for COMPLETED)
 * @returns {Promise<Object>} Updated task
 */
export const updateTaskStatus = async (taskId, status, workSummary = null) => {
  try {
    const url = `${API_BASE_URL}/tasks/${taskId}/status`;
    
    const body = {
      status: status,
    };
    
    if (workSummary) {
      body.workSummary = workSummary;
    }

    const response = await fetch(url, {
      method: 'PATCH',
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
    console.error('Error updating task status:', error);
    throw error;
  }
};

/**
 * Get priority color based on priority level
 * @param {string} priority - Priority level (LOW, MEDIUM, HIGH, CRITICAL)
 * @returns {string} Color code
 */
export const getPriorityColor = (priority) => {
  switch (priority) {
    case 'CRITICAL':
      return '#dc3545'; // red
    case 'HIGH':
      return '#fd7e14'; // orange
    case 'MEDIUM':
      return '#ffc107'; // yellow
    case 'LOW':
      return '#28a745'; // green
    default:
      return '#6c757d'; // gray
  }
};

/**
 * Get status color based on task status
 * @param {string} status - Task status
 * @returns {string} Color code
 */
export const getStatusColor = (status) => {
  switch (status) {
    case 'ASSIGNED':
      return '#007bff'; // blue
    case 'IN_PROGRESS':
      return '#ffc107'; // yellow
    case 'COMPLETED':
      return '#28a745'; // green
    case 'UNASSIGNED':
      return '#6c757d'; // gray
    default:
      return '#6c757d'; // gray
  }
};

/**
 * Format duration from minutes to hours and minutes
 * @param {number} minutes - Duration in minutes
 * @returns {string} Formatted duration string
 */
export const formatDuration = (minutes) => {
  if (!minutes) return '0m';
  
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  
  if (hours === 0) {
    return `${mins}m`;
  } else if (mins === 0) {
    return `${hours}h`;
  } else {
    return `${hours}h ${mins}m`;
  }
};

/**
 * Sort tasks by priority and creation date
 * Priority order: CRITICAL > HIGH > MEDIUM > LOW
 * @param {Array} tasks - Array of tasks to sort
 * @returns {Array} Sorted array of tasks
 */
export const sortTasksByPriority = (tasks) => {
  const priorityOrder = {
    CRITICAL: 4,
    HIGH: 3,
    MEDIUM: 2,
    LOW: 1,
  };

  return [...tasks].sort((a, b) => {
    const priorityDiff = priorityOrder[b.priority] - priorityOrder[a.priority];
    if (priorityDiff !== 0) {
      return priorityDiff;
    }
    // If priorities are equal, sort by creation date (newest first)
    return new Date(b.createdAt) - new Date(a.createdAt);
  });
};

/**
 * Calculate distance between two geographic coordinates using Haversine formula
 * @param {number} lat1 - Latitude of first point
 * @param {number} lon1 - Longitude of first point
 * @param {number} lat2 - Latitude of second point
 * @param {number} lon2 - Longitude of second point
 * @returns {number} Distance in kilometers
 */
export const calculateDistance = (lat1, lon1, lat2, lon2) => {
  const R = 6371; // Radius of the Earth in kilometers
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLon = (lon2 - lon1) * Math.PI / 180;
  const a = 
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
    Math.sin(dLon / 2) * Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  const distance = R * c;
  return distance;
};

/**
 * Format distance for display
 * @param {number} distance - Distance in kilometers
 * @returns {string} Formatted distance string
 */
export const formatDistance = (distance) => {
  if (distance < 1) {
    return `${Math.round(distance * 1000)}m`;
  }
  return `${distance.toFixed(1)}km`;
};

/**
 * Parse address to extract coordinates
 * In a real implementation, this would geocode the address
 * For now, we return mock coordinates based on address hash
 * @param {string} address - Task address
 * @returns {object} Coordinates {latitude, longitude}
 */
export const parseAddressCoordinates = (address) => {
  // Mock implementation - in production, use geocoding service
  // Using Springfield, IL area coordinates
  const baseLatitude = 39.7817;
  const baseLongitude = -89.6501;
  
  // Generate pseudo-random offset based on address for demo purposes
  const hash = address.split('').reduce((acc, char) => acc + char.charCodeAt(0), 0);
  const latOffset = ((hash % 100) - 50) / 1000; // ±0.05 degrees
  const lonOffset = ((hash % 150) - 75) / 1000; // ±0.075 degrees
  
  return {
    latitude: baseLatitude + latOffset,
    longitude: baseLongitude + lonOffset,
  };
};
