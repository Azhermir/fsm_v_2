import { render, screen, waitFor, cleanup } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import TaskDetail from './TaskDetail'

describe('TaskDetail', () => {
  const mockTask = {
    id: 1,
    title: 'Fix HVAC',
    description: 'AC not working',
    clientAddress: '123 Main St',
    priority: 'HIGH',
    estimatedDuration: 120,
    status: 'ASSIGNED',
    createdAt: '2025-11-20T22:00:00',
    assignedTechnicianId: 5,
  }

  const mockOnClose = vi.fn()
  const mockOnReassign = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()
    global.fetch = vi.fn()
  })

  afterEach(() => {
    cleanup()
  })

  it('renders task details correctly', () => {
    render(
      <TaskDetail
        task={mockTask}
        onClose={mockOnClose}
        onReassign={mockOnReassign}
      />
    )

    expect(screen.getByText('Task Details')).toBeInTheDocument()
    expect(screen.getByText('Fix HVAC')).toBeInTheDocument()
    expect(screen.getByText('AC not working')).toBeInTheDocument()
    expect(screen.getByText('123 Main St')).toBeInTheDocument()
    expect(screen.getByText('HIGH')).toBeInTheDocument()
    expect(screen.getByText('ASSIGNED')).toBeInTheDocument()
    expect(screen.getByText('2.0 hours')).toBeInTheDocument()
  })

  it('shows reassign button for assigned tasks', () => {
    render(
      <TaskDetail
        task={mockTask}
        onClose={mockOnClose}
        onReassign={mockOnReassign}
      />
    )

    expect(screen.getByText('Reassign')).toBeInTheDocument()
  })

  it('does not show reassign button for unassigned tasks', () => {
    const unassignedTask = { ...mockTask, status: 'UNASSIGNED' }

    render(
      <TaskDetail
        task={unassignedTask}
        onClose={mockOnClose}
        onReassign={mockOnReassign}
      />
    )

    expect(screen.queryByText('Reassign')).not.toBeInTheDocument()
  })

  it('does not show reassign button for in_progress tasks', () => {
    const inProgressTask = { ...mockTask, status: 'IN_PROGRESS' }

    render(
      <TaskDetail
        task={inProgressTask}
        onClose={mockOnClose}
        onReassign={mockOnReassign}
      />
    )

    expect(screen.queryByText('Reassign')).not.toBeInTheDocument()
  })

  it('does not show reassign button for completed tasks', () => {
    const completedTask = { ...mockTask, status: 'COMPLETED' }

    render(
      <TaskDetail
        task={completedTask}
        onClose={mockOnClose}
        onReassign={mockOnReassign}
      />
    )

    expect(screen.queryByText('Reassign')).not.toBeInTheDocument()
  })

  it('calls onClose when close button is clicked', async () => {
    const user = userEvent.setup()

    render(
      <TaskDetail
        task={mockTask}
        onClose={mockOnClose}
        onReassign={mockOnReassign}
      />
    )

    const closeButton = screen.getByLabelText('Close')
    await user.click(closeButton)

    expect(mockOnClose).toHaveBeenCalledTimes(1)
  })

  it('calls onClose when close action button is clicked', async () => {
    const user = userEvent.setup()

    render(
      <TaskDetail
        task={mockTask}
        onClose={mockOnClose}
        onReassign={mockOnReassign}
      />
    )

    const closeButtons = screen.getAllByRole('button', { name: /close/i })
    const closeActionButton = closeButtons.find(btn => btn.textContent === 'Close')
    await user.click(closeActionButton)

    expect(mockOnClose).toHaveBeenCalledTimes(1)
  })

  it('calls onClose when clicking overlay', async () => {
    const user = userEvent.setup()

    render(
      <TaskDetail
        task={mockTask}
        onClose={mockOnClose}
        onReassign={mockOnReassign}
      />
    )

    const overlay = screen.getByText('Task Details').closest('.task-detail-overlay')
    await user.click(overlay)

    expect(mockOnClose).toHaveBeenCalledTimes(1)
  })

  it('does not call onClose when clicking inside modal', async () => {
    const user = userEvent.setup()

    render(
      <TaskDetail
        task={mockTask}
        onClose={mockOnClose}
        onReassign={mockOnReassign}
      />
    )

    const modal = screen.getByText('Task Details').closest('.task-detail')
    await user.click(modal)

    expect(mockOnClose).not.toHaveBeenCalled()
  })

  it('shows reassign modal when reassign button is clicked', async () => {
    const user = userEvent.setup()
    
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => [],
    })

    render(
      <TaskDetail
        task={mockTask}
        onClose={mockOnClose}
        onReassign={mockOnReassign}
      />
    )

    const reassignButton = screen.getByText('Reassign')
    await user.click(reassignButton)

    await waitFor(() => {
      expect(screen.getByText('Reassign Task')).toBeInTheDocument()
    })
  })

  it('displays assigned technician when present', () => {
    render(
      <TaskDetail
        task={mockTask}
        onClose={mockOnClose}
        onReassign={mockOnReassign}
      />
    )

    expect(screen.getByText('Technician #5')).toBeInTheDocument()
  })

  it('does not display assigned technician section when not assigned', () => {
    const unassignedTask = { ...mockTask, assignedTechnicianId: null, status: 'UNASSIGNED' }

    render(
      <TaskDetail
        task={unassignedTask}
        onClose={mockOnClose}
        onReassign={mockOnReassign}
      />
    )

    expect(screen.queryByText(/Assigned Technician/i)).not.toBeInTheDocument()
  })

  it('renders task without description correctly', () => {
    const taskWithoutDescription = { ...mockTask, description: null }

    render(
      <TaskDetail
        task={taskWithoutDescription}
        onClose={mockOnClose}
        onReassign={mockOnReassign}
      />
    )

    expect(screen.getByText('Fix HVAC')).toBeInTheDocument()
    expect(screen.queryByText('AC not working')).not.toBeInTheDocument()
  })

  it('applies correct priority CSS classes', () => {
    const { rerender } = render(
      <TaskDetail
        task={{ ...mockTask, priority: 'CRITICAL' }}
        onClose={mockOnClose}
        onReassign={mockOnReassign}
      />
    )

    expect(screen.getByText('CRITICAL')).toHaveClass('priority-critical')

    rerender(
      <TaskDetail
        task={{ ...mockTask, priority: 'HIGH' }}
        onClose={mockOnClose}
        onReassign={mockOnReassign}
      />
    )

    expect(screen.getByText('HIGH')).toHaveClass('priority-high')

    rerender(
      <TaskDetail
        task={{ ...mockTask, priority: 'MEDIUM' }}
        onClose={mockOnClose}
        onReassign={mockOnReassign}
      />
    )

    expect(screen.getByText('MEDIUM')).toHaveClass('priority-medium')

    rerender(
      <TaskDetail
        task={{ ...mockTask, priority: 'LOW' }}
        onClose={mockOnClose}
        onReassign={mockOnReassign}
      />
    )

    expect(screen.getByText('LOW')).toHaveClass('priority-low')
  })

  it('applies correct status CSS classes', () => {
    const { rerender } = render(
      <TaskDetail
        task={{ ...mockTask, status: 'ASSIGNED' }}
        onClose={mockOnClose}
        onReassign={mockOnReassign}
      />
    )

    expect(screen.getByText('ASSIGNED')).toHaveClass('status-assigned')

    rerender(
      <TaskDetail
        task={{ ...mockTask, status: 'UNASSIGNED' }}
        onClose={mockOnClose}
        onReassign={mockOnReassign}
      />
    )

    expect(screen.getByText('UNASSIGNED')).toHaveClass('status-unassigned')

    rerender(
      <TaskDetail
        task={{ ...mockTask, status: 'IN_PROGRESS' }}
        onClose={mockOnClose}
        onReassign={mockOnReassign}
      />
    )

    expect(screen.getByText('IN_PROGRESS')).toHaveClass('status-in_progress')

    rerender(
      <TaskDetail
        task={{ ...mockTask, status: 'COMPLETED' }}
        onClose={mockOnClose}
        onReassign={mockOnReassign}
      />
    )

    expect(screen.getByText('COMPLETED')).toHaveClass('status-completed')
  })

  it('formats duration correctly', () => {
    const { rerender } = render(
      <TaskDetail
        task={{ ...mockTask, estimatedDuration: 90 }}
        onClose={mockOnClose}
        onReassign={mockOnReassign}
      />
    )

    expect(screen.getByText('1.5 hours')).toBeInTheDocument()

    rerender(
      <TaskDetail
        task={{ ...mockTask, estimatedDuration: 45 }}
        onClose={mockOnClose}
        onReassign={mockOnReassign}
      />
    )

    expect(screen.getByText('0.8 hours')).toBeInTheDocument()
  })
})
