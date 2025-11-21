import AsyncStorage from '@react-native-async-storage/async-storage';

const QUEUE_KEY = '@offline_queue';

/**
 * Generate a unique ID for queue items
 * @returns {string} Unique ID
 */
const generateUniqueId = () => {
  // Use a combination of timestamp and random value for uniqueness
  return `${Date.now()}-${Math.random().toString(36).substring(2, 11)}`;
};

/**
 * Offline Queue Service
 * Manages offline status updates queue
 */

/**
 * Add a status update to the offline queue
 * @param {number} taskId - The task ID
 * @param {string} status - The new status
 * @param {string} workSummary - Optional work summary for completion
 * @returns {Promise<void>}
 */
export const addToQueue = async (taskId, status, workSummary = null) => {
  try {
    const queue = await getQueue();
    const queueItem = {
      id: generateUniqueId(),
      taskId,
      status,
      workSummary,
      timestamp: new Date().toISOString(),
    };
    queue.push(queueItem);
    await AsyncStorage.setItem(QUEUE_KEY, JSON.stringify(queue));
  } catch (error) {
    console.error('Error adding to queue:', error);
    throw error;
  }
};

/**
 * Get all items in the offline queue
 * @returns {Promise<Array>} Array of queue items
 */
export const getQueue = async () => {
  try {
    const queueJson = await AsyncStorage.getItem(QUEUE_KEY);
    return queueJson ? JSON.parse(queueJson) : [];
  } catch (error) {
    console.error('Error getting queue:', error);
    return [];
  }
};

/**
 * Remove an item from the queue
 * @param {string} itemId - The queue item ID
 * @returns {Promise<void>}
 */
export const removeFromQueue = async (itemId) => {
  try {
    const queue = await getQueue();
    const updatedQueue = queue.filter(item => item.id !== itemId);
    await AsyncStorage.setItem(QUEUE_KEY, JSON.stringify(updatedQueue));
  } catch (error) {
    console.error('Error removing from queue:', error);
    throw error;
  }
};

/**
 * Clear the entire queue
 * @returns {Promise<void>}
 */
export const clearQueue = async () => {
  try {
    await AsyncStorage.removeItem(QUEUE_KEY);
  } catch (error) {
    console.error('Error clearing queue:', error);
    throw error;
  }
};

/**
 * Process the offline queue
 * @param {Function} updateStatusFn - Function to call for each item
 * @returns {Promise<{successful: number, failed: number}>}
 */
export const processQueue = async (updateStatusFn) => {
  try {
    const queue = await getQueue();
    let successful = 0;
    let failed = 0;

    for (const item of queue) {
      try {
        await updateStatusFn(item.taskId, item.status, item.workSummary);
        await removeFromQueue(item.id);
        successful++;
      } catch (error) {
        console.error(`Failed to process queue item ${item.id}:`, error);
        failed++;
      }
    }

    return { successful, failed };
  } catch (error) {
    console.error('Error processing queue:', error);
    return { successful: 0, failed: 0 };
  }
};
