// API Configuration
const API_BASE_URL = 'http://localhost:8080/api';

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
