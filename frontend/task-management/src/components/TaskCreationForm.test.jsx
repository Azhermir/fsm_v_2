import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import TaskCreationForm from './TaskCreationForm';
import * as geocodingService from '../services/geocodingService';

// Mock the geocoding service
vi.mock('../services/geocodingService', () => ({
  searchAddresses: vi.fn(),
  geocodeAddress: vi.fn(),
  validateAddress: vi.fn(),
}));

describe('TaskCreationForm', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    global.fetch = vi.fn();
    geocodingService.searchAddresses.mockResolvedValue([]);
    geocodingService.geocodeAddress.mockResolvedValue({
      lat: 40.7128,
      lng: -74.0060,
      success: true,
    });
  });

  it('should render the form with all required fields', () => {
    render(<TaskCreationForm />);
    
    expect(screen.getByText('Create New Service Task')).toBeInTheDocument();
    expect(screen.getByLabelText('Title')).toBeInTheDocument();
    expect(screen.getByLabelText('Description')).toBeInTheDocument();
    expect(screen.getByLabelText('Client Address')).toBeInTheDocument();
    expect(screen.getByLabelText('Priority')).toBeInTheDocument();
    expect(screen.getByLabelText('Estimated Duration (minutes)')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Create Task' })).toBeInTheDocument();
  });

  it('should render priority dropdown with all options', () => {
    render(<TaskCreationForm />);
    
    const prioritySelect = screen.getByLabelText('Priority');
    expect(prioritySelect).toBeInTheDocument();
    
    const options = screen.getAllByRole('option');
    expect(options).toHaveLength(4);
    expect(screen.getByRole('option', { name: 'Low' })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'Medium' })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'High' })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'Critical' })).toBeInTheDocument();
  });

  it('should have MEDIUM as default priority', () => {
    render(<TaskCreationForm />);
    
    const prioritySelect = screen.getByLabelText('Priority');
    expect(prioritySelect.value).toBe('MEDIUM');
  });

  it('should update form fields when user types', async () => {
    const user = userEvent.setup();
    render(<TaskCreationForm />);
    
    const titleInput = screen.getByLabelText('Title');
    const descriptionInput = screen.getByLabelText('Description');
    const durationInput = screen.getByLabelText('Estimated Duration (minutes)');
    
    await user.type(titleInput, 'Test Title');
    await user.type(descriptionInput, 'Test Description');
    await user.type(durationInput, '60');
    
    expect(titleInput.value).toBe('Test Title');
    expect(descriptionInput.value).toBe('Test Description');
    expect(durationInput.value).toBe('60');
  });

  it('should update priority when user selects different option', async () => {
    const user = userEvent.setup();
    render(<TaskCreationForm />);
    
    const prioritySelect = screen.getByLabelText('Priority');
    
    await user.selectOptions(prioritySelect, 'HIGH');
    expect(prioritySelect.value).toBe('HIGH');
    
    await user.selectOptions(prioritySelect, 'LOW');
    expect(prioritySelect.value).toBe('LOW');
  });

  it('should disable submit button when form is invalid', () => {
    render(<TaskCreationForm />);
    
    const submitButton = screen.getByRole('button', { name: 'Create Task' });
    expect(submitButton).toBeDisabled();
  });

  it('should enable submit button when all fields are valid', async () => {
    const user = userEvent.setup();
    render(<TaskCreationForm />);
    
    await user.type(screen.getByLabelText('Title'), 'Test Title');
    await user.type(screen.getByLabelText('Description'), 'Test Description');
    
    const addressInput = screen.getByPlaceholderText('Start typing an address...');
    await user.type(addressInput, '123 Main St');
    
    await user.type(screen.getByLabelText('Estimated Duration (minutes)'), '60');
    
    const submitButton = screen.getByRole('button', { name: 'Create Task' });
    expect(submitButton).not.toBeDisabled();
  });

  it('should show error messages when submitting empty form', async () => {
    const user = userEvent.setup();
    render(<TaskCreationForm />);
    
    const submitButton = screen.getByRole('button', { name: 'Create Task' });
    expect(submitButton).toBeDisabled();
    
    const form = screen.getByRole('button', { name: 'Create Task' }).closest('form');
    fireEvent.submit(form);
    
    await waitFor(() => {
      expect(screen.getByText('Title is required')).toBeInTheDocument();
      expect(screen.getByText('Description is required')).toBeInTheDocument();
      expect(screen.getByText('Client address is required')).toBeInTheDocument();
      expect(screen.getByText('Estimated duration is required')).toBeInTheDocument();
    });
  });

  it('should search for addresses when user types', async () => {
    const user = userEvent.setup();
    geocodingService.searchAddresses.mockResolvedValue([
      { description: '123 Main St', placeId: '1' },
    ]);
    
    render(<TaskCreationForm />);
    
    const addressInput = screen.getByPlaceholderText('Start typing an address...');
    await user.type(addressInput, '123');
    
    await waitFor(() => {
      expect(geocodingService.searchAddresses).toHaveBeenCalledWith('123');
    });
  });

  it('should geocode address when selected from suggestions', async () => {
    const user = userEvent.setup();
    geocodingService.searchAddresses.mockResolvedValue([
      { description: '123 Main St, New York, NY', placeId: '1' },
    ]);
    geocodingService.geocodeAddress.mockResolvedValue({
      lat: 40.7128,
      lng: -74.0060,
      success: true,
    });
    
    render(<TaskCreationForm />);
    
    const addressInput = screen.getByPlaceholderText('Start typing an address...');
    await user.type(addressInput, '123 Main');
    
    await waitFor(() => {
      expect(screen.getByText('123 Main St, New York, NY')).toBeInTheDocument();
    });
    
    await user.click(screen.getByText('123 Main St, New York, NY'));
    
    await waitFor(() => {
      expect(geocodingService.geocodeAddress).toHaveBeenCalledWith('123 Main St, New York, NY');
      expect(screen.getByText(/Coordinates: 40.7128, -74.0060/)).toBeInTheDocument();
    });
  });

  it('should show geocoding error if address cannot be geocoded', async () => {
    const user = userEvent.setup();
    geocodingService.geocodeAddress.mockResolvedValue({
      success: false,
    });
    
    render(<TaskCreationForm />);
    
    const addressInput = screen.getByPlaceholderText('Start typing an address...');
    await user.type(addressInput, 'Invalid Address');
    
    await user.type(screen.getByLabelText('Title'), 'Test Title');
    await user.type(screen.getByLabelText('Description'), 'Test Description');
    await user.type(screen.getByLabelText('Estimated Duration (minutes)'), '60');
    
    const submitButton = screen.getByRole('button', { name: 'Create Task' });
    await user.click(submitButton);
    
    await waitFor(() => {
      expect(screen.getByText(/Unable to geocode address/)).toBeInTheDocument();
    });
  });

  it('should call API with coordinates on form submit', async () => {
    const user = userEvent.setup();
    global.fetch = vi.fn(() =>
      Promise.resolve({
        ok: true,
        json: () => Promise.resolve({ id: '123' }),
      })
    );
    
    geocodingService.geocodeAddress.mockResolvedValue({
      lat: 40.7128,
      lng: -74.0060,
      success: true,
    });
    
    render(<TaskCreationForm />);
    
    await user.type(screen.getByLabelText('Title'), 'Fix HVAC');
    await user.type(screen.getByLabelText('Description'), 'AC not working');
    
    const addressInput = screen.getByPlaceholderText('Start typing an address...');
    await user.type(addressInput, '456 Oak Ave');
    
    await user.selectOptions(screen.getByLabelText('Priority'), 'HIGH');
    await user.type(screen.getByLabelText('Estimated Duration (minutes)'), '120');
    
    const submitButton = screen.getByRole('button', { name: 'Create Task' });
    await user.click(submitButton);
    
    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/tasks',
        expect.objectContaining({
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            title: 'Fix HVAC',
            description: 'AC not working',
            clientAddress: '456 Oak Ave',
            priority: 'HIGH',
            estimatedDuration: 120,
            latitude: 40.7128,
            longitude: -74.0060,
          }),
        })
      );
    });
  });

  it('should show success message after successful task creation', async () => {
    const user = userEvent.setup();
    global.fetch = vi.fn(() =>
      Promise.resolve({
        ok: true,
        json: () => Promise.resolve({ id: '123' }),
      })
    );
    
    render(<TaskCreationForm />);
    
    await user.type(screen.getByLabelText('Title'), 'Test Task');
    await user.type(screen.getByLabelText('Description'), 'Test Description');
    
    const addressInput = screen.getByPlaceholderText('Start typing an address...');
    await user.type(addressInput, 'Test Address');
    
    await user.type(screen.getByLabelText('Estimated Duration (minutes)'), '60');
    
    const submitButton = screen.getByRole('button', { name: 'Create Task' });
    await user.click(submitButton);
    
    await waitFor(() => {
      expect(screen.getByText('Task created successfully!')).toBeInTheDocument();
    });
  });

  it('should clear form after successful submission', async () => {
    const user = userEvent.setup();
    global.fetch = vi.fn(() =>
      Promise.resolve({
        ok: true,
        json: () => Promise.resolve({ id: '123' }),
      })
    );
    
    render(<TaskCreationForm />);
    
    await user.type(screen.getByLabelText('Title'), 'Test Task');
    await user.type(screen.getByLabelText('Description'), 'Test Description');
    
    const addressInput = screen.getByPlaceholderText('Start typing an address...');
    await user.type(addressInput, 'Test Address');
    
    await user.type(screen.getByLabelText('Estimated Duration (minutes)'), '60');
    
    const submitButton = screen.getByRole('button', { name: 'Create Task' });
    await user.click(submitButton);
    
    await waitFor(() => {
      expect(screen.getByLabelText('Title').value).toBe('');
      expect(screen.getByLabelText('Description').value).toBe('');
      expect(screen.getByPlaceholderText('Start typing an address...').value).toBe('');
      expect(screen.getByLabelText('Priority').value).toBe('MEDIUM');
      expect(screen.getByLabelText('Estimated Duration (minutes)').value).toBe('');
    });
  });

  it('should disable submit button while submitting', async () => {
    const user = userEvent.setup();
    let resolvePromise;
    global.fetch = vi.fn(() => new Promise((resolve) => {
      resolvePromise = resolve;
    }));
    
    render(<TaskCreationForm />);
    
    await user.type(screen.getByLabelText('Title'), 'Test Task');
    await user.type(screen.getByLabelText('Description'), 'Test Description');
    
    const addressInput = screen.getByPlaceholderText('Start typing an address...');
    await user.type(addressInput, 'Test Address');
    
    await user.type(screen.getByLabelText('Estimated Duration (minutes)'), '60');
    
    const submitButton = screen.getByRole('button', { name: 'Create Task' });
    await user.click(submitButton);
    
    expect(screen.getByRole('button', { name: 'Creating...' })).toBeDisabled();
    
    resolvePromise({
      ok: true,
      json: () => Promise.resolve({ id: '123' }),
    });
    
    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Create Task' })).toBeInTheDocument();
    });
  });

  it('should handle API error gracefully', async () => {
    const user = userEvent.setup();
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {});
    
    global.fetch = vi.fn(() =>
      Promise.resolve({
        ok: false,
        status: 400,
      })
    );
    
    render(<TaskCreationForm />);
    
    await user.type(screen.getByLabelText('Title'), 'Test');
    await user.type(screen.getByLabelText('Description'), 'Test Desc');
    
    const addressInput = screen.getByPlaceholderText('Start typing an address...');
    await user.type(addressInput, 'Test Addr');
    
    await user.type(screen.getByLabelText('Estimated Duration (minutes)'), '30');
    
    const submitButton = screen.getByRole('button', { name: 'Create Task' });
    await user.click(submitButton);
    
    await waitFor(() => {
      expect(consoleError).toHaveBeenCalledWith('Failed to create task');
    });
    
    consoleError.mockRestore();
  });

  it('should convert estimatedDuration to integer before API call', async () => {
    const user = userEvent.setup();
    global.fetch = vi.fn(() =>
      Promise.resolve({
        ok: true,
        json: () => Promise.resolve({ id: '123' }),
      })
    );
    
    render(<TaskCreationForm />);
    
    await user.type(screen.getByLabelText('Title'), 'Test');
    await user.type(screen.getByLabelText('Description'), 'Test');
    
    const addressInput = screen.getByPlaceholderText('Start typing an address...');
    await user.type(addressInput, 'Test');
    
    await user.type(screen.getByLabelText('Estimated Duration (minutes)'), '45');
    
    const submitButton = screen.getByRole('button', { name: 'Create Task' });
    await user.click(submitButton);
    
    await waitFor(() => {
      const callArgs = global.fetch.mock.calls[0];
      const body = JSON.parse(callArgs[1].body);
      expect(body.estimatedDuration).toBe(45);
      expect(typeof body.estimatedDuration).toBe('number');
    });
  });

  it('should render all priority enum values correctly', () => {
    render(<TaskCreationForm />);
    
    const prioritySelect = screen.getByLabelText('Priority');
    const options = Array.from(prioritySelect.options).map(opt => opt.value);
    
    expect(options).toContain('LOW');
    expect(options).toContain('MEDIUM');
    expect(options).toContain('HIGH');
    expect(options).toContain('CRITICAL');
  });
});
