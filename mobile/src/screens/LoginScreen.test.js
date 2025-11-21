import React from 'react';
import { render, fireEvent, waitFor } from '@testing-library/react-native';
import { Alert } from 'react-native';
import LoginScreen from './LoginScreen';
import { useAuth } from '../context/AuthContext';

// Mock the useAuth hook
jest.mock('../context/AuthContext', () => ({
  useAuth: jest.fn(),
}));

// Mock Alert
jest.spyOn(Alert, 'alert');

describe('LoginScreen', () => {
  const mockLogin = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    useAuth.mockReturnValue({
      login: mockLogin,
    });
    Alert.alert.mockImplementation(() => {});
  });

  it('should render login form correctly', () => {
    const { getByText, getByPlaceholderText } = render(<LoginScreen />);

    expect(getByText('FSM Mobile')).toBeTruthy();
    expect(getByText('Field Service Management')).toBeTruthy();
    expect(getByText('Technician ID')).toBeTruthy();
    expect(getByText('Name')).toBeTruthy();
    expect(getByText('Login')).toBeTruthy();
    expect(getByPlaceholderText('Enter your technician ID')).toBeTruthy();
    expect(getByPlaceholderText('Enter your name')).toBeTruthy();
  });

  it('should display demo credentials', () => {
    const { getByText } = render(<LoginScreen />);

    expect(getByText('Demo Credentials:')).toBeTruthy();
    expect(getByText('Technician ID: tech-001')).toBeTruthy();
    expect(getByText('Name: John Smith')).toBeTruthy();
  });

  it('should update technician ID when typed', () => {
    const { getByPlaceholderText } = render(<LoginScreen />);
    const input = getByPlaceholderText('Enter your technician ID');

    fireEvent.changeText(input, 'tech-001');

    expect(input.props.value).toBe('tech-001');
  });

  it('should update name when typed', () => {
    const { getByPlaceholderText } = render(<LoginScreen />);
    const input = getByPlaceholderText('Enter your name');

    fireEvent.changeText(input, 'John Smith');

    expect(input.props.value).toBe('John Smith');
  });

  it('should show alert if technician ID is empty', () => {
    const { getByPlaceholderText, getByText } = render(<LoginScreen />);

    const nameInput = getByPlaceholderText('Enter your name');
    fireEvent.changeText(nameInput, 'John Smith');

    const loginButton = getByText('Login');
    fireEvent.press(loginButton);

    expect(Alert.alert).toHaveBeenCalledWith('Error', 'Please enter your Technician ID');
    expect(mockLogin).not.toHaveBeenCalled();
  });

  it('should show alert if name is empty', () => {
    const { getByPlaceholderText, getByText } = render(<LoginScreen />);

    const techIdInput = getByPlaceholderText('Enter your technician ID');
    fireEvent.changeText(techIdInput, 'tech-001');

    const loginButton = getByText('Login');
    fireEvent.press(loginButton);

    expect(Alert.alert).toHaveBeenCalledWith('Error', 'Please enter your name');
    expect(mockLogin).not.toHaveBeenCalled();
  });

  it('should trim whitespace from inputs', async () => {
    const { getByPlaceholderText, getByText } = render(<LoginScreen />);

    const techIdInput = getByPlaceholderText('Enter your technician ID');
    const nameInput = getByPlaceholderText('Enter your name');

    fireEvent.changeText(techIdInput, '  tech-001  ');
    fireEvent.changeText(nameInput, '  John Smith  ');

    const loginButton = getByText('Login');
    fireEvent.press(loginButton);

    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith('tech-001', 'John Smith');
    });
  });

  it('should call login with correct credentials', async () => {
    mockLogin.mockResolvedValueOnce();

    const { getByPlaceholderText, getByText } = render(<LoginScreen />);

    const techIdInput = getByPlaceholderText('Enter your technician ID');
    const nameInput = getByPlaceholderText('Enter your name');

    fireEvent.changeText(techIdInput, 'tech-001');
    fireEvent.changeText(nameInput, 'John Smith');

    const loginButton = getByText('Login');
    fireEvent.press(loginButton);

    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith('tech-001', 'John Smith');
    });
  });

  it('should show loading state during login', async () => {
    let resolveLogin;
    mockLogin.mockReturnValue(new Promise((resolve) => {
      resolveLogin = resolve;
    }));

    const { getByPlaceholderText, getByText } = render(<LoginScreen />);

    const techIdInput = getByPlaceholderText('Enter your technician ID');
    const nameInput = getByPlaceholderText('Enter your name');

    fireEvent.changeText(techIdInput, 'tech-001');
    fireEvent.changeText(nameInput, 'John Smith');

    const loginButton = getByText('Login');
    fireEvent.press(loginButton);

    await waitFor(() => {
      expect(getByText('Logging in...')).toBeTruthy();
    });

    resolveLogin();
  });

  it('should disable inputs during login', async () => {
    let resolveLogin;
    mockLogin.mockReturnValue(new Promise((resolve) => {
      resolveLogin = resolve;
    }));

    const { getByPlaceholderText, getByText } = render(<LoginScreen />);

    const techIdInput = getByPlaceholderText('Enter your technician ID');
    const nameInput = getByPlaceholderText('Enter your name');

    fireEvent.changeText(techIdInput, 'tech-001');
    fireEvent.changeText(nameInput, 'John Smith');

    const loginButton = getByText('Login');
    fireEvent.press(loginButton);

    await waitFor(() => {
      expect(techIdInput.props.editable).toBe(false);
      expect(nameInput.props.editable).toBe(false);
    });

    resolveLogin();
  });

  it('should handle login errors gracefully', async () => {
    mockLogin.mockRejectedValueOnce(new Error('Login failed'));

    const { getByPlaceholderText, getByText } = render(<LoginScreen />);

    const techIdInput = getByPlaceholderText('Enter your technician ID');
    const nameInput = getByPlaceholderText('Enter your name');

    fireEvent.changeText(techIdInput, 'tech-001');
    fireEvent.changeText(nameInput, 'John Smith');

    const loginButton = getByText('Login');
    fireEvent.press(loginButton);

    await waitFor(() => {
      expect(Alert.alert).toHaveBeenCalledWith('Error', 'Failed to login. Please try again.');
    });

    // Should re-enable inputs after error
    await waitFor(() => {
      expect(getByText('Login')).toBeTruthy();
    });
  });

  it('should render FSM emoji logo', () => {
    const { getByText } = render(<LoginScreen />);

    expect(getByText('🔧')).toBeTruthy();
  });
});
