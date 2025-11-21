import {
  fetchTechnicianTasks,
  getPriorityColor,
  getStatusColor,
  formatDuration,
  sortTasksByPriority,
} from './taskService';

// Mock fetch globally
global.fetch = jest.fn();

describe('taskService', () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  describe('fetchTechnicianTasks', () => {
    it('should fetch tasks for a technician successfully', async () => {
      const mockTasks = [
        { id: 1, title: 'Task 1', priority: 'HIGH' },
        { id: 2, title: 'Task 2', priority: 'LOW' },
      ];

      global.fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockTasks,
      });

      const result = await fetchTechnicianTasks('tech-001');

      expect(global.fetch).toHaveBeenCalledWith(
        expect.stringContaining('/technicians/tech-001/tasks'),
        expect.objectContaining({
          method: 'GET',
          headers: { 'Content-Type': 'application/json' },
        })
      );
      expect(result).toEqual(mockTasks);
    });

    it('should fetch tasks with status filter', async () => {
      const mockTasks = [{ id: 1, title: 'Task 1', status: 'ASSIGNED' }];

      global.fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockTasks,
      });

      await fetchTechnicianTasks('tech-001', 'ASSIGNED');

      expect(global.fetch).toHaveBeenCalledWith(
        expect.stringContaining('?status=ASSIGNED'),
        expect.any(Object)
      );
    });

    it('should throw error on failed request', async () => {
      global.fetch.mockResolvedValueOnce({
        ok: false,
        status: 404,
      });

      await expect(fetchTechnicianTasks('tech-001')).rejects.toThrow();
    });

    it('should throw error on network failure', async () => {
      global.fetch.mockRejectedValueOnce(new Error('Network error'));

      await expect(fetchTechnicianTasks('tech-001')).rejects.toThrow('Network error');
    });
  });

  describe('getPriorityColor', () => {
    it('should return red for CRITICAL priority', () => {
      expect(getPriorityColor('CRITICAL')).toBe('#dc3545');
    });

    it('should return orange for HIGH priority', () => {
      expect(getPriorityColor('HIGH')).toBe('#fd7e14');
    });

    it('should return yellow for MEDIUM priority', () => {
      expect(getPriorityColor('MEDIUM')).toBe('#ffc107');
    });

    it('should return green for LOW priority', () => {
      expect(getPriorityColor('LOW')).toBe('#28a745');
    });

    it('should return gray for unknown priority', () => {
      expect(getPriorityColor('UNKNOWN')).toBe('#6c757d');
    });
  });

  describe('getStatusColor', () => {
    it('should return blue for ASSIGNED status', () => {
      expect(getStatusColor('ASSIGNED')).toBe('#007bff');
    });

    it('should return yellow for IN_PROGRESS status', () => {
      expect(getStatusColor('IN_PROGRESS')).toBe('#ffc107');
    });

    it('should return green for COMPLETED status', () => {
      expect(getStatusColor('COMPLETED')).toBe('#28a745');
    });

    it('should return gray for UNASSIGNED status', () => {
      expect(getStatusColor('UNASSIGNED')).toBe('#6c757d');
    });

    it('should return gray for unknown status', () => {
      expect(getStatusColor('UNKNOWN')).toBe('#6c757d');
    });
  });

  describe('formatDuration', () => {
    it('should format 0 minutes correctly', () => {
      expect(formatDuration(0)).toBe('0m');
    });

    it('should format minutes only', () => {
      expect(formatDuration(45)).toBe('45m');
    });

    it('should format hours only', () => {
      expect(formatDuration(120)).toBe('2h');
    });

    it('should format hours and minutes', () => {
      expect(formatDuration(90)).toBe('1h 30m');
    });

    it('should handle undefined/null as 0', () => {
      expect(formatDuration(null)).toBe('0m');
      expect(formatDuration(undefined)).toBe('0m');
    });

    it('should format large durations correctly', () => {
      expect(formatDuration(185)).toBe('3h 5m');
    });
  });

  describe('sortTasksByPriority', () => {
    it('should sort tasks by priority order', () => {
      const tasks = [
        { id: 1, priority: 'LOW', createdAt: '2025-11-21T10:00:00' },
        { id: 2, priority: 'CRITICAL', createdAt: '2025-11-21T10:00:00' },
        { id: 3, priority: 'MEDIUM', createdAt: '2025-11-21T10:00:00' },
        { id: 4, priority: 'HIGH', createdAt: '2025-11-21T10:00:00' },
      ];

      const sorted = sortTasksByPriority(tasks);

      expect(sorted[0].priority).toBe('CRITICAL');
      expect(sorted[1].priority).toBe('HIGH');
      expect(sorted[2].priority).toBe('MEDIUM');
      expect(sorted[3].priority).toBe('LOW');
    });

    it('should sort by creation date when priorities are equal', () => {
      const tasks = [
        { id: 1, priority: 'HIGH', createdAt: '2025-11-21T09:00:00' },
        { id: 2, priority: 'HIGH', createdAt: '2025-11-21T11:00:00' },
        { id: 3, priority: 'HIGH', createdAt: '2025-11-21T10:00:00' },
      ];

      const sorted = sortTasksByPriority(tasks);

      expect(sorted[0].id).toBe(2); // newest first
      expect(sorted[1].id).toBe(3);
      expect(sorted[2].id).toBe(1);
    });

    it('should not mutate the original array', () => {
      const tasks = [
        { id: 1, priority: 'LOW', createdAt: '2025-11-21T10:00:00' },
        { id: 2, priority: 'HIGH', createdAt: '2025-11-21T10:00:00' },
      ];

      const original = [...tasks];
      sortTasksByPriority(tasks);

      expect(tasks).toEqual(original);
    });

    it('should handle empty array', () => {
      const sorted = sortTasksByPriority([]);
      expect(sorted).toEqual([]);
    });

    it('should handle single task', () => {
      const tasks = [{ id: 1, priority: 'MEDIUM', createdAt: '2025-11-21T10:00:00' }];
      const sorted = sortTasksByPriority(tasks);
      expect(sorted).toEqual(tasks);
    });
  });
});
