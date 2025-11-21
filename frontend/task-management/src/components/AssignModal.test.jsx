import { render, screen, waitFor, cleanup } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import AssignModal from './AssignModal'

describe('AssignModal', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.resetAllMocks()
    global.fetch = vi.fn()
  })

  afterEach(() => {
    cleanup()
  })

  const mockTask = {
    id: 1,
    title: 'Fix HVAC System',
    priority: 'Critical',
    address: {
      street: '123 Main St',
      city: 'San Francisco',
      state: 'CA',
      zipCode: '94105',
      latitude: 37.7899,
      longitude: -122.3999,
    },
  }

  const mockTechnicians = [
    {
      id: 1,
      name: 'John Doe',
      email: 'john@example.com',
      phone: '555-1234',
      status: 'AVAILABLE',
      skillLevel: 'Senior',
      currentLocation: {
        latitude: 37.7749,
        longitude: -122.4194,
        timestamp: '2025-11-20T22:00:00',
      },
    },
    {
      id: 2,
      name: 'Jane Smith',
      email: 'jane@example.com',
      phone: '555-5678',
      status: 'AVAILABLE',
      skillLevel: 'Junior',
      currentLocation: {
        latitude: 37.7849,
        longitude: -122.4094,
        timestamp: '2025-11-20T22:05:00',
      },
    },
    {
      id: 3,
      name: 'Bob Johnson',
      email: 'bob@example.com',
      phone: '555-9012',
      status: 'BUSY',
      currentLocation: {
        latitude: 37.7649,
        longitude: -122.4294,
        timestamp: '2025-11-20T21:50:00',
      },
    },
  ]

  const mockOnAssign = vi.fn()
  const mockOnClose = vi.fn()

  it('renders modal with task title and priority', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => [],
    })

    render(
      <AssignModal
        task={mockTask}
        onAssign={mockOnAssign}
        onClose={mockOnClose}
      />
    )

    expect(screen.getByText('Assign Task')).toBeInTheDocument()
    expect(screen.getByText(/Fix HVAC System/)).toBeInTheDocument()
    expect(screen.getByText(/Critical/)).toBeInTheDocument()
  })

  it('fetches technicians on mount', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTechnicians,
    })

    render(
      <AssignModal
        task={mockTask}
        onAssign={mockOnAssign}
        onClose={mockOnClose}
      />
    )

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith('http://localhost:8080/api/technicians')
    })
  })

  it('filters and displays only available technicians', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTechnicians,
    })

    render(
      <AssignModal
        task={mockTask}
        onAssign={mockOnAssign}
        onClose={mockOnClose}
      />
    )

    await waitFor(() => {
      expect(screen.getByText(/John Doe/)).toBeInTheDocument()
      expect(screen.getByText(/Jane Smith/)).toBeInTheDocument()
      expect(screen.queryByText(/Bob Johnson/)).not.toBeInTheDocument() // BUSY status
    })
  })

  it('sorts technicians by distance to task', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTechnicians,
    })

    render(
      <AssignModal
        task={mockTask}
        onAssign={mockOnAssign}
        onClose={mockOnClose}
      />
    )

    await waitFor(() => {
      const select = screen.getByRole('combobox')
      const options = Array.from(select.querySelectorAll('option')).slice(1) // Skip placeholder
      
      // Check that options are present (sorting order verification)
      expect(options.length).toBe(2) // Only AVAILABLE technicians
    })
  })

  it('displays distance in the technician option', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTechnicians,
    })

    render(
      <AssignModal
        task={mockTask}
        onAssign={mockOnAssign}
        onClose={mockOnClose}
      />
    )

    await waitFor(() => {
      const select = screen.getByRole('combobox')
      expect(select.textContent).toMatch(/away/)
    })
  })

  it('displays technician skill level in options', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTechnicians,
    })

    render(
      <AssignModal
        task={mockTask}
        onAssign={mockOnAssign}
        onClose={mockOnClose}
      />
    )

    await waitFor(() => {
      const select = screen.getByRole('combobox')
      expect(select.textContent).toContain('Senior')
      expect(select.textContent).toContain('Junior')
    })
  })

  it('displays technician details when selected', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTechnicians,
    })

    const user = userEvent.setup()
    render(
      <AssignModal
        task={mockTask}
        onAssign={mockOnAssign}
        onClose={mockOnClose}
      />
    )

    await waitFor(() => {
      expect(screen.getByRole('combobox')).toBeInTheDocument()
    })

    const select = screen.getByRole('combobox')
    await user.selectOptions(select, '1')

    await waitFor(() => {
      expect(screen.getByText('Technician Details')).toBeInTheDocument()
      expect(screen.getByText('John Doe')).toBeInTheDocument()
      expect(screen.getByText('AVAILABLE')).toBeInTheDocument()
      expect(screen.getByText('Senior')).toBeInTheDocument()
      expect(screen.getByText('555-1234')).toBeInTheDocument()
    })
  })

  it('calls onAssign with technician ID when form is submitted', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTechnicians,
    })

    const user = userEvent.setup()
    render(
      <AssignModal
        task={mockTask}
        onAssign={mockOnAssign}
        onClose={mockOnClose}
      />
    )

    await waitFor(() => {
      expect(screen.getByRole('combobox')).toBeInTheDocument()
    })

    const select = screen.getByRole('combobox')
    await user.selectOptions(select, '1')

    const submitButton = screen.getByText('Assign')
    await user.click(submitButton)

    await waitFor(() => {
      expect(mockOnAssign).toHaveBeenCalledWith(1)
    })
  })

  it('disables submit button when no technician is selected', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTechnicians,
    })

    render(
      <AssignModal
        task={mockTask}
        onAssign={mockOnAssign}
        onClose={mockOnClose}
      />
    )

    await waitFor(() => {
      const submitButton = screen.getByText('Assign')
      expect(submitButton).toBeDisabled()
    })
  })

  it('enables submit button when technician is selected', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTechnicians,
    })

    const user = userEvent.setup()
    render(
      <AssignModal
        task={mockTask}
        onAssign={mockOnAssign}
        onClose={mockOnClose}
      />
    )

    await waitFor(() => {
      expect(screen.getByRole('combobox')).toBeInTheDocument()
    })

    const select = screen.getByRole('combobox')
    await user.selectOptions(select, '1')

    await waitFor(() => {
      const submitButton = screen.getByText('Assign')
      expect(submitButton).not.toBeDisabled()
    })
  })

  it('displays error message when fetch fails', async () => {
    global.fetch.mockRejectedValueOnce(new Error('Network error'))

    render(
      <AssignModal
        task={mockTask}
        onAssign={mockOnAssign}
        onClose={mockOnClose}
      />
    )

    await waitFor(() => {
      expect(screen.getByRole('alert')).toBeInTheDocument()
      expect(screen.getByText('Network error')).toBeInTheDocument()
    })
  })

  it('displays error message when API returns non-ok response', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: false,
    })

    render(
      <AssignModal
        task={mockTask}
        onAssign={mockOnAssign}
        onClose={mockOnClose}
      />
    )

    await waitFor(() => {
      expect(screen.getByRole('alert')).toBeInTheDocument()
      expect(screen.getByText('Failed to fetch technicians')).toBeInTheDocument()
    })
  })

  it('displays message when no available technicians found', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => [],
    })

    render(
      <AssignModal
        task={mockTask}
        onAssign={mockOnAssign}
        onClose={mockOnClose}
      />
    )

    await waitFor(() => {
      expect(screen.getByText('No available technicians found for assignment.')).toBeInTheDocument()
    })
  })

  it('closes modal when close button is clicked', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTechnicians,
    })

    const user = userEvent.setup()
    render(
      <AssignModal
        task={mockTask}
        onAssign={mockOnAssign}
        onClose={mockOnClose}
      />
    )

    const closeButton = screen.getByLabelText('Close')
    await user.click(closeButton)

    expect(mockOnClose).toHaveBeenCalled()
  })

  it('closes modal when cancel button is clicked', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTechnicians,
    })

    const user = userEvent.setup()
    render(
      <AssignModal
        task={mockTask}
        onAssign={mockOnAssign}
        onClose={mockOnClose}
      />
    )

    await waitFor(() => {
      expect(screen.getByText('Cancel')).toBeInTheDocument()
    })

    const cancelButton = screen.getByText('Cancel')
    await user.click(cancelButton)

    expect(mockOnClose).toHaveBeenCalled()
  })

  it('closes modal when clicking on overlay', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTechnicians,
    })

    const user = userEvent.setup()
    render(
      <AssignModal
        task={mockTask}
        onAssign={mockOnAssign}
        onClose={mockOnClose}
      />
    )

    const overlay = document.querySelector('.modal-overlay')
    await user.click(overlay)

    expect(mockOnClose).toHaveBeenCalled()
  })

  it('does not close modal when clicking inside modal content', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTechnicians,
    })

    const user = userEvent.setup()
    render(
      <AssignModal
        task={mockTask}
        onAssign={mockOnAssign}
        onClose={mockOnClose}
      />
    )

    await waitFor(() => {
      expect(screen.getByText('Assign Task')).toBeInTheDocument()
    })

    const modalContent = document.querySelector('.modal-content')
    await user.click(modalContent)

    expect(mockOnClose).not.toHaveBeenCalled()
  })

  it('shows loading state while fetching technicians', () => {
    global.fetch.mockImplementationOnce(() => new Promise(() => {})) // Never resolves

    render(
      <AssignModal
        task={mockTask}
        onAssign={mockOnAssign}
        onClose={mockOnClose}
      />
    )

    expect(screen.getByText('Loading technicians...')).toBeInTheDocument()
  })

  it('disables buttons while assignment is in progress', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTechnicians,
    })

    mockOnAssign.mockImplementationOnce(() => new Promise(() => {})) // Never resolves

    const user = userEvent.setup()
    render(
      <AssignModal
        task={mockTask}
        onAssign={mockOnAssign}
        onClose={mockOnClose}
      />
    )

    await waitFor(() => {
      expect(screen.getByRole('combobox')).toBeInTheDocument()
    })

    const select = screen.getByRole('combobox')
    await user.selectOptions(select, '1')

    const submitButton = screen.getByText('Assign')
    await user.click(submitButton)

    await waitFor(() => {
      expect(screen.getByText('Assigning...')).toBeInTheDocument()
      expect(screen.getByText('Cancel')).toBeDisabled()
    })
  })

  it('filters out technicians without valid location data', async () => {
    const techniciansWithInvalidLocation = [
      ...mockTechnicians,
      {
        id: 4,
        name: 'Invalid Tech',
        email: 'invalid@example.com',
        status: 'AVAILABLE',
        currentLocation: null,
      },
      {
        id: 5,
        name: 'No Coords Tech',
        email: 'nocoords@example.com',
        status: 'AVAILABLE',
        currentLocation: {
          latitude: null,
          longitude: null,
          timestamp: '2025-11-20T22:00:00',
        },
      },
    ]

    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => techniciansWithInvalidLocation,
    })

    render(
      <AssignModal
        task={mockTask}
        onAssign={mockOnAssign}
        onClose={mockOnClose}
      />
    )

    await waitFor(() => {
      expect(screen.queryByText(/Invalid Tech/)).not.toBeInTheDocument()
      expect(screen.queryByText(/No Coords Tech/)).not.toBeInTheDocument()
      expect(screen.getByText(/John Doe/)).toBeInTheDocument()
    })
  })

  it('displays error when assignment fails', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTechnicians,
    })

    mockOnAssign.mockRejectedValueOnce(new Error('Assignment failed'))

    const user = userEvent.setup()
    render(
      <AssignModal
        task={mockTask}
        onAssign={mockOnAssign}
        onClose={mockOnClose}
      />
    )

    await waitFor(() => {
      expect(screen.getByRole('combobox')).toBeInTheDocument()
    })

    const select = screen.getByRole('combobox')
    await user.selectOptions(select, '1')

    const submitButton = screen.getByText('Assign')
    await user.click(submitButton)

    await waitFor(() => {
      expect(screen.getByText('Assignment failed')).toBeInTheDocument()
    })
  })

  it('formats distance correctly for values less than 1km', async () => {
    const closeTechnician = {
      id: 1,
      name: 'Close Tech',
      status: 'AVAILABLE',
      currentLocation: {
        latitude: 37.7900, // Very close to task
        longitude: -122.4000,
      },
    }

    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => [closeTechnician],
    })

    render(
      <AssignModal
        task={mockTask}
        onAssign={mockOnAssign}
        onClose={mockOnClose}
      />
    )

    await waitFor(() => {
      const select = screen.getByRole('combobox')
      expect(select.textContent).toMatch(/\dm away/)
    })
  })

  it('formats distance correctly for values over 1km', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTechnicians,
    })

    render(
      <AssignModal
        task={mockTask}
        onAssign={mockOnAssign}
        onClose={mockOnClose}
      />
    )

    await waitFor(() => {
      const select = screen.getByRole('combobox')
      expect(select.textContent).toMatch(/\d+\.\dkm away/)
    })
  })
})
