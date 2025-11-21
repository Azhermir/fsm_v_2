import React from 'react';
import { render, fireEvent, waitFor } from '@testing-library/react-native';
import TaskDetailScreen from './TaskDetailScreen';
import * as Location from 'expo-location';

// Mock dependencies
jest.mock('expo-location');

// Mock react-native-maps
jest.mock('react-native-maps', () => {
  const React = require('react');
  const { View } = require('react-native');
  return {
    __esModule: true,
    default: ({ children, ...props }) => <View {...props}>{children}</View>,
    Marker: ({ children, ...props }) => <View {...props}>{children}</View>,
  };
});

// Import mocked modules after setting up mocks
const Linking = require('react-native').Linking;
const Alert = require('react-native').Alert;
const Platform = require('react-native').Platform;

// Create spy functions
const mockCanOpenURL = jest.fn();
const mockOpenURL = jest.fn();
const mockAlert = jest.fn();

// Assign to the imported modules
Linking.canOpenURL = mockCanOpenURL;
Linking.openURL = mockOpenURL;
Alert.alert = mockAlert;

describe('TaskDetailScreen', () => {
  const mockTask = {
    id: 1,
    title: 'Fix HVAC System',
    description: 'Air conditioning not working properly',
    clientAddress: '123 Main St, Springfield, IL 62701',
    priority: 'CRITICAL',
    estimatedDuration: 120,
    status: 'ASSIGNED',
    createdAt: '2025-11-21T10:00:00',
    instructions: 'Use safety equipment',
  };

  const mockRoute = {
    params: {
      task: mockTask,
    },
  };

  const mockNavigation = {
    goBack: jest.fn(),
    navigate: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
    
    // Mock location permission granted
    Location.requestForegroundPermissionsAsync.mockResolvedValue({
      status: 'granted',
    });
    
    // Mock current location
    Location.getCurrentPositionAsync.mockResolvedValue({
      coords: {
        latitude: 39.7817,
        longitude: -89.6501,
      },
    });

    // Mock Linking
    mockCanOpenURL.mockResolvedValue(true);
    mockOpenURL.mockResolvedValue(true);
    mockAlert.mockImplementation(() => {});
  });

  describe('Rendering', () => {
    it('should render task title correctly', async () => {
      const { getByText } = render(
        <TaskDetailScreen route={mockRoute} navigation={mockNavigation} />
      );

      await waitFor(() => {
        expect(getByText('Fix HVAC System')).toBeTruthy();
      });
    });

    it('should render task description', async () => {
      const { getByText } = render(
        <TaskDetailScreen route={mockRoute} navigation={mockNavigation} />
      );

      await waitFor(() => {
        expect(getByText('Air conditioning not working properly')).toBeTruthy();
      });
    });

    it('should render task address', async () => {
      const { getByText } = render(
        <TaskDetailScreen route={mockRoute} navigation={mockNavigation} />
      );

      await waitFor(() => {
        expect(getByText(/123 Main St, Springfield, IL 62701/)).toBeTruthy();
      });
    });

    it('should render priority badge', async () => {
      const { getByText } = render(
        <TaskDetailScreen route={mockRoute} navigation={mockNavigation} />
      );

      await waitFor(() => {
        expect(getByText('CRITICAL PRIORITY')).toBeTruthy();
      });
    });

    it('should render task duration', async () => {
      const { getByText } = render(
        <TaskDetailScreen route={mockRoute} navigation={mockNavigation} />
      );

      await waitFor(() => {
        expect(getByText(/2h/)).toBeTruthy();
      });
    });

    it('should render task status', async () => {
      const { getByText } = render(
        <TaskDetailScreen route={mockRoute} navigation={mockNavigation} />
      );

      await waitFor(() => {
        expect(getByText('ASSIGNED')).toBeTruthy();
      });
    });

    it('should render special instructions when provided', async () => {
      const { getByText } = render(
        <TaskDetailScreen route={mockRoute} navigation={mockNavigation} />
      );

      await waitFor(() => {
        expect(getByText('Use safety equipment')).toBeTruthy();
      });
    });

    it('should not render instructions section when not provided', async () => {
      const taskWithoutInstructions = { ...mockTask, instructions: undefined };
      const route = { params: { task: taskWithoutInstructions } };

      const { queryByText } = render(
        <TaskDetailScreen route={route} navigation={mockNavigation} />
      );

      await waitFor(() => {
        expect(queryByText('Special Instructions')).toBeNull();
      });
    });

    it('should not render description section when not provided', async () => {
      const taskWithoutDescription = { ...mockTask, description: undefined };
      const route = { params: { task: taskWithoutDescription } };

      const { queryByText } = render(
        <TaskDetailScreen route={route} navigation={mockNavigation} />
      );

      await waitFor(() => {
        expect(queryByText('Description')).toBeNull();
      });
    });

    it('should render navigate button', async () => {
      const { getByText } = render(
        <TaskDetailScreen route={mockRoute} navigation={mockNavigation} />
      );

      await waitFor(() => {
        expect(getByText(/Navigate to Location/)).toBeTruthy();
      });
    });
  });

  describe('Location and Distance', () => {
    it('should request location permission on mount', async () => {
      render(<TaskDetailScreen route={mockRoute} navigation={mockNavigation} />);

      await waitFor(() => {
        expect(Location.requestForegroundPermissionsAsync).toHaveBeenCalled();
      });
    });

    it('should get current location when permission granted', async () => {
      render(<TaskDetailScreen route={mockRoute} navigation={mockNavigation} />);

      await waitFor(() => {
        expect(Location.getCurrentPositionAsync).toHaveBeenCalled();
      });
    });

    it('should display distance when location is available', async () => {
      const { getByText } = render(
        <TaskDetailScreen route={mockRoute} navigation={mockNavigation} />
      );

      await waitFor(() => {
        expect(getByText(/Distance:/)).toBeTruthy();
        expect(getByText(/from your location/)).toBeTruthy();
      });
    });

    it('should handle location permission denied', async () => {
      Location.requestForegroundPermissionsAsync.mockResolvedValue({
        status: 'denied',
      });

      const { queryByText } = render(
        <TaskDetailScreen route={mockRoute} navigation={mockNavigation} />
      );

      await waitFor(() => {
        expect(Location.getCurrentPositionAsync).not.toHaveBeenCalled();
      });
    });

    it('should show alert on location error', async () => {
      Location.getCurrentPositionAsync.mockRejectedValue(
        new Error('Location error')
      );

      render(<TaskDetailScreen route={mockRoute} navigation={mockNavigation} />);

      await waitFor(() => {
        expect(mockAlert).toHaveBeenCalledWith(
          'Location Error',
          expect.any(String)
        );
      });
    });

    it('should display loading indicator while getting location', () => {
      const { getByText } = render(
        <TaskDetailScreen route={mockRoute} navigation={mockNavigation} />
      );

      expect(getByText('Getting your location...')).toBeTruthy();
    });
  });

  describe('Navigation', () => {
    it('should go back when back button is pressed', async () => {
      const { getByText } = render(
        <TaskDetailScreen route={mockRoute} navigation={mockNavigation} />
      );

      await waitFor(() => {
        fireEvent.press(getByText('← Back'));
      });

      expect(mockNavigation.goBack).toHaveBeenCalled();
    });

    it('should open maps app when navigate button is pressed on iOS', async () => {
      Platform.OS = 'ios';
      
      const { getByText } = render(
        <TaskDetailScreen route={mockRoute} navigation={mockNavigation} />
      );

      await waitFor(() => {
        const navigateButton = getByText(/Navigate to Location/);
        fireEvent.press(navigateButton);
      });

      await waitFor(() => {
        expect(mockCanOpenURL).toHaveBeenCalled();
        expect(mockOpenURL).toHaveBeenCalled();
      });
    });

    it('should open maps app when navigate button is pressed on Android', async () => {
      Platform.OS = 'android';
      
      const { getByText } = render(
        <TaskDetailScreen route={mockRoute} navigation={mockNavigation} />
      );

      await waitFor(() => {
        const navigateButton = getByText(/Navigate to Location/);
        fireEvent.press(navigateButton);
      });

      await waitFor(() => {
        expect(mockCanOpenURL).toHaveBeenCalled();
        expect(mockOpenURL).toHaveBeenCalled();
      });
    });

    it('should fallback to web maps if native app not supported', async () => {
      const { getByText } = render(
        <TaskDetailScreen route={mockRoute} navigation={mockNavigation} />
      );

      await waitFor(() => {
        expect(getByText(/Navigate to Location/)).toBeTruthy();
      });

      mockCanOpenURL.mockResolvedValue(false);
      
      const navigateButton = getByText(/Navigate to Location/);
      fireEvent.press(navigateButton);

      await waitFor(() => {
        expect(mockOpenURL).toHaveBeenCalledWith(
          expect.stringContaining('google.com/maps')
        );
      });
    });

    it('should show alert if unable to open maps', async () => {
      const { getByText } = render(
        <TaskDetailScreen route={mockRoute} navigation={mockNavigation} />
      );

      await waitFor(() => {
        expect(getByText(/Navigate to Location/)).toBeTruthy();
      });

      mockOpenURL.mockRejectedValue(new Error('Cannot open'));
      
      const navigateButton = getByText(/Navigate to Location/);
      fireEvent.press(navigateButton);

      await waitFor(() => {
        expect(mockAlert).toHaveBeenCalledWith('Error', expect.any(String));
      });
    });
  });

  describe('Task Priority and Status Display', () => {
    it('should render HIGH priority with correct color', async () => {
      const highPriorityTask = { ...mockTask, priority: 'HIGH' };
      const route = { params: { task: highPriorityTask } };

      const { getByText } = render(
        <TaskDetailScreen route={route} navigation={mockNavigation} />
      );

      await waitFor(() => {
        expect(getByText('HIGH PRIORITY')).toBeTruthy();
      });
    });

    it('should render MEDIUM priority with correct color', async () => {
      const mediumPriorityTask = { ...mockTask, priority: 'MEDIUM' };
      const route = { params: { task: mediumPriorityTask } };

      const { getByText } = render(
        <TaskDetailScreen route={route} navigation={mockNavigation} />
      );

      await waitFor(() => {
        expect(getByText('MEDIUM PRIORITY')).toBeTruthy();
      });
    });

    it('should render LOW priority with correct color', async () => {
      const lowPriorityTask = { ...mockTask, priority: 'LOW' };
      const route = { params: { task: lowPriorityTask } };

      const { getByText } = render(
        <TaskDetailScreen route={route} navigation={mockNavigation} />
      );

      await waitFor(() => {
        expect(getByText('LOW PRIORITY')).toBeTruthy();
      });
    });

    it('should render IN_PROGRESS status', async () => {
      const inProgressTask = { ...mockTask, status: 'IN_PROGRESS' };
      const route = { params: { task: inProgressTask } };

      const { getByText } = render(
        <TaskDetailScreen route={route} navigation={mockNavigation} />
      );

      await waitFor(() => {
        expect(getByText('IN PROGRESS')).toBeTruthy();
      });
    });

    it('should render COMPLETED status', async () => {
      const completedTask = { ...mockTask, status: 'COMPLETED' };
      const route = { params: { task: completedTask } };

      const { getByText } = render(
        <TaskDetailScreen route={route} navigation={mockNavigation} />
      );

      await waitFor(() => {
        expect(getByText('COMPLETED')).toBeTruthy();
      });
    });
  });

  describe('Date and Time Display', () => {
    it('should render created date when provided', async () => {
      const { getByText } = render(
        <TaskDetailScreen route={mockRoute} navigation={mockNavigation} />
      );

      await waitFor(() => {
        expect(getByText(/Created:/)).toBeTruthy();
      });
    });

    it('should not crash when created date is missing', async () => {
      const taskWithoutDate = { ...mockTask, createdAt: undefined };
      const route = { params: { task: taskWithoutDate } };

      expect(() => {
        render(<TaskDetailScreen route={route} navigation={mockNavigation} />);
      }).not.toThrow();
    });
  });

  describe('Duration Display', () => {
    it('should render duration in hours and minutes format', async () => {
      const { getByText } = render(
        <TaskDetailScreen route={mockRoute} navigation={mockNavigation} />
      );

      await waitFor(() => {
        expect(getByText(/2h/)).toBeTruthy();
      });
    });

    it('should render short duration correctly', async () => {
      const shortTask = { ...mockTask, estimatedDuration: 45 };
      const route = { params: { task: shortTask } };

      const { getByText } = render(
        <TaskDetailScreen route={route} navigation={mockNavigation} />
      );

      await waitFor(() => {
        expect(getByText(/45m/)).toBeTruthy();
      });
    });

    it('should render long duration correctly', async () => {
      const longTask = { ...mockTask, estimatedDuration: 195 };
      const route = { params: { task: longTask } };

      const { getByText } = render(
        <TaskDetailScreen route={route} navigation={mockNavigation} />
      );

      await waitFor(() => {
        expect(getByText(/3h 15m/)).toBeTruthy();
      });
    });
  });
});
