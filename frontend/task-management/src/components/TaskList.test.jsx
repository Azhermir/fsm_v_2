import { render, screen, waitFor, cleanup } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import TaskList from './TaskList'

describe('TaskList', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.resetAllMocks()
    global.fetch = vi.fn()
  })

  afterEach(() => {
    cleanup()
  })

  const mockTasks = [
    {
      id: 1,
      title: 'Fix HVAC',
      description: 'AC not working',
      clientAddress: '123 Main St',
      priority: 'HIGH',
      estimatedDuration: 120,
      status: 'UNASSIGNED',
      createdAt: '2025-11-20T22:00:00',
    },
    {
      id: 2,
      title: 'Install Lights',
      description: 'New lighting system',
      clientAddress: '456 Oak Ave',
      priority: 'MEDIUM',
      estimatedDuration: 180,
      status: 'ASSIGNED',
      createdAt: '2025-11-20T21:00:00',
    },
    {
      id: 3,
      title: 'Emergency Repair',
      description: 'Water leak',
      clientAddress: '789 Pine Rd',
      priority: 'CRITICAL',
      estimatedDuration: 60,
      status: 'IN_PROGRESS',
      createdAt: '2025-11-20T23:00:00',
    },
  ]

  it('renders the task list header', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => [],
    })

    render(<TaskList />)
    
    expect(screen.getByText('Service Tasks')).toBeInTheDocument()
    
    await waitFor(() => {
      expect(screen.getByRole('button', { name: /refresh/i })).toBeInTheDocument()
    })
  })

  it('fetches and displays tasks on mount', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTasks,
    })

    render(<TaskList />)

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith('http://localhost:8080/api/tasks')
    })

    await waitFor(() => {
      expect(screen.getByText('Fix HVAC')).toBeInTheDocument()
      expect(screen.getByText('Install Lights')).toBeInTheDocument()
      expect(screen.getByText('Emergency Repair')).toBeInTheDocument()
    })
  })

  it('displays task details correctly', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => [mockTasks[0]],
    })

    render(<TaskList />)

    await waitFor(() => {
      expect(screen.getByText('Fix HVAC')).toBeInTheDocument()
      expect(screen.getByText('AC not working')).toBeInTheDocument()
      expect(screen.getByText('123 Main St')).toBeInTheDocument()
      expect(screen.getByText('HIGH')).toBeInTheDocument()
      expect(screen.getByText('UNASSIGNED')).toBeInTheDocument()
      expect(screen.getByText('2.0')).toBeInTheDocument() // 120 minutes / 60 = 2.0 hours
    })
  })

  it('sorts tasks by priority and creation date', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTasks,
    })

    render(<TaskList />)

    await waitFor(() => {
      const rows = screen.getAllByRole('row').slice(1) // Skip header row
      const titles = rows.map(row => row.cells[0].textContent)
      
      // Emergency Repair (CRITICAL, newest) should be first
      // Fix HVAC (HIGH) should be second
      // Install Lights (MEDIUM, oldest) should be last
      expect(titles[0]).toContain('Emergency Repair')
      expect(titles[1]).toContain('Fix HVAC')
      expect(titles[2]).toContain('Install Lights')
    })
  })

  it('shows loading state while fetching', async () => {
    global.fetch.mockImplementationOnce(() => new Promise(resolve => {
      setTimeout(() => resolve({
        ok: true,
        json: async () => [],
      }), 100)
    }))

    render(<TaskList />)

    expect(screen.getByText('Loading tasks...')).toBeInTheDocument()
  })

  it('shows no tasks message when list is empty', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => [],
    })

    render(<TaskList />)

    await waitFor(() => {
      expect(screen.getByText('No tasks found')).toBeInTheDocument()
    })
  })

  it('shows error message when fetch fails', async () => {
    global.fetch.mockRejectedValueOnce(new Error('Failed to fetch tasks'))

    render(<TaskList />)

    await waitFor(() => {
      expect(screen.getByText(/failed to fetch tasks/i)).toBeInTheDocument()
    })
  })

  it('refreshes task list when refresh button is clicked', async () => {
    const user = userEvent.setup()
    
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => [mockTasks[0]],
    })

    render(<TaskList />)

    await waitFor(() => {
      expect(screen.getByText('Fix HVAC')).toBeInTheDocument()
    })

    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTasks,
    })

    const refreshButton = screen.getByRole('button', { name: /refresh/i })
    await user.click(refreshButton)

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledTimes(2)
    })

    await waitFor(() => {
      expect(screen.getByText('Emergency Repair')).toBeInTheDocument()
    })
  })

  it('disables refresh button while loading', async () => {
    const user = userEvent.setup()
    
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => [],
    })

    render(<TaskList />)

    await waitFor(() => {
      expect(screen.queryByText('Loading tasks...')).not.toBeInTheDocument()
    })

    global.fetch.mockImplementationOnce(() => new Promise(resolve => {
      setTimeout(() => resolve({
        ok: true,
        json: async () => [],
      }), 100)
    }))

    const refreshButton = screen.getByRole('button', { name: /refresh/i })
    await user.click(refreshButton)

    expect(screen.getByRole('button', { name: /loading/i })).toBeDisabled()
  })

  it('displays priority badges with correct styling classes', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTasks,
    })

    render(<TaskList />)

    await waitFor(() => {
      const criticalBadge = screen.getByText('CRITICAL')
      const highBadge = screen.getByText('HIGH')
      const mediumBadge = screen.getByText('MEDIUM')

      expect(criticalBadge).toHaveClass('priority-critical')
      expect(highBadge).toHaveClass('priority-high')
      expect(mediumBadge).toHaveClass('priority-medium')
    })
  })

  it('displays status badges with correct styling classes', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTasks,
    })

    render(<TaskList />)

    await waitFor(() => {
      const unassignedBadge = screen.getByText('UNASSIGNED')
      const assignedBadge = screen.getByText('ASSIGNED')
      const inProgressBadge = screen.getByText('IN_PROGRESS')

      expect(unassignedBadge).toHaveClass('status-unassigned')
      expect(assignedBadge).toHaveClass('status-assigned')
      expect(inProgressBadge).toHaveClass('status-in_progress')
    })
  })

  it('formats duration correctly', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => [
        { ...mockTasks[0], estimatedDuration: 90 }, // 1.5 hours
        { ...mockTasks[1], estimatedDuration: 45 }, // 0.8 hours (rounded to 0.8)
      ],
    })

    render(<TaskList />)

    await waitFor(() => {
      expect(screen.getByText('1.5')).toBeInTheDocument()
      expect(screen.getByText('0.8')).toBeInTheDocument()
    })
  })

  it('displays tasks without description correctly', async () => {
    const taskWithoutDescription = {
      ...mockTasks[0],
      description: null,
    }

    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => [taskWithoutDescription],
    })

    render(<TaskList />)

    await waitFor(() => {
      expect(screen.getByText('Fix HVAC')).toBeInTheDocument()
      expect(screen.queryByText('AC not working')).not.toBeInTheDocument()
    })
  })

  it('handles response errors correctly', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: false,
    })

    render(<TaskList />)

    await waitFor(() => {
      expect(screen.getByText(/failed to fetch tasks/i)).toBeInTheDocument()
    })
  })
})
