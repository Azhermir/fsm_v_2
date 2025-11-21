import AsyncStorage from '@react-native-async-storage/async-storage';
import {
  addToQueue,
  getQueue,
  removeFromQueue,
  clearQueue,
  processQueue,
} from './offlineQueueService';

// Create a storage mock with state
let mockStorage = {};

beforeAll(() => {
  AsyncStorage.setItem.mockImplementation(async (key, value) => {
    mockStorage[key] = value;
    return Promise.resolve();
  });

  AsyncStorage.getItem.mockImplementation(async (key) => {
    return Promise.resolve(mockStorage[key] || null);
  });

  AsyncStorage.removeItem.mockImplementation(async (key) => {
    delete mockStorage[key];
    return Promise.resolve();
  });

  AsyncStorage.clear.mockImplementation(async () => {
    mockStorage = {};
    return Promise.resolve();
  });
});

describe('offlineQueueService', () => {
  beforeEach(async () => {
    mockStorage = {};
    jest.clearAllMocks();
  });

  describe('addToQueue', () => {
    it('should add an item to an empty queue', async () => {
      await addToQueue(1, 'IN_PROGRESS');
      
      const queue = await getQueue();
      expect(queue).toHaveLength(1);
      expect(queue[0]).toMatchObject({
        taskId: 1,
        status: 'IN_PROGRESS',
        workSummary: null,
      });
      expect(queue[0].id).toBeDefined();
      expect(queue[0].timestamp).toBeDefined();
    });

    it('should add an item with work summary', async () => {
      await addToQueue(1, 'COMPLETED', 'Fixed the HVAC system');
      
      const queue = await getQueue();
      expect(queue).toHaveLength(1);
      expect(queue[0]).toMatchObject({
        taskId: 1,
        status: 'COMPLETED',
        workSummary: 'Fixed the HVAC system',
      });
    });

    it('should add multiple items to the queue', async () => {
      await addToQueue(1, 'IN_PROGRESS');
      await addToQueue(2, 'COMPLETED', 'Task completed');
      
      const queue = await getQueue();
      expect(queue).toHaveLength(2);
      expect(queue[0].taskId).toBe(1);
      expect(queue[1].taskId).toBe(2);
    });

    it('should throw error if AsyncStorage fails', async () => {
      AsyncStorage.setItem.mockRejectedValueOnce(new Error('Storage error'));
      
      await expect(addToQueue(1, 'IN_PROGRESS')).rejects.toThrow('Storage error');
    });
  });

  describe('getQueue', () => {
    it('should return empty array when queue is empty', async () => {
      const queue = await getQueue();
      expect(queue).toEqual([]);
    });

    it('should return queue items', async () => {
      await addToQueue(1, 'IN_PROGRESS');
      await addToQueue(2, 'COMPLETED', 'Done');
      
      const queue = await getQueue();
      expect(queue).toHaveLength(2);
    });

    it('should return empty array on AsyncStorage error', async () => {
      AsyncStorage.getItem.mockRejectedValueOnce(new Error('Storage error'));
      
      const queue = await getQueue();
      expect(queue).toEqual([]);
    });
  });

  describe('removeFromQueue', () => {
    it('should remove an item from the queue', async () => {
      await addToQueue(1, 'IN_PROGRESS');
      await addToQueue(2, 'COMPLETED', 'Done');
      
      let queue = await getQueue();
      const itemId = queue[0].id;
      
      await removeFromQueue(itemId);
      
      queue = await getQueue();
      expect(queue).toHaveLength(1);
      expect(queue[0].taskId).toBe(2);
    });

    it('should do nothing if item does not exist', async () => {
      await addToQueue(1, 'IN_PROGRESS');
      
      await removeFromQueue('non-existent-id');
      
      const queue = await getQueue();
      expect(queue).toHaveLength(1);
    });

    it('should throw error if AsyncStorage fails', async () => {
      AsyncStorage.setItem.mockRejectedValueOnce(new Error('Storage error'));
      
      await expect(removeFromQueue('some-id')).rejects.toThrow('Storage error');
    });
  });

  describe('clearQueue', () => {
    it('should clear the entire queue', async () => {
      await addToQueue(1, 'IN_PROGRESS');
      await addToQueue(2, 'COMPLETED', 'Done');
      
      await clearQueue();
      
      const queue = await getQueue();
      expect(queue).toEqual([]);
    });

    it('should throw error if AsyncStorage fails', async () => {
      AsyncStorage.removeItem.mockRejectedValueOnce(new Error('Storage error'));
      
      await expect(clearQueue()).rejects.toThrow('Storage error');
    });
  });

  describe('processQueue', () => {
    it('should process all items successfully', async () => {
      await addToQueue(1, 'IN_PROGRESS');
      await addToQueue(2, 'COMPLETED', 'Done');
      
      const updateStatusFn = jest.fn().mockResolvedValue({});
      
      const result = await processQueue(updateStatusFn);
      
      expect(result).toEqual({ successful: 2, failed: 0 });
      expect(updateStatusFn).toHaveBeenCalledTimes(2);
      expect(updateStatusFn).toHaveBeenCalledWith(1, 'IN_PROGRESS', null);
      expect(updateStatusFn).toHaveBeenCalledWith(2, 'COMPLETED', 'Done');
      
      const queue = await getQueue();
      expect(queue).toEqual([]);
    });

    it('should handle partial failures', async () => {
      await addToQueue(1, 'IN_PROGRESS');
      await addToQueue(2, 'COMPLETED', 'Done');
      await addToQueue(3, 'IN_PROGRESS');
      
      const updateStatusFn = jest.fn()
        .mockResolvedValueOnce({})
        .mockRejectedValueOnce(new Error('Network error'))
        .mockResolvedValueOnce({});
      
      const result = await processQueue(updateStatusFn);
      
      expect(result).toEqual({ successful: 2, failed: 1 });
      expect(updateStatusFn).toHaveBeenCalledTimes(3);
      
      const queue = await getQueue();
      expect(queue).toHaveLength(1);
      expect(queue[0].taskId).toBe(2);
    });

    it('should return zero counts on error', async () => {
      AsyncStorage.getItem.mockRejectedValueOnce(new Error('Storage error'));
      
      const updateStatusFn = jest.fn();
      const result = await processQueue(updateStatusFn);
      
      expect(result).toEqual({ successful: 0, failed: 0 });
      expect(updateStatusFn).not.toHaveBeenCalled();
    });
  });
});
