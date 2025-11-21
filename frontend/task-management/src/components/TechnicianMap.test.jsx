import { render, screen, waitFor, cleanup, act } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import TechnicianMap from './TechnicianMap'

// Mock react-leaflet components
vi.mock('react-leaflet', () => ({
  MapContainer: ({ children, ...props }) => (
    <div data-testid="map-container" {...props}>{children}</div>
  ),
  TileLayer: () => <div data-testid="tile-layer" />,
  Marker: ({ children, position }) => (
    <div data-testid="marker" data-position={JSON.stringify(position)}>
      {children}
    </div>
  ),
  Popup: ({ children }) => <div data-testid="popup">{children}</div>,
}))

// Mock leaflet
vi.mock('leaflet', () => ({
  default: {
    Icon: {
      Default: {
        prototype: { _getIconUrl: null },
        mergeOptions: vi.fn(),
      },
    },
    divIcon: vi.fn((options) => options),
  },
}))

describe('TechnicianMap', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.resetAllMocks()
    global.fetch = vi.fn()
  })

  afterEach(() => {
    cleanup()
  })

  const mockTechnicians = [
    {
      id: 1,
      name: 'John Doe',
      email: 'john@example.com',
      phone: '555-1234',
      status: 'AVAILABLE',
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
      status: 'BUSY',
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
      status: 'OFFLINE',
      currentLocation: {
        latitude: 37.7649,
        longitude: -122.4294,
        timestamp: '2025-11-20T21:50:00',
      },
    },
  ]

  const mockTasks = [
    {
      id: 1,
      title: 'Fix HVAC System',
      description: 'Air conditioning not working',
      priority: 'Critical',
      status: 'Unassigned',
      dueDate: '2025-11-25T10:00:00',
      address: {
        street: '123 Main St',
        city: 'San Francisco',
        state: 'CA',
        zipCode: '94105',
        latitude: 37.7899,
        longitude: -122.3999,
      },
    },
    {
      id: 2,
      title: 'Plumbing Repair',
      description: 'Leaky faucet',
      priority: 'High',
      status: 'Unassigned',
      dueDate: '2025-11-26T14:00:00',
      address: {
        street: '456 Oak Ave',
        city: 'San Francisco',
        state: 'CA',
        zipCode: '94102',
        latitude: 37.7799,
        longitude: -122.4099,
      },
    },
    {
      id: 3,
      title: 'Electrical Inspection',
      description: 'Routine inspection',
      priority: 'Medium',
      status: 'Unassigned',
      dueDate: '2025-11-27T09:00:00',
      address: {
        street: '789 Pine St',
        city: 'San Francisco',
        state: 'CA',
        zipCode: '94108',
        latitude: 37.7699,
        longitude: -122.4199,
      },
    },
    {
      id: 4,
      title: 'Paint Touch-up',
      description: 'Minor paint work',
      priority: 'Low',
      status: 'Unassigned',
      dueDate: '2025-11-28T11:00:00',
      address: {
        street: '321 Elm St',
        city: 'San Francisco',
        state: 'CA',
        zipCode: '94109',
        latitude: 37.7599,
        longitude: -122.4299,
      },
    },
  ]

  it('renders the map header', async () => {
    global.fetch.mockImplementation((url) => {
      if (url.includes('/api/technicians')) {
        return Promise.resolve({
          ok: true,
          json: async () => [],
        })
      }
      if (url.includes('/api/tasks')) {
        return Promise.resolve({
          ok: true,
          json: async () => [],
        })
      }
    })

    render(<TechnicianMap />)
    
    expect(screen.getByText('Technician Locations')).toBeInTheDocument()
    
    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith('http://localhost:8080/api/technicians')
      expect(global.fetch).toHaveBeenCalledWith('http://localhost:8080/api/tasks?status=Unassigned')
    })
  })

  it('fetches and displays technicians on mount', async () => {
    global.fetch.mockImplementation((url) => {
      if (url.includes('/api/technicians')) {
        return Promise.resolve({
          ok: true,
          json: async () => mockTechnicians,
        })
      }
      if (url.includes('/api/tasks')) {
        return Promise.resolve({
          ok: true,
          json: async () => [],
        })
      }
    })

    render(<TechnicianMap />)

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith('http://localhost:8080/api/technicians')
      expect(global.fetch).toHaveBeenCalledWith('http://localhost:8080/api/tasks?status=Unassigned')
    })

    await waitFor(() => {
      expect(screen.getByText('John Doe')).toBeInTheDocument()
      expect(screen.getByText('Jane Smith')).toBeInTheDocument()
      expect(screen.getByText('Bob Johnson')).toBeInTheDocument()
    })
  })

  it('displays technician status correctly', async () => {
    global.fetch.mockImplementation((url) => {
      if (url.includes('/api/technicians')) {
        return Promise.resolve({
          ok: true,
          json: async () => mockTechnicians,
        })
      }
      if (url.includes('/api/tasks')) {
        return Promise.resolve({
          ok: true,
          json: async () => [],
        })
      }
    })

    render(<TechnicianMap />)

    await waitFor(() => {
      expect(screen.getByText('Status: Available')).toBeInTheDocument()
      expect(screen.getByText('Status: Busy')).toBeInTheDocument()
      expect(screen.getByText('Status: Offline')).toBeInTheDocument()
    })
  })

  it('displays technician phone numbers', async () => {
    global.fetch.mockImplementation((url) => {
      if (url.includes('/api/technicians')) {
        return Promise.resolve({
          ok: true,
          json: async () => mockTechnicians,
        })
      }
      if (url.includes('/api/tasks')) {
        return Promise.resolve({
          ok: true,
          json: async () => [],
        })
      }
    })

    render(<TechnicianMap />)

    await waitFor(() => {
      expect(screen.getByText('Phone: 555-1234')).toBeInTheDocument()
      expect(screen.getByText('Phone: 555-5678')).toBeInTheDocument()
      expect(screen.getByText('Phone: 555-9012')).toBeInTheDocument()
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

    global.fetch.mockImplementation((url) => {
      if (url.includes('/api/technicians')) {
        return Promise.resolve({
          ok: true,
          json: async () => techniciansWithInvalidLocation,
        })
      }
      if (url.includes('/api/tasks')) {
        return Promise.resolve({
          ok: true,
          json: async () => [],
        })
      }
    })

    render(<TechnicianMap />)

    await waitFor(() => {
      expect(screen.queryByText('Invalid Tech')).not.toBeInTheDocument()
      expect(screen.queryByText('No Coords Tech')).not.toBeInTheDocument()
      expect(screen.getByText('John Doe')).toBeInTheDocument()
    })
  })

  it('handles refresh button click', async () => {
    global.fetch.mockImplementation((url) => {
      if (url.includes('/api/technicians')) {
        return Promise.resolve({
          ok: true,
          json: async () => mockTechnicians,
        })
      }
      if (url.includes('/api/tasks')) {
        return Promise.resolve({
          ok: true,
          json: async () => [],
        })
      }
    })

    const user = userEvent.setup()
    render(<TechnicianMap />)

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledTimes(2)
    })

    const refreshButton = screen.getByText('Refresh')
    await user.click(refreshButton)

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledTimes(4)
    })
  })

  it('displays error message when fetch fails', async () => {
    global.fetch.mockRejectedValueOnce(new Error('Network error'))

    render(<TechnicianMap />)

    await waitFor(() => {
      expect(screen.getByRole('alert')).toBeInTheDocument()
      expect(screen.getByText('Network error')).toBeInTheDocument()
    })
  })

  it('displays error message when API returns non-ok response', async () => {
    global.fetch.mockImplementation((url) => {
      if (url.includes('/api/technicians')) {
        return Promise.resolve({
          ok: false,
        })
      }
      if (url.includes('/api/tasks')) {
        return Promise.resolve({
          ok: true,
          json: async () => [],
        })
      }
    })

    render(<TechnicianMap />)

    await waitFor(() => {
      expect(screen.getByRole('alert')).toBeInTheDocument()
      expect(screen.getByText('Failed to fetch technicians')).toBeInTheDocument()
    })
  })

  it('displays no data message when no technicians available', async () => {
    global.fetch.mockImplementation((url) => {
      if (url.includes('/api/technicians')) {
        return Promise.resolve({
          ok: true,
          json: async () => [],
        })
      }
      if (url.includes('/api/tasks')) {
        return Promise.resolve({
          ok: true,
          json: async () => [],
        })
      }
    })

    render(<TechnicianMap />)

    await waitFor(() => {
      expect(screen.getByText('No technicians with valid location data found.')).toBeInTheDocument()
    })
  })

  it('displays last update timestamp', async () => {
    global.fetch.mockImplementation((url) => {
      if (url.includes('/api/technicians')) {
        return Promise.resolve({
          ok: true,
          json: async () => mockTechnicians,
        })
      }
      if (url.includes('/api/tasks')) {
        return Promise.resolve({
          ok: true,
          json: async () => [],
        })
      }
    })

    render(<TechnicianMap />)

    await waitFor(() => {
      expect(screen.getByText(/Last updated:/)).toBeInTheDocument()
    })
  })

  it('renders legend with status colors', async () => {
    global.fetch.mockImplementation((url) => {
      if (url.includes('/api/technicians')) {
        return Promise.resolve({
          ok: true,
          json: async () => [],
        })
      }
      if (url.includes('/api/tasks')) {
        return Promise.resolve({
          ok: true,
          json: async () => [],
        })
      }
    })

    render(<TechnicianMap />)

    await waitFor(() => {
      const legend = document.querySelector('.legend')
      expect(legend).toBeInTheDocument()
      expect(legend.textContent).toContain('Available')
      expect(legend.textContent).toContain('Busy')
      expect(legend.textContent).toContain('Offline')
    })
  })

  it('renders map container with correct props', async () => {
    global.fetch.mockImplementation((url) => {
      if (url.includes('/api/technicians')) {
        return Promise.resolve({
          ok: true,
          json: async () => mockTechnicians,
        })
      }
      if (url.includes('/api/tasks')) {
        return Promise.resolve({
          ok: true,
          json: async () => [],
        })
      }
    })

    render(<TechnicianMap />)

    await waitFor(() => {
      const mapContainer = screen.getByTestId('map-container')
      expect(mapContainer).toBeInTheDocument()
    })
  })

  it('creates markers for each technician with valid location', async () => {
    global.fetch.mockImplementation((url) => {
      if (url.includes('/api/technicians')) {
        return Promise.resolve({
          ok: true,
          json: async () => mockTechnicians,
        })
      }
      if (url.includes('/api/tasks')) {
        return Promise.resolve({
          ok: true,
          json: async () => [],
        })
      }
    })

    render(<TechnicianMap />)

    await waitFor(() => {
      const markers = screen.getAllByTestId('marker')
      expect(markers).toHaveLength(3)
    })
  })

  it('displays location timestamp in popup', async () => {
    global.fetch.mockImplementation((url) => {
      if (url.includes('/api/technicians')) {
        return Promise.resolve({
          ok: true,
          json: async () => mockTechnicians,
        })
      }
      if (url.includes('/api/tasks')) {
        return Promise.resolve({
          ok: true,
          json: async () => [],
        })
      }
    })

    render(<TechnicianMap />)

    await waitFor(() => {
      const timestamps = screen.getAllByText(/Location updated:/)
      expect(timestamps.length).toBe(3) // One for each technician
    })
  })

  // Task-related tests
  it('fetches and displays unassigned tasks on mount', async () => {
    global.fetch.mockImplementation((url) => {
      if (url.includes('/api/technicians')) {
        return Promise.resolve({
          ok: true,
          json: async () => [],
        })
      }
      if (url.includes('/api/tasks')) {
        return Promise.resolve({
          ok: true,
          json: async () => mockTasks,
        })
      }
    })

    render(<TechnicianMap />)

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith('http://localhost:8080/api/tasks?status=Unassigned')
    })

    await waitFor(() => {
      expect(screen.getByText('Fix HVAC System')).toBeInTheDocument()
      expect(screen.getByText('Plumbing Repair')).toBeInTheDocument()
      expect(screen.getByText('Electrical Inspection')).toBeInTheDocument()
      expect(screen.getByText('Paint Touch-up')).toBeInTheDocument()
    })
  })

  it('displays task priority correctly', async () => {
    global.fetch.mockImplementation((url) => {
      if (url.includes('/api/technicians')) {
        return Promise.resolve({
          ok: true,
          json: async () => [],
        })
      }
      if (url.includes('/api/tasks')) {
        return Promise.resolve({
          ok: true,
          json: async () => mockTasks,
        })
      }
    })

    render(<TechnicianMap />)

    await waitFor(() => {
      expect(screen.getByText('Priority: Critical')).toBeInTheDocument()
      expect(screen.getByText('Priority: High')).toBeInTheDocument()
      expect(screen.getByText('Priority: Medium')).toBeInTheDocument()
      expect(screen.getByText('Priority: Low')).toBeInTheDocument()
    })
  })

  it('displays task details in popup', async () => {
    global.fetch.mockImplementation((url) => {
      if (url.includes('/api/technicians')) {
        return Promise.resolve({
          ok: true,
          json: async () => [],
        })
      }
      if (url.includes('/api/tasks')) {
        return Promise.resolve({
          ok: true,
          json: async () => [mockTasks[0]],
        })
      }
    })

    render(<TechnicianMap />)

    await waitFor(() => {
      expect(screen.getByText('Fix HVAC System')).toBeInTheDocument()
      expect(screen.getByText('Air conditioning not working')).toBeInTheDocument()
      expect(screen.getByText('123 Main St')).toBeInTheDocument()
      expect(screen.getByText('San Francisco, CA 94105')).toBeInTheDocument()
    })
  })

  it('filters out tasks without valid location data', async () => {
    const tasksWithInvalidLocation = [
      ...mockTasks,
      {
        id: 5,
        title: 'Invalid Task',
        description: 'No location',
        priority: 'High',
        status: 'Unassigned',
        address: null,
      },
      {
        id: 6,
        title: 'No Coords Task',
        description: 'No coordinates',
        priority: 'Medium',
        status: 'Unassigned',
        address: {
          street: '999 Invalid St',
          city: 'San Francisco',
          state: 'CA',
          zipCode: '94199',
          latitude: null,
          longitude: null,
        },
      },
    ]

    global.fetch.mockImplementation((url) => {
      if (url.includes('/api/technicians')) {
        return Promise.resolve({
          ok: true,
          json: async () => [],
        })
      }
      if (url.includes('/api/tasks')) {
        return Promise.resolve({
          ok: true,
          json: async () => tasksWithInvalidLocation,
        })
      }
    })

    render(<TechnicianMap />)

    await waitFor(() => {
      expect(screen.queryByText('Invalid Task')).not.toBeInTheDocument()
      expect(screen.queryByText('No Coords Task')).not.toBeInTheDocument()
      expect(screen.getByText('Fix HVAC System')).toBeInTheDocument()
    })
  })

  it('creates markers for both technicians and tasks', async () => {
    global.fetch.mockImplementation((url) => {
      if (url.includes('/api/technicians')) {
        return Promise.resolve({
          ok: true,
          json: async () => mockTechnicians,
        })
      }
      if (url.includes('/api/tasks')) {
        return Promise.resolve({
          ok: true,
          json: async () => mockTasks,
        })
      }
    })

    render(<TechnicianMap />)

    await waitFor(() => {
      const markers = screen.getAllByTestId('marker')
      expect(markers).toHaveLength(7) // 3 technicians + 4 tasks
    })
  })

  it('displays updated legend with task priorities', async () => {
    global.fetch.mockImplementation((url) => {
      if (url.includes('/api/technicians')) {
        return Promise.resolve({
          ok: true,
          json: async () => [],
        })
      }
      if (url.includes('/api/tasks')) {
        return Promise.resolve({
          ok: true,
          json: async () => [],
        })
      }
    })

    render(<TechnicianMap />)

    await waitFor(() => {
      const legend = document.querySelector('.legend')
      expect(legend).toBeInTheDocument()
      expect(legend.textContent).toContain('Technicians:')
      expect(legend.textContent).toContain('Tasks:')
      expect(legend.textContent).toContain('Critical')
      expect(legend.textContent).toContain('High')
      expect(legend.textContent).toContain('Medium')
      expect(legend.textContent).toContain('Low')
    })
  })

  it('refreshes both technicians and tasks when refresh button clicked', async () => {
    global.fetch.mockImplementation((url) => {
      if (url.includes('/api/technicians')) {
        return Promise.resolve({
          ok: true,
          json: async () => mockTechnicians,
        })
      }
      if (url.includes('/api/tasks')) {
        return Promise.resolve({
          ok: true,
          json: async () => mockTasks,
        })
      }
    })

    const user = userEvent.setup()
    render(<TechnicianMap />)

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledTimes(2) // Initial calls
    })

    const refreshButton = screen.getByText('Refresh')
    await user.click(refreshButton)

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledTimes(4) // 2 more calls after refresh
    })
  })

  it('handles task fetch errors gracefully without affecting technician display', async () => {
    global.fetch.mockImplementation((url) => {
      if (url.includes('/api/technicians')) {
        return Promise.resolve({
          ok: true,
          json: async () => mockTechnicians,
        })
      }
      if (url.includes('/api/tasks')) {
        return Promise.reject(new Error('Task fetch failed'))
      }
    })

    // Spy on console.error to verify error logging
    const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

    render(<TechnicianMap />)

    await waitFor(() => {
      // Technicians should still be displayed
      expect(screen.getByText('John Doe')).toBeInTheDocument()
      // Error should be logged
      expect(consoleErrorSpy).toHaveBeenCalled()
    })

    consoleErrorSpy.mockRestore()
  })

  it('displays task due date in popup', async () => {
    global.fetch.mockImplementation((url) => {
      if (url.includes('/api/technicians')) {
        return Promise.resolve({
          ok: true,
          json: async () => [],
        })
      }
      if (url.includes('/api/tasks')) {
        return Promise.resolve({
          ok: true,
          json: async () => [mockTasks[0]],
        })
      }
    })

    render(<TechnicianMap />)

    await waitFor(() => {
      expect(screen.getByText(/Due:/)).toBeInTheDocument()
    })
  })
})
