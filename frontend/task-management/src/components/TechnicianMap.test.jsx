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

  it('renders the map header', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => [],
    })

    render(<TechnicianMap />)
    
    expect(screen.getByText('Technician Locations')).toBeInTheDocument()
    
    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith('http://localhost:8080/api/technicians')
    })
  })

  it('fetches and displays technicians on mount', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTechnicians,
    })

    render(<TechnicianMap />)

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith('http://localhost:8080/api/technicians')
    })

    await waitFor(() => {
      expect(screen.getByText('John Doe')).toBeInTheDocument()
      expect(screen.getByText('Jane Smith')).toBeInTheDocument()
      expect(screen.getByText('Bob Johnson')).toBeInTheDocument()
    })
  })

  it('displays technician status correctly', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTechnicians,
    })

    render(<TechnicianMap />)

    await waitFor(() => {
      expect(screen.getByText('Status: Available')).toBeInTheDocument()
      expect(screen.getByText('Status: Busy')).toBeInTheDocument()
      expect(screen.getByText('Status: Offline')).toBeInTheDocument()
    })
  })

  it('displays technician phone numbers', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTechnicians,
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

    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => techniciansWithInvalidLocation,
    })

    render(<TechnicianMap />)

    await waitFor(() => {
      expect(screen.queryByText('Invalid Tech')).not.toBeInTheDocument()
      expect(screen.queryByText('No Coords Tech')).not.toBeInTheDocument()
      expect(screen.getByText('John Doe')).toBeInTheDocument()
    })
  })

  it('handles refresh button click', async () => {
    global.fetch.mockResolvedValue({
      ok: true,
      json: async () => mockTechnicians,
    })

    const user = userEvent.setup()
    render(<TechnicianMap />)

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledTimes(1)
    })

    const refreshButton = screen.getByText('Refresh')
    await user.click(refreshButton)

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledTimes(2)
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
    global.fetch.mockResolvedValueOnce({
      ok: false,
    })

    render(<TechnicianMap />)

    await waitFor(() => {
      expect(screen.getByRole('alert')).toBeInTheDocument()
      expect(screen.getByText('Failed to fetch technicians')).toBeInTheDocument()
    })
  })

  it('displays no data message when no technicians available', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => [],
    })

    render(<TechnicianMap />)

    await waitFor(() => {
      expect(screen.getByText('No technicians with valid location data found.')).toBeInTheDocument()
    })
  })

  it('displays last update timestamp', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTechnicians,
    })

    render(<TechnicianMap />)

    await waitFor(() => {
      expect(screen.getByText(/Last updated:/)).toBeInTheDocument()
    })
  })

  it('renders legend with status colors', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => [],
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
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTechnicians,
    })

    render(<TechnicianMap />)

    await waitFor(() => {
      const mapContainer = screen.getByTestId('map-container')
      expect(mapContainer).toBeInTheDocument()
    })
  })

  it('creates markers for each technician with valid location', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTechnicians,
    })

    render(<TechnicianMap />)

    await waitFor(() => {
      const markers = screen.getAllByTestId('marker')
      expect(markers).toHaveLength(3)
    })
  })

  it('displays location timestamp in popup', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockTechnicians,
    })

    render(<TechnicianMap />)

    await waitFor(() => {
      const timestamps = screen.getAllByText(/Location updated:/)
      expect(timestamps.length).toBe(3) // One for each technician
    })
  })
})
