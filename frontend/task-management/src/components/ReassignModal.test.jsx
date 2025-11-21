import { render, screen, waitFor, cleanup } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import ReassignModal from './ReassignModal'

describe('ReassignModal', () => {
  const mockTask = {
    id: 1,
    title: 'Fix HVAC',
    assignedTechnicianId: 5,
  }

  const mockTechnicians = [
    { id: 1, name: 'John Doe', skillLevel: 'SENIOR' },
    { id: 2, name: 'Jane Smith', skillLevel: 'JUNIOR' },
    { id: 3, name: 'Bob Johnson', skillLevel: 'INTERMEDIATE' },
    { id: 5, name: 'Currently Assigned', skillLevel: 'SENIOR' }, // Should be filtered out
  ]

  const mockOnReassign = vi.fn()
  const mockOnClose = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()
    global.fetch = vi.fn()
  })

  afterEach(() => {
    cleanup()
  })

  it('renders modal with task title', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTechnicians,
    })

    render(
      <ReassignModal
        task={mockTask}
        onReassign={mockOnReassign}
        onClose={mockOnClose}
      />
    )

    expect(screen.getByText('Reassign Task')).toBeInTheDocument()
    expect(screen.getByText(/Fix HVAC/)).toBeInTheDocument()
  })

  it('fetches technicians on mount', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTechnicians,
    })

    render(
      <ReassignModal
        task={mockTask}
        onReassign={mockOnReassign}
        onClose={mockOnClose}
      />
    )

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith('http://localhost:8080/api/technicians')
    })
  })

  it('displays technicians in dropdown excluding currently assigned', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTechnicians,
    })

    render(
      <ReassignModal
        task={mockTask}
        onReassign={mockOnReassign}
        onClose={mockOnClose}
      />
    )

    await waitFor(() => {
      expect(screen.getByText('John Doe (SENIOR)')).toBeInTheDocument()
      expect(screen.getByText('Jane Smith (JUNIOR)')).toBeInTheDocument()
      expect(screen.getByText('Bob Johnson (INTERMEDIATE)')).toBeInTheDocument()
      expect(screen.queryByText('Currently Assigned (SENIOR)')).not.toBeInTheDocument()
    })
  })

  it('displays loading state while fetching technicians', () => {
    global.fetch.mockImplementationOnce(() => new Promise(() => {}))

    render(
      <ReassignModal
        task={mockTask}
        onReassign={mockOnReassign}
        onClose={mockOnClose}
      />
    )

    expect(screen.getByText('Loading technicians...')).toBeInTheDocument()
  })

  it('shows error when fetch fails', async () => {
    global.fetch.mockRejectedValueOnce(new Error('Failed to fetch technicians'))

    render(
      <ReassignModal
        task={mockTask}
        onReassign={mockOnReassign}
        onClose={mockOnClose}
      />
    )

    await waitFor(() => {
      expect(screen.getByText(/failed to fetch technicians/i)).toBeInTheDocument()
    })
  })

  it('shows error when response is not ok', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: false,
    })

    render(
      <ReassignModal
        task={mockTask}
        onReassign={mockOnReassign}
        onClose={mockOnClose}
      />
    )

    await waitFor(() => {
      expect(screen.getByText(/failed to fetch technicians/i)).toBeInTheDocument()
    })
  })

  it('displays all reason options', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTechnicians,
    })

    render(
      <ReassignModal
        task={mockTask}
        onReassign={mockOnReassign}
        onClose={mockOnClose}
      />
    )

    await waitFor(() => {
      expect(screen.getByText('Technician Unavailable')).toBeInTheDocument()
      expect(screen.getByText('Urgent Priority')).toBeInTheDocument()
      expect(screen.getByText('Closer Location')).toBeInTheDocument()
      expect(screen.getByText('Skill Mismatch')).toBeInTheDocument()
      expect(screen.getByText('Other')).toBeInTheDocument()
    })
  })

  it('calls onClose when close button is clicked', async () => {
    const user = userEvent.setup()
    
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTechnicians,
    })

    render(
      <ReassignModal
        task={mockTask}
        onReassign={mockOnReassign}
        onClose={mockOnClose}
      />
    )

    const closeButton = screen.getByLabelText('Close')
    await user.click(closeButton)

    expect(mockOnClose).toHaveBeenCalledTimes(1)
  })

  it('calls onClose when cancel button is clicked', async () => {
    const user = userEvent.setup()
    
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTechnicians,
    })

    render(
      <ReassignModal
        task={mockTask}
        onReassign={mockOnReassign}
        onClose={mockOnClose}
      />
    )

    await waitFor(() => {
      expect(screen.queryByText('Loading technicians...')).not.toBeInTheDocument()
    })

    const cancelButton = screen.getByRole('button', { name: /cancel/i })
    await user.click(cancelButton)

    expect(mockOnClose).toHaveBeenCalledTimes(1)
  })

  it('calls onClose when clicking overlay', async () => {
    const user = userEvent.setup()
    
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTechnicians,
    })

    render(
      <ReassignModal
        task={mockTask}
        onReassign={mockOnReassign}
        onClose={mockOnClose}
      />
    )

    await waitFor(() => {
      expect(screen.queryByText('Loading technicians...')).not.toBeInTheDocument()
    })

    const overlay = screen.getByText('Reassign Task').closest('.modal-overlay')
    await user.click(overlay)

    expect(mockOnClose).toHaveBeenCalledTimes(1)
  })

  it('does not call onClose when clicking inside modal', async () => {
    const user = userEvent.setup()
    
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTechnicians,
    })

    render(
      <ReassignModal
        task={mockTask}
        onReassign={mockOnReassign}
        onClose={mockOnClose}
      />
    )

    await waitFor(() => {
      expect(screen.queryByText('Loading technicians...')).not.toBeInTheDocument()
    })

    const modal = screen.getByText('Reassign Task').closest('.modal-content')
    await user.click(modal)

    expect(mockOnClose).not.toHaveBeenCalled()
  })

  it('calls onReassign with technician and no reason when submitted', async () => {
    const user = userEvent.setup()
    
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTechnicians,
    })

    mockOnReassign.mockResolvedValueOnce()

    render(
      <ReassignModal
        task={mockTask}
        onReassign={mockOnReassign}
        onClose={mockOnClose}
      />
    )

    await waitFor(() => {
      expect(screen.queryByText('Loading technicians...')).not.toBeInTheDocument()
    })

    const technicianSelect = screen.getByLabelText(/Select New Technician/i)
    await user.selectOptions(technicianSelect, '1')

    const submitButton = screen.getByRole('button', { name: /^reassign$/i })
    await user.click(submitButton)

    await waitFor(() => {
      expect(mockOnReassign).toHaveBeenCalledWith(1, null)
    })
  })

  it('calls onReassign with technician and reason when submitted', async () => {
    const user = userEvent.setup()
    
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTechnicians,
    })

    mockOnReassign.mockResolvedValueOnce()

    render(
      <ReassignModal
        task={mockTask}
        onReassign={mockOnReassign}
        onClose={mockOnClose}
      />
    )

    await waitFor(() => {
      expect(screen.queryByText('Loading technicians...')).not.toBeInTheDocument()
    })

    const technicianSelect = screen.getByLabelText(/Select New Technician/i)
    await user.selectOptions(technicianSelect, '2')

    const reasonSelect = screen.getByLabelText(/Reason/i)
    await user.selectOptions(reasonSelect, 'urgent_priority')

    const submitButton = screen.getByRole('button', { name: /reassign/i })
    await user.click(submitButton)

    await waitFor(() => {
      expect(mockOnReassign).toHaveBeenCalledWith(2, 'urgent_priority')
    })
  })

  it('disables submit button while loading', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTechnicians,
    })

    render(
      <ReassignModal
        task={mockTask}
        onReassign={mockOnReassign}
        onClose={mockOnClose}
      />
    )

    await waitFor(() => {
      expect(screen.queryByText('Loading technicians...')).not.toBeInTheDocument()
    })

    const submitButton = screen.getByRole('button', { name: /reassign/i })
    expect(submitButton).toBeDisabled()
  })

  it('disables submit button while technicians are loading', () => {
    global.fetch.mockImplementationOnce(() => new Promise(() => {}))

    render(
      <ReassignModal
        task={mockTask}
        onReassign={mockOnReassign}
        onClose={mockOnClose}
      />
    )

    const submitButton = screen.getByRole('button', { name: /reassign/i })
    expect(submitButton).toBeDisabled()
  })

  it('shows loading state on submit button when reassigning', async () => {
    const user = userEvent.setup()
    
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTechnicians,
    })

    mockOnReassign.mockImplementationOnce(() => new Promise(() => {}))

    render(
      <ReassignModal
        task={mockTask}
        onReassign={mockOnReassign}
        onClose={mockOnClose}
      />
    )

    await waitFor(() => {
      expect(screen.queryByText('Loading technicians...')).not.toBeInTheDocument()
    })

    const technicianSelect = screen.getByLabelText(/Select New Technician/i)
    await user.selectOptions(technicianSelect, '1')

    const submitButton = screen.getByRole('button', { name: /reassign/i })
    await user.click(submitButton)

    await waitFor(() => {
      expect(screen.getByText('Reassigning...')).toBeInTheDocument()
    })
  })

  it('disables form fields while reassigning', async () => {
    const user = userEvent.setup()
    
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTechnicians,
    })

    mockOnReassign.mockImplementationOnce(() => new Promise(() => {}))

    render(
      <ReassignModal
        task={mockTask}
        onReassign={mockOnReassign}
        onClose={mockOnClose}
      />
    )

    await waitFor(() => {
      expect(screen.queryByText('Loading technicians...')).not.toBeInTheDocument()
    })

    const technicianSelect = screen.getByLabelText(/Select New Technician/i)
    await user.selectOptions(technicianSelect, '1')

    const submitButton = screen.getByRole('button', { name: /reassign/i })
    await user.click(submitButton)

    await waitFor(() => {
      expect(technicianSelect).toBeDisabled()
      expect(screen.getByLabelText(/Reason/i)).toBeDisabled()
      expect(screen.getByRole('button', { name: /cancel/i })).toBeDisabled()
    })
  })

  it('shows error when onReassign throws error', async () => {
    const user = userEvent.setup()
    
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTechnicians,
    })

    mockOnReassign.mockRejectedValueOnce(new Error('Reassignment failed'))

    render(
      <ReassignModal
        task={mockTask}
        onReassign={mockOnReassign}
        onClose={mockOnClose}
      />
    )

    await waitFor(() => {
      expect(screen.queryByText('Loading technicians...')).not.toBeInTheDocument()
    })

    const technicianSelect = screen.getByLabelText(/Select New Technician/i)
    await user.selectOptions(technicianSelect, '1')

    const submitButton = screen.getByRole('button', { name: /reassign/i })
    await user.click(submitButton)

    await waitFor(() => {
      expect(screen.getByText(/reassignment failed/i)).toBeInTheDocument()
    })
  })

  it('displays technicians without skill level', async () => {
    const techniciansWithoutSkillLevel = [
      { id: 1, name: 'John Doe' },
      { id: 2, name: 'Jane Smith', skillLevel: null },
    ]

    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => techniciansWithoutSkillLevel,
    })

    render(
      <ReassignModal
        task={mockTask}
        onReassign={mockOnReassign}
        onClose={mockOnClose}
      />
    )

    await waitFor(() => {
      expect(screen.queryByText('Loading technicians...')).not.toBeInTheDocument()
    })

    // Should show name without skill level in parentheses
    expect(screen.getByRole('option', { name: 'John Doe' })).toBeInTheDocument()
    expect(screen.getByRole('option', { name: 'Jane Smith' })).toBeInTheDocument()
  })
})
