import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import TaskCreationForm from './TaskCreationForm';

describe('TaskCreationForm', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    global.fetch = vi.fn();
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
    const addressInput = screen.getByLabelText('Client Address');
    const durationInput = screen.getByLabelText('Estimated Duration (minutes)');
    
    await user.type(titleInput, 'Test Title');
    await user.type(descriptionInput, 'Test Description');
    await user.type(addressInput, '123 Main St');
    await user.type(durationInput, '60');
    
    expect(titleInput.value).toBe('Test Title');
    expect(descriptionInput.value).toBe('Test Description');
    expect(addressInput.value).toBe('123 Main St');
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
    await user.type(screen.getByLabelText('Client Address'), '123 Main St');
    await user.type(screen.getByLabelText('Estimated Duration (minutes)'), '60');
    
    const submitButton = screen.getByRole('button', { name: 'Create Task' });
    expect(submitButton).not.toBeDisabled();
  });

  it('should show error messages when submitting empty form', async () => {
    const user = userEvent.setup();
    render(<TaskCreationForm />);
    
    const submitButton = screen.getByRole('button', { name: 'Create Task' });
    expect(submitButton).toBeDisabled();
    
    // Force click on the form submit (simulating Enter key press)
    const form = screen.getByRole('button', { name: 'Create Task' }).closest('form');
    fireEvent.submit(form);
    
    await waitFor(() => {
      expect(screen.getByText('Title is required')).toBeInTheDocument();
      expect(screen.getByText('Description is required')).toBeInTheDocument();
      expect(screen.getByText('Client address is required')).toBeInTheDocument();
      expect(screen.getByText('Estimated duration is required')).toBeInTheDocument();
    });
  });

  it('should clear error message when user starts typing', async () => {
    const user = userEvent.setup();
    render(<TaskCreationForm />);
    
    const form = screen.getByRole('button', { name: 'Create Task' }).closest('form');
    fireEvent.submit(form);
    
    await waitFor(() => {
      expect(screen.getByText('Title is required')).toBeInTheDocument();
    });
    
    const titleInput = screen.getByLabelText('Title');
    await user.type(titleInput, 'T');
    
    expect(screen.queryByText('Title is required')).not.toBeInTheDocument();
  });

  it('should show error for negative estimated duration', async () => {
    const user = userEvent.setup();
    render(<TaskCreationForm />);
    
    const durationInput = screen.getByLabelText('Estimated Duration (minutes)');
    await user.type(durationInput, '-5');
    
    const submitButton = screen.getByRole('button', { name: 'Create Task' });
    expect(submitButton).toBeDisabled();
  });

  it('should show error for zero estimated duration', async () => {
    const user = userEvent.setup();
    render(<TaskCreationForm />);
    
    const durationInput = screen.getByLabelText('Estimated Duration (minutes)');
    await user.type(durationInput, '0');
    
    const submitButton = screen.getByRole('button', { name: 'Create Task' });
    expect(submitButton).toBeDisabled();
  });

  it('should call API with correct data on form submit', async () => {
    const user = userEvent.setup();
    global.fetch = vi.fn(() =>
      Promise.resolve({
        ok: true,
        json: () => Promise.resolve({ id: '123' }),
      })
    );
    
    render(<TaskCreationForm />);
    
    await user.type(screen.getByLabelText('Title'), 'Fix HVAC');
    await user.type(screen.getByLabelText('Description'), 'AC not working');
    await user.type(screen.getByLabelText('Client Address'), '456 Oak Ave');
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
    await user.type(screen.getByLabelText('Client Address'), 'Test Address');
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
    await user.type(screen.getByLabelText('Client Address'), 'Test Address');
    await user.type(screen.getByLabelText('Estimated Duration (minutes)'), '60');
    
    const submitButton = screen.getByRole('button', { name: 'Create Task' });
    await user.click(submitButton);
    
    await waitFor(() => {
      expect(screen.getByLabelText('Title').value).toBe('');
      expect(screen.getByLabelText('Description').value).toBe('');
      expect(screen.getByLabelText('Client Address').value).toBe('');
      expect(screen.getByLabelText('Priority').value).toBe('MEDIUM');
      expect(screen.getByLabelText('Estimated Duration (minutes)').value).toBe('');
    });
  });

  it('should clear success message when user modifies form', async () => {
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
    await user.type(screen.getByLabelText('Client Address'), 'Test Address');
    await user.type(screen.getByLabelText('Estimated Duration (minutes)'), '60');
    
    const submitButton = screen.getByRole('button', { name: 'Create Task' });
    await user.click(submitButton);
    
    await waitFor(() => {
      expect(screen.getByText('Task created successfully!')).toBeInTheDocument();
    });
    
    // Start typing in title field
    await user.type(screen.getByLabelText('Title'), 'N');
    
    expect(screen.queryByText('Task created successfully!')).not.toBeInTheDocument();
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
    await user.type(screen.getByLabelText('Client Address'), 'Test Address');
    await user.type(screen.getByLabelText('Estimated Duration (minutes)'), '60');
    
    const submitButton = screen.getByRole('button', { name: 'Create Task' });
    await user.click(submitButton);
    
    expect(screen.getByRole('button', { name: 'Creating...' })).toBeDisabled();
    
    // Resolve the promise
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
    await user.type(screen.getByLabelText('Client Address'), 'Test Addr');
    await user.type(screen.getByLabelText('Estimated Duration (minutes)'), '30');
    
    const submitButton = screen.getByRole('button', { name: 'Create Task' });
    await user.click(submitButton);
    
    await waitFor(() => {
      expect(consoleError).toHaveBeenCalledWith('Failed to create task');
    });
    
    consoleError.mockRestore();
  });

  it('should handle network error', async () => {
    const user = userEvent.setup();
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {});
    
    global.fetch = vi.fn(() => Promise.reject(new Error('Network error')));
    
    render(<TaskCreationForm />);
    
    await user.type(screen.getByLabelText('Title'), 'Test');
    await user.type(screen.getByLabelText('Description'), 'Test Desc');
    await user.type(screen.getByLabelText('Client Address'), 'Test Addr');
    await user.type(screen.getByLabelText('Estimated Duration (minutes)'), '30');
    
    const submitButton = screen.getByRole('button', { name: 'Create Task' });
    await user.click(submitButton);
    
    await waitFor(() => {
      expect(consoleError).toHaveBeenCalledWith(
        'Error creating task:',
        expect.any(Error)
      );
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
    await user.type(screen.getByLabelText('Client Address'), 'Test');
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

  it('should have correct input types for all fields', () => {
    render(<TaskCreationForm />);
    
    expect(screen.getByLabelText('Title')).toHaveAttribute('type', 'text');
    expect(screen.getByLabelText('Client Address')).toHaveAttribute('type', 'text');
    expect(screen.getByLabelText('Estimated Duration (minutes)')).toHaveAttribute('type', 'number');
  });

  it('should add error class to inputs with validation errors', async () => {
    render(<TaskCreationForm />);
    
    const form = screen.getByRole('button', { name: 'Create Task' }).closest('form');
    fireEvent.submit(form);
    
    await waitFor(() => {
      expect(screen.getByLabelText('Title')).toHaveClass('input-error');
      expect(screen.getByLabelText('Description')).toHaveClass('input-error');
      expect(screen.getByLabelText('Client Address')).toHaveClass('input-error');
      expect(screen.getByLabelText('Estimated Duration (minutes)')).toHaveClass('input-error');
    });
  });

  it('should have proper ARIA attributes for error messages', async () => {
    render(<TaskCreationForm />);
    
    const form = screen.getByRole('button', { name: 'Create Task' }).closest('form');
    fireEvent.submit(form);
    
    await waitFor(() => {
      const titleInput = screen.getByLabelText('Title');
      expect(titleInput).toHaveAttribute('aria-invalid', 'true');
      expect(titleInput).toHaveAttribute('aria-describedby', 'title-error');
    });
  });
});
