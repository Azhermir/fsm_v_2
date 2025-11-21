import { render, screen, waitFor, fireEvent } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import CreateTaskForm from './CreateTaskForm'

describe('CreateTaskForm', () => {
  beforeEach(() => {
    global.fetch = vi.fn()
  })

  it('renders the form with all required fields', () => {
    render(<CreateTaskForm />)
    
    expect(screen.getByText('Create New Service Task')).toBeInTheDocument()
    expect(screen.getByLabelText(/title/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/description/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/client address/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/priority/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/estimated duration/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /create task/i })).toBeInTheDocument()
  })

  it('shows all priority options in dropdown', () => {
    render(<CreateTaskForm />)
    
    const prioritySelect = screen.getByLabelText(/priority/i)
    expect(prioritySelect).toHaveValue('MEDIUM')
    
    const options = screen.getAllByRole('option')
    expect(options).toHaveLength(4)
    expect(screen.getByRole('option', { name: 'Low' })).toBeInTheDocument()
    expect(screen.getByRole('option', { name: 'Medium' })).toBeInTheDocument()
    expect(screen.getByRole('option', { name: 'High' })).toBeInTheDocument()
    expect(screen.getByRole('option', { name: 'Critical' })).toBeInTheDocument()
  })

  it('updates form fields when user types', async () => {
    const user = userEvent.setup()
    render(<CreateTaskForm />)

    const titleInput = screen.getByLabelText(/title/i)
    const descriptionInput = screen.getByLabelText(/description/i)
    const addressInput = screen.getByLabelText(/client address/i)
    const durationInput = screen.getByLabelText(/estimated duration/i)

    await user.type(titleInput, 'Fix HVAC')
    await user.type(descriptionInput, 'AC not working')
    await user.type(addressInput, '123 Main St')
    await user.type(durationInput, '2')

    expect(titleInput).toHaveValue('Fix HVAC')
    expect(descriptionInput).toHaveValue('AC not working')
    expect(addressInput).toHaveValue('123 Main St')
    expect(durationInput).toHaveValue(2)
  })

  it('changes priority when dropdown is changed', async () => {
    const user = userEvent.setup()
    render(<CreateTaskForm />)

    const prioritySelect = screen.getByLabelText(/priority/i)
    await user.selectOptions(prioritySelect, 'HIGH')

    expect(prioritySelect).toHaveValue('HIGH')
  })

  it('submits form with correct data and shows success message', async () => {
    const user = userEvent.setup()
    const mockResponse = {
      id: 1,
      title: 'Fix HVAC',
      description: 'AC not working',
      clientAddress: '123 Main St',
      priority: 'HIGH',
      estimatedDuration: 120,
      status: 'UNASSIGNED',
      createdAt: '2025-11-20T22:00:00',
    }

    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockResponse,
    })

    render(<CreateTaskForm />)

    await user.type(screen.getByLabelText(/title/i), 'Fix HVAC')
    await user.type(screen.getByLabelText(/description/i), 'AC not working')
    await user.type(screen.getByLabelText(/client address/i), '123 Main St')
    await user.selectOptions(screen.getByLabelText(/priority/i), 'HIGH')
    await user.type(screen.getByLabelText(/estimated duration/i), '2')

    await user.click(screen.getByRole('button', { name: /create task/i }))

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
            clientAddress: '123 Main St',
            latitude: null,
            longitude: null,
            priority: 'HIGH',
            estimatedDuration: 2,
          }),
        })
      )
    })

    await waitFor(() => {
      expect(screen.getByText(/task "Fix HVAC" created successfully!/i)).toBeInTheDocument()
    })
  })

  it('resets form after successful submission', async () => {
    const user = userEvent.setup()
    const mockResponse = {
      id: 1,
      title: 'Fix HVAC',
      priority: 'HIGH',
    }

    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockResponse,
    })

    render(<CreateTaskForm />)

    await user.type(screen.getByLabelText(/title/i), 'Fix HVAC')
    await user.type(screen.getByLabelText(/description/i), 'AC not working')
    await user.type(screen.getByLabelText(/client address/i), '123 Main St')
    await user.type(screen.getByLabelText(/estimated duration/i), '2')

    await user.click(screen.getByRole('button', { name: /create task/i }))

    await waitFor(() => {
      expect(screen.getByLabelText(/title/i)).toHaveValue('')
      expect(screen.getByLabelText(/description/i)).toHaveValue('')
      expect(screen.getByLabelText(/client address/i)).toHaveValue('')
      expect(screen.getByLabelText(/estimated duration/i)).toHaveValue(null)
    })
  })

  it('shows error message when submission fails', async () => {
    const user = userEvent.setup()
    
    global.fetch.mockResolvedValueOnce({
      ok: false,
      json: async () => ({ message: 'Validation failed' }),
    })

    render(<CreateTaskForm />)

    await user.type(screen.getByLabelText(/title/i), 'Fix HVAC')
    await user.type(screen.getByLabelText(/description/i), 'AC not working')
    await user.type(screen.getByLabelText(/client address/i), '123 Main St')
    await user.type(screen.getByLabelText(/estimated duration/i), '2')

    await user.click(screen.getByRole('button', { name: /create task/i }))

    await waitFor(() => {
      expect(screen.getByText(/validation failed/i)).toBeInTheDocument()
    })
  })

  it('disables submit button while submitting', async () => {
    const user = userEvent.setup()
    
    global.fetch.mockImplementationOnce(() => new Promise(resolve => {
      setTimeout(() => resolve({
        ok: true,
        json: async () => ({ id: 1, title: 'Test' }),
      }), 100)
    }))

    render(<CreateTaskForm />)

    await user.type(screen.getByLabelText(/title/i), 'Fix HVAC')
    await user.type(screen.getByLabelText(/description/i), 'AC not working')
    await user.type(screen.getByLabelText(/client address/i), '123 Main St')
    await user.type(screen.getByLabelText(/estimated duration/i), '2')

    const submitButton = screen.getByRole('button', { name: /create task/i })
    await user.click(submitButton)

    expect(screen.getByRole('button', { name: /creating/i })).toBeDisabled()
  })

  it('clears messages when user starts typing', async () => {
    const user = userEvent.setup()
    
    global.fetch.mockResolvedValueOnce({
      ok: false,
      json: async () => ({ message: 'Error occurred' }),
    })

    render(<CreateTaskForm />)

    await user.type(screen.getByLabelText(/title/i), 'Fix HVAC')
    await user.type(screen.getByLabelText(/description/i), 'AC not working')
    await user.type(screen.getByLabelText(/client address/i), '123 Main St')
    await user.type(screen.getByLabelText(/estimated duration/i), '2')

    await user.click(screen.getByRole('button', { name: /create task/i }))

    await waitFor(() => {
      expect(screen.getByText(/error occurred/i)).toBeInTheDocument()
    })

    await user.type(screen.getByLabelText(/title/i), ' Updated')

    expect(screen.queryByText(/error occurred/i)).not.toBeInTheDocument()
  })

  it('handles network errors gracefully', async () => {
    const user = userEvent.setup()
    
    global.fetch.mockRejectedValueOnce(new Error('Network error'))

    render(<CreateTaskForm />)

    await user.type(screen.getByLabelText(/title/i), 'Fix HVAC')
    await user.type(screen.getByLabelText(/description/i), 'AC not working')
    await user.type(screen.getByLabelText(/client address/i), '123 Main St')
    await user.type(screen.getByLabelText(/estimated duration/i), '2')

    await user.click(screen.getByRole('button', { name: /create task/i }))

    await waitFor(() => {
      expect(screen.getByText(/network error/i)).toBeInTheDocument()
    })
  })

  it('shows validation error when title exceeds max length', async () => {
    const user = userEvent.setup()
    render(<CreateTaskForm />)

    const titleInput = screen.getByLabelText(/title/i)
    // Type exactly at the limit, then try to add more via paste which bypasses maxLength
    const longTitle = 'a'.repeat(201)
    
    // Simulate pasting content that exceeds max length
    fireEvent.change(titleInput, { target: { value: longTitle } })
    await user.tab() // Trigger blur

    await waitFor(() => {
      expect(screen.getByText(/title must not exceed 200 characters/i)).toBeInTheDocument()
    })
  })

  it('shows validation error when description exceeds max length', async () => {
    const user = userEvent.setup()
    render(<CreateTaskForm />)

    const descriptionInput = screen.getByLabelText(/description/i)
    const longDescription = 'a'.repeat(2001)
    
    // Simulate pasting content that exceeds max length
    fireEvent.change(descriptionInput, { target: { value: longDescription } })
    await user.tab() // Trigger blur

    await waitFor(() => {
      expect(screen.getByText(/description must not exceed 2000 characters/i)).toBeInTheDocument()
    })
  })

  it('shows validation error when required fields are empty on blur', async () => {
    const user = userEvent.setup()
    render(<CreateTaskForm />)

    const titleInput = screen.getByLabelText(/title/i)
    const descriptionInput = screen.getByLabelText(/description/i)
    const addressInput = screen.getByLabelText(/client address/i)

    // Focus and blur without entering data
    await user.click(titleInput)
    await user.tab()

    await user.click(descriptionInput)
    await user.tab()

    await user.click(addressInput)
    await user.tab()

    await waitFor(() => {
      expect(screen.getByText(/title is required/i)).toBeInTheDocument()
      expect(screen.getByText(/description is required/i)).toBeInTheDocument()
      expect(screen.getByText(/client address is required/i)).toBeInTheDocument()
    })
  })

  it('prevents form submission when validation errors exist', async () => {
    const user = userEvent.setup()
    render(<CreateTaskForm />)

    const titleInput = screen.getByLabelText(/title/i)
    const longTitle = 'a'.repeat(201)
    
    // Simulate pasting content that exceeds max length
    fireEvent.change(titleInput, { target: { value: longTitle } })
    await user.tab() // Trigger blur and validation error

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /create task/i })).toBeDisabled()
    })

    await user.click(screen.getByRole('button', { name: /create task/i }))
    
    // Verify fetch was not called
    expect(global.fetch).not.toHaveBeenCalled()
  })

  it('clears validation error when user corrects the field', async () => {
    const user = userEvent.setup()
    render(<CreateTaskForm />)

    const titleInput = screen.getByLabelText(/title/i)
    
    // Trigger validation error
    await user.click(titleInput)
    await user.tab()

    await waitFor(() => {
      expect(screen.getByText(/title is required/i)).toBeInTheDocument()
    })

    // Correct the field
    await user.type(titleInput, 'Valid title')

    await waitFor(() => {
      expect(screen.queryByText(/title is required/i)).not.toBeInTheDocument()
    })
  })

  it('shows character count for description field', () => {
    render(<CreateTaskForm />)
    
    expect(screen.getByText(/0 \/ 2000 characters/i)).toBeInTheDocument()
  })

  it('updates character count as user types in description', async () => {
    const user = userEvent.setup()
    render(<CreateTaskForm />)

    const descriptionInput = screen.getByLabelText(/description/i)
    await user.type(descriptionInput, 'Test description')

    await waitFor(() => {
      expect(screen.getByText(/16 \/ 2000 characters/i)).toBeInTheDocument()
    })
  })

  it('prevents submission when required fields are missing and shows error message', async () => {
    const user = userEvent.setup()
    render(<CreateTaskForm />)

    // Try to submit empty form
    await user.click(screen.getByRole('button', { name: /create task/i }))

    await waitFor(() => {
      expect(screen.getByText(/please fix the validation errors before submitting/i)).toBeInTheDocument()
    })

    // Verify fetch was not called
    expect(global.fetch).not.toHaveBeenCalled()
  })

  it('handles server-side validation errors', async () => {
    const user = userEvent.setup()
    
    global.fetch.mockResolvedValueOnce({
      ok: false,
      json: async () => ({ 
        validationErrors: ['Title is required', 'Description must not exceed 2000 characters'] 
      }),
    })

    render(<CreateTaskForm />)

    await user.type(screen.getByLabelText(/title/i), 'Fix HVAC')
    await user.type(screen.getByLabelText(/description/i), 'AC not working')
    await user.type(screen.getByLabelText(/client address/i), '123 Main St')
    await user.type(screen.getByLabelText(/estimated duration/i), '2')

    await user.click(screen.getByRole('button', { name: /create task/i }))

    await waitFor(() => {
      expect(screen.getByText(/title is required; description must not exceed 2000 characters/i)).toBeInTheDocument()
    })
  })
})
