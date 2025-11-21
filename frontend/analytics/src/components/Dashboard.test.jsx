import { render, screen, waitFor, cleanup } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import Dashboard from './Dashboard'

// Mock PieChart component
vi.mock('./PieChart', () => ({
  default: ({ data }) => (
    <div data-testid="pie-chart-mock">
      {data.map((item, i) => (
        <div key={i}>{item.label}: {item.value}</div>
      ))}
    </div>
  )
}))

describe('Dashboard', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.resetAllMocks()
    global.fetch = vi.fn()
  })

  afterEach(() => {
    cleanup()
  })

  const mockMetrics = {
    totalTasks: 100,
    completedTasks: 60,
    openTasks: 40,
    tasksByPriority: {
      CRITICAL: 10,
      HIGH: 20,
      MEDIUM: 30,
      LOW: 40
    },
    averageCompletionTimeMinutes: 45.5,
    technicianWorkload: {
      1: 10,
      2: 15,
      3: 20
    }
  }

  it('renders dashboard header', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockMetrics,
    })

    render(<Dashboard disableAutoRefresh={true} />)
    
    await waitFor(() => {
      expect(screen.getByText('Analytics Dashboard')).toBeInTheDocument()
    })
    
    expect(screen.getByRole('button', { name: /refresh/i })).toBeInTheDocument()
  })

  it('fetches and displays metrics on mount', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockMetrics,
    })

    render(<Dashboard disableAutoRefresh={true} />)

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith('http://localhost:8080/api/dashboard/metrics')
    })

    await waitFor(() => {
      expect(screen.getByText('100')).toBeInTheDocument() // Total tasks
      expect(screen.getByText('60')).toBeInTheDocument() // Completed tasks
      expect(screen.getByText('60.0%')).toBeInTheDocument() // Completion rate
    })
  })

  it('shows loading state initially', () => {
    global.fetch.mockImplementationOnce(() => new Promise(() => {})) // Never resolves

    render(<Dashboard disableAutoRefresh={true} />)

    expect(screen.getByText('Loading dashboard...')).toBeInTheDocument()
  })

  it('shows error message when fetch fails', async () => {
    global.fetch.mockRejectedValueOnce(new Error('Network error'))

    render(<Dashboard disableAutoRefresh={true} />)

    await waitFor(() => {
      expect(screen.getByText(/error loading dashboard/i)).toBeInTheDocument()
    })
  })

  it('displays all KPI cards correctly', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockMetrics,
    })

    render(<Dashboard disableAutoRefresh={true} />)

    await waitFor(() => {
      expect(screen.getByText('Total Tasks')).toBeInTheDocument()
      expect(screen.getByText('Completed')).toBeInTheDocument()
      expect(screen.getByText('Completion Rate')).toBeInTheDocument()
      expect(screen.getByText('Avg Completion Time')).toBeInTheDocument()
    })
  })

  it('calculates completion percentage correctly', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockMetrics,
    })

    render(<Dashboard disableAutoRefresh={true} />)

    await waitFor(() => {
      expect(screen.getByText('60.0%')).toBeInTheDocument() // 60/100 * 100 = 60.0%
    })
  })

  it('displays completion percentage with one decimal place', async () => {
    const metrics = {
      ...mockMetrics,
      totalTasks: 3,
      completedTasks: 2,
    }

    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => metrics,
    })

    render(<Dashboard disableAutoRefresh={true} />)

    await waitFor(() => {
      expect(screen.getByText('66.7%')).toBeInTheDocument() // 2/3 * 100 = 66.666...
    })
  })

  it('displays 0.0% when no tasks exist', async () => {
    const metrics = {
      ...mockMetrics,
      totalTasks: 0,
      completedTasks: 0,
    }

    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => metrics,
    })

    render(<Dashboard disableAutoRefresh={true} />)

    await waitFor(() => {
      expect(screen.getByText('0.0%')).toBeInTheDocument()
    })
  })

  it('formats average completion time correctly', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockMetrics,
    })

    render(<Dashboard disableAutoRefresh={true} />)

    await waitFor(() => {
      expect(screen.getByText('45.5 min')).toBeInTheDocument()
    })
  })

  it('displays N/A when average completion time is null', async () => {
    const metrics = {
      ...mockMetrics,
      averageCompletionTimeMinutes: null,
    }

    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => metrics,
    })

    render(<Dashboard disableAutoRefresh={true} />)

    await waitFor(() => {
      expect(screen.getByText('N/A')).toBeInTheDocument()
    })
  })

  it('renders priority distribution chart', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockMetrics,
    })

    render(<Dashboard disableAutoRefresh={true} />)

    await waitFor(() => {
      expect(screen.getByText('Priority Distribution')).toBeInTheDocument()
      expect(screen.getByTestId('pie-chart-mock')).toBeInTheDocument()
    })
  })

  it('passes correct data to pie chart', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockMetrics,
    })

    render(<Dashboard disableAutoRefresh={true} />)

    await waitFor(() => {
      const chart = screen.getByTestId('pie-chart-mock')
      expect(chart).toHaveTextContent('CRITICAL: 10')
      expect(chart).toHaveTextContent('HIGH: 20')
      expect(chart).toHaveTextContent('MEDIUM: 30')
      expect(chart).toHaveTextContent('LOW: 40')
    })
  })

  it('filters out zero-value priorities from chart', async () => {
    const metrics = {
      ...mockMetrics,
      tasksByPriority: {
        CRITICAL: 0,
        HIGH: 20,
        MEDIUM: 0,
        LOW: 40
      }
    }

    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => metrics,
    })

    render(<Dashboard disableAutoRefresh={true} />)

    await waitFor(() => {
      const chart = screen.getByTestId('pie-chart-mock')
      expect(chart).not.toHaveTextContent('CRITICAL: 0')
      expect(chart).toHaveTextContent('HIGH: 20')
      expect(chart).not.toHaveTextContent('MEDIUM: 0')
      expect(chart).toHaveTextContent('LOW: 40')
    })
  })

  it('refreshes metrics when refresh button is clicked', async () => {
    const user = userEvent.setup({ delay: null })
    
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockMetrics,
    })

    render(<Dashboard disableAutoRefresh={true} />)

    await waitFor(() => {
      expect(screen.getByText('100')).toBeInTheDocument()
    })

    const updatedMetrics = { ...mockMetrics, totalTasks: 150 }
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => updatedMetrics,
    })

    const refreshButton = screen.getByRole('button', { name: /refresh/i })
    await user.click(refreshButton)

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledTimes(2)
      expect(screen.getByText('150')).toBeInTheDocument()
    })
  })

  it('disables refresh button while loading', async () => {
    const user = userEvent.setup({ delay: null })
    
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockMetrics,
    })

    render(<Dashboard disableAutoRefresh={true} />)

    await waitFor(() => {
      expect(screen.queryByText('Loading dashboard...')).not.toBeInTheDocument()
    })

    global.fetch.mockImplementationOnce(() => new Promise(resolve => {
      setTimeout(() => resolve({
        ok: true,
        json: async () => mockMetrics,
      }), 100)
    }))

    const refreshButton = screen.getByRole('button', { name: /refresh/i })
    await user.click(refreshButton)

    expect(screen.getByRole('button', { name: /loading/i })).toBeDisabled()
  })

  it.skip('auto-refreshes metrics every 30 seconds', async () => {
    vi.useFakeTimers()
    
    global.fetch.mockResolvedValue({
      ok: true,
      json: async () => mockMetrics,
    })

    render(<Dashboard disableAutoRefresh={true} />)

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledTimes(1)
    })

    // Advance time by 30 seconds
    vi.advanceTimersByTime(30000)

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledTimes(2)
    })

    // Advance time by another 30 seconds
    vi.advanceTimersByTime(30000)

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledTimes(3)
    })
    
    vi.useRealTimers()
  })

  it.skip('clears auto-refresh interval on unmount', async () => {
    vi.useFakeTimers()
    
    global.fetch.mockResolvedValue({
      ok: true,
      json: async () => mockMetrics,
    })

    const { unmount } = render(<Dashboard disableAutoRefresh={true} />)

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledTimes(1)
    })

    unmount()

    // Advance time - should not trigger more fetches
    vi.advanceTimersByTime(60000)

    // Still should only be 1 call
    expect(global.fetch).toHaveBeenCalledTimes(1)
    
    vi.useRealTimers()
  })

  it.skip('renders date range filter controls', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockMetrics,
    })

    render(<Dashboard disableAutoRefresh={true} />)

    await waitFor(() => {
      expect(screen.getByLabelText('Date Range:')).toBeInTheDocument()
    })
    
    expect(screen.getByRole('option', { name: 'All Time' })).toBeInTheDocument()
    expect(screen.getByRole('option', { name: 'Today' })).toBeInTheDocument()
    expect(screen.getByRole('option', { name: 'This Week' })).toBeInTheDocument()
    expect(screen.getByRole('option', { name: 'Custom Range' })).toBeInTheDocument()
  })

  it.skip('fetches metrics with today filter', async () => {
    const user = userEvent.setup({ delay: null })
    
    global.fetch.mockResolvedValue({
      ok: true,
      json: async () => mockMetrics,
    })

    render(<Dashboard disableAutoRefresh={true} />)

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith('http://localhost:8080/api/dashboard/metrics')
    })

    const select = screen.getByLabelText('Date Range:')
    await user.selectOptions(select, 'today')

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith('http://localhost:8080/api/dashboard/metrics?range=today')
    })
  })

  it.skip('fetches metrics with this_week filter', async () => {
    const user = userEvent.setup({ delay: null })
    
    global.fetch.mockResolvedValue({
      ok: true,
      json: async () => mockMetrics,
    })

    render(<Dashboard disableAutoRefresh={true} />)

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledTimes(1)
    })

    const select = screen.getByLabelText('Date Range:')
    await user.selectOptions(select, 'this_week')

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith('http://localhost:8080/api/dashboard/metrics?range=this_week')
    })
  })

  it.skip('shows custom date inputs when custom range is selected', async () => {
    const user = userEvent.setup({ delay: null })
    
    global.fetch.mockResolvedValue({
      ok: true,
      json: async () => mockMetrics,
    })

    render(<Dashboard disableAutoRefresh={true} />)

    await waitFor(() => {
      expect(screen.getByLabelText('Date Range:')).toBeInTheDocument()
    })

    const select = screen.getByLabelText('Date Range:')
    await user.selectOptions(select, 'custom')

    await waitFor(() => {
      expect(screen.getByLabelText('Start Date:')).toBeInTheDocument()
      expect(screen.getByLabelText('End Date:')).toBeInTheDocument()
      expect(screen.getByRole('button', { name: 'Apply' })).toBeInTheDocument()
    })
  })

  it.skip('hides custom date inputs when switching away from custom', async () => {
    const user = userEvent.setup({ delay: null })
    
    global.fetch.mockResolvedValue({
      ok: true,
      json: async () => mockMetrics,
    })

    render(<Dashboard disableAutoRefresh={true} />)

    await waitFor(() => {
      expect(screen.getByLabelText('Date Range:')).toBeInTheDocument()
    })

    const select = screen.getByLabelText('Date Range:')
    await user.selectOptions(select, 'custom')

    await waitFor(() => {
      expect(screen.getByLabelText('Start Date:')).toBeInTheDocument()
    })

    await user.selectOptions(select, 'today')

    await waitFor(() => {
      expect(screen.queryByLabelText('Start Date:')).not.toBeInTheDocument()
    })
  })

  it.skip('fetches metrics with custom date range', async () => {
    const user = userEvent.setup({ delay: null })
    
    global.fetch.mockResolvedValue({
      ok: true,
      json: async () => mockMetrics,
    })

    render(<Dashboard disableAutoRefresh={true} />)

    await waitFor(() => {
      expect(screen.getByLabelText('Date Range:')).toBeInTheDocument()
    })

    const select = screen.getByLabelText('Date Range:')
    await user.selectOptions(select, 'custom')

    await waitFor(() => {
      expect(screen.getByLabelText('Start Date:')).toBeInTheDocument()
    })

    const startDateInput = screen.getByLabelText('Start Date:')
    const endDateInput = screen.getByLabelText('End Date:')

    await user.type(startDateInput, '2025-01-01')
    await user.type(endDateInput, '2025-01-31')

    const applyButton = screen.getByRole('button', { name: 'Apply' })
    await user.click(applyButton)

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/dashboard/metrics?range=custom&startDate=2025-01-01&endDate=2025-01-31'
      )
    })
  })

  it.skip('handles retry button on error', async () => {
    const user = userEvent.setup({ delay: null })
    
    global.fetch.mockRejectedValueOnce(new Error('Network error'))

    render(<Dashboard disableAutoRefresh={true} />)

    await waitFor(() => {
      expect(screen.getByText(/error loading dashboard/i)).toBeInTheDocument()
    })

    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockMetrics,
    })

    const retryButton = screen.getByRole('button', { name: /retry/i })
    await user.click(retryButton)

    await waitFor(() => {
      expect(screen.getByText('100')).toBeInTheDocument()
    })
  })

  it.skip('handles non-ok response from server', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: false,
      statusText: 'Server Error'
    })

    render(<Dashboard disableAutoRefresh={true} />)

    await waitFor(() => {
      expect(screen.getByText(/failed to fetch metrics: server error/i)).toBeInTheDocument()
    })
  })

  it.skip('resets custom dates when changing to non-custom range', async () => {
    const user = userEvent.setup({ delay: null })
    
    global.fetch.mockResolvedValue({
      ok: true,
      json: async () => mockMetrics,
    })

    render(<Dashboard disableAutoRefresh={true} />)

    await waitFor(() => {
      expect(screen.getByLabelText('Date Range:')).toBeInTheDocument()
    })

    const select = screen.getByLabelText('Date Range:')
    await user.selectOptions(select, 'custom')

    await waitFor(() => {
      expect(screen.getByLabelText('Start Date:')).toBeInTheDocument()
    })

    const startDateInput = screen.getByLabelText('Start Date:')
    await user.type(startDateInput, '2025-01-01')

    await user.selectOptions(select, 'today')
    await user.selectOptions(select, 'custom')

    await waitFor(() => {
      expect(screen.getByLabelText('Start Date:')).toBeInTheDocument()
    })

    // Custom date inputs should be empty after switching back
    const newStartDateInput = screen.getByLabelText('Start Date:')
    expect(newStartDateInput.value).toBe('')
  })

  it('renders date range select', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockMetrics,
    })

    render(<Dashboard disableAutoRefresh={true} />)

    await waitFor(() => {
      const select = screen.getByLabelText('Date Range:')
      expect(select).toBeInTheDocument()
    })
  })

  it('changes date range when select option is changed', async () => {
    const user = userEvent.setup({ delay: null })
    
    global.fetch.mockResolvedValue({
      ok: true,
      json: async () => mockMetrics,
    })

    render(<Dashboard disableAutoRefresh={true} />)

    await waitFor(() => {
      expect(screen.getByLabelText('Date Range:')).toBeInTheDocument()
    })

    const select = screen.getByLabelText('Date Range:')
    expect(select.value).toBe('')

    await user.selectOptions(select, 'today')
    expect(select.value).toBe('today')
  })

  it('shows custom date inputs with correct labels', async () => {
    const user = userEvent.setup({ delay: null })
    
    global.fetch.mockResolvedValue({
      ok: true,
      json: async () => mockMetrics,
    })

    render(<Dashboard disableAutoRefresh={true} />)

    await waitFor(() => {
      expect(screen.getByLabelText('Date Range:')).toBeInTheDocument()
    })

    const select = screen.getByLabelText('Date Range:')
    await user.selectOptions(select, 'custom')

    await waitFor(() => {
      const startDate = screen.getByLabelText('Start Date:')
      const endDate = screen.getByLabelText('End Date:')
      expect(startDate).toBeInTheDocument()
      expect(endDate).toBeInTheDocument()
      expect(startDate.type).toBe('date')
      expect(endDate.type).toBe('date')
    })
  })

  it('changes start and end date values', async () => {
    const user = userEvent.setup({ delay: null })
    
    global.fetch.mockResolvedValue({
      ok: true,
      json: async () => mockMetrics,
    })

    render(<Dashboard disableAutoRefresh={true} />)

    await waitFor(() => {
      expect(screen.getByLabelText('Date Range:')).toBeInTheDocument()
    })

    const select = screen.getByLabelText('Date Range:')
    await user.selectOptions(select, 'custom')

    await waitFor(() => {
      expect(screen.getByLabelText('Start Date:')).toBeInTheDocument()
    })

    const startDateInput = screen.getByLabelText('Start Date:')
    const endDateInput = screen.getByLabelText('End Date:')

    await user.type(startDateInput, '2025-01-01')
    await user.type(endDateInput, '2025-01-31')

    expect(startDateInput.value).toBe('2025-01-01')
    expect(endDateInput.value).toBe('2025-01-31')
  })

  it('renders Apply button in custom date range', async () => {
    const user = userEvent.setup({ delay: null })
    
    global.fetch.mockResolvedValue({
      ok: true,
      json: async () => mockMetrics,
    })

    render(<Dashboard disableAutoRefresh={true} />)

    await waitFor(() => {
      expect(screen.getByLabelText('Date Range:')).toBeInTheDocument()
    })

    const select = screen.getByLabelText('Date Range:')
    await user.selectOptions(select, 'custom')

    await waitFor(() => {
      const applyButton = screen.getByRole('button', { name: 'Apply' })
      expect(applyButton).toBeInTheDocument()
    })
  })
})
