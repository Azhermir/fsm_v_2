import {
  fetchTechnicianTasks,
  updateTaskStatus,
  getPriorityColor,
  getStatusColor,
  formatDuration,
  sortTasksByPriority,
  calculateDistance,
  formatDistance,
  parseAddressCoordinates,
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

  describe('updateTaskStatus', () => {
    it('should update task status to IN_PROGRESS successfully', async () => {
      const mockResponse = {
        id: 1,
        title: 'Fix HVAC',
        status: 'IN_PROGRESS',
      };

      global.fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockResponse,
      });

      const result = await updateTaskStatus(1, 'IN_PROGRESS');

      expect(global.fetch).toHaveBeenCalledWith(
        expect.stringContaining('/tasks/1/status'),
        expect.objectContaining({
          method: 'PATCH',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ status: 'IN_PROGRESS' }),
        })
      );
      expect(result).toEqual(mockResponse);
    });

    it('should update task status to COMPLETED with work summary', async () => {
      const mockResponse = {
        id: 1,
        title: 'Fix HVAC',
        status: 'COMPLETED',
        workSummary: 'Fixed the system',
      };

      global.fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockResponse,
      });

      const result = await updateTaskStatus(1, 'COMPLETED', 'Fixed the system');

      expect(global.fetch).toHaveBeenCalledWith(
        expect.stringContaining('/tasks/1/status'),
        expect.objectContaining({
          method: 'PATCH',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            status: 'COMPLETED',
            workSummary: 'Fixed the system',
          }),
        })
      );
      expect(result).toEqual(mockResponse);
    });

    it('should throw error on HTTP error', async () => {
      global.fetch.mockResolvedValueOnce({
        ok: false,
        status: 400,
        json: async () => ({ message: 'Invalid status transition' }),
      });

      await expect(updateTaskStatus(1, 'COMPLETED')).rejects.toThrow('Invalid status transition');
    });

    it('should throw error on network error', async () => {
      global.fetch.mockRejectedValueOnce(new Error('Network error'));

      await expect(updateTaskStatus(1, 'IN_PROGRESS')).rejects.toThrow('Network error');
    });

    it('should handle error response without JSON', async () => {
      global.fetch.mockResolvedValueOnce({
        ok: false,
        status: 500,
        json: async () => {
          throw new Error('Not JSON');
        },
      });

      await expect(updateTaskStatus(1, 'IN_PROGRESS')).rejects.toThrow('HTTP error! status: 500');
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

  describe('calculateDistance', () => {
    it('should calculate distance between two points correctly', () => {
      // Test with known coordinates: Springfield, IL to Chicago, IL
      const springfieldLat = 39.7817;
      const springfieldLon = -89.6501;
      const chicagoLat = 41.8781;
      const chicagoLon = -87.6298;

      const distance = calculateDistance(
        springfieldLat,
        springfieldLon,
        chicagoLat,
        chicagoLon
      );

      // Distance should be approximately 288 km
      expect(distance).toBeGreaterThan(285);
      expect(distance).toBeLessThan(295);
    });

    it('should return 0 for same coordinates', () => {
      const distance = calculateDistance(39.7817, -89.6501, 39.7817, -89.6501);
      expect(distance).toBe(0);
    });

    it('should calculate short distances correctly', () => {
      const lat1 = 39.7817;
      const lon1 = -89.6501;
      const lat2 = 39.7827; // ~1km north
      const lon2 = -89.6501;

      const distance = calculateDistance(lat1, lon1, lat2, lon2);

      expect(distance).toBeGreaterThan(0);
      expect(distance).toBeLessThan(2);
    });

    it('should handle negative coordinates', () => {
      const distance = calculateDistance(-33.8688, 151.2093, -37.8136, 144.9631);
      
      // Distance between Sydney and Melbourne
      expect(distance).toBeGreaterThan(700);
      expect(distance).toBeLessThan(750);
    });
  });

  describe('formatDistance', () => {
    it('should format distance less than 1km in meters', () => {
      expect(formatDistance(0.5)).toBe('500m');
      expect(formatDistance(0.25)).toBe('250m');
      expect(formatDistance(0.123)).toBe('123m');
    });

    it('should format distance in kilometers with one decimal', () => {
      expect(formatDistance(1.5)).toBe('1.5km');
      expect(formatDistance(10.7)).toBe('10.7km');
      expect(formatDistance(100.234)).toBe('100.2km');
    });

    it('should format distance exactly 1km correctly', () => {
      expect(formatDistance(1)).toBe('1.0km');
    });

    it('should round meters to nearest integer', () => {
      expect(formatDistance(0.1234)).toBe('123m');
      expect(formatDistance(0.9876)).toBe('988m');
    });

    it('should format very small distances', () => {
      expect(formatDistance(0.001)).toBe('1m');
      expect(formatDistance(0.0001)).toBe('0m');
    });

    it('should format large distances', () => {
      expect(formatDistance(1234.567)).toBe('1234.6km');
    });
  });

  describe('parseAddressCoordinates', () => {
    it('should return coordinates for an address', () => {
      const coords = parseAddressCoordinates('123 Main St, Springfield, IL 62701');
      
      expect(coords).toHaveProperty('latitude');
      expect(coords).toHaveProperty('longitude');
      expect(typeof coords.latitude).toBe('number');
      expect(typeof coords.longitude).toBe('number');
    });

    it('should return coordinates in Springfield, IL area', () => {
      const coords = parseAddressCoordinates('123 Main St, Springfield, IL 62701');
      
      // Coordinates should be near Springfield, IL
      expect(coords.latitude).toBeGreaterThan(39.7);
      expect(coords.latitude).toBeLessThan(39.9);
      expect(coords.longitude).toBeGreaterThan(-89.8);
      expect(coords.longitude).toBeLessThan(-89.5);
    });

    it('should return different coordinates for different addresses', () => {
      const coords1 = parseAddressCoordinates('123 Main St, Springfield, IL 62701');
      const coords2 = parseAddressCoordinates('456 Oak Ave, Springfield, IL 62702');
      
      expect(coords1.latitude).not.toBe(coords2.latitude);
      expect(coords1.longitude).not.toBe(coords2.longitude);
    });

    it('should return consistent coordinates for same address', () => {
      const address = '789 Pine Rd, Springfield, IL 62703';
      const coords1 = parseAddressCoordinates(address);
      const coords2 = parseAddressCoordinates(address);
      
      expect(coords1.latitude).toBe(coords2.latitude);
      expect(coords1.longitude).toBe(coords2.longitude);
    });

    it('should handle empty address', () => {
      const coords = parseAddressCoordinates('');
      
      expect(coords).toHaveProperty('latitude');
      expect(coords).toHaveProperty('longitude');
    });

    it('should generate reasonable coordinate offsets', () => {
      const coords = parseAddressCoordinates('123 Main St, Springfield, IL 62701');
      const baseLatitude = 39.7817;
      const baseLongitude = -89.6501;
      
      // Offsets should be within reasonable range (±0.05 degrees for lat, ±0.075 for lon)
      expect(Math.abs(coords.latitude - baseLatitude)).toBeLessThan(0.06);
      expect(Math.abs(coords.longitude - baseLongitude)).toBeLessThan(0.08);
    });
  });
});
