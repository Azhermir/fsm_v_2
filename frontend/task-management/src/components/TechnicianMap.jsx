import { useState, useEffect } from 'react'
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import './TechnicianMap.css'

const API_BASE_URL = 'http://localhost:8080'

// Fix for default marker icons in react-leaflet
delete L.Icon.Default.prototype._getIconUrl
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
})

// Create custom marker icons based on technician status
const createMarkerIcon = (status) => {
  const colors = {
    AVAILABLE: '#28a745',  // green
    BUSY: '#ffc107',       // yellow/orange
    OFFLINE: '#6c757d'     // gray
  }
  
  const color = colors[status] || '#6c757d'
  
  return L.divIcon({
    className: 'custom-marker',
    html: `<div style="background-color: ${color}; width: 25px; height: 25px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 5px rgba(0,0,0,0.3);"></div>`,
    iconSize: [25, 25],
    iconAnchor: [12, 12],
    popupAnchor: [0, -12]
  })
}

// Create custom marker icons for tasks based on priority (square shape to distinguish from technicians)
const createTaskMarkerIcon = (priority) => {
  const colors = {
    Critical: '#dc3545',  // red
    High: '#fd7e14',      // orange
    Medium: '#ffc107',    // yellow
    Low: '#28a745'        // green
  }
  
  const color = colors[priority] || '#6c757d'
  
  return L.divIcon({
    className: 'custom-marker task-marker',
    html: `<div style="background-color: ${color}; width: 25px; height: 25px; border: 3px solid white; box-shadow: 0 2px 5px rgba(0,0,0,0.3);"></div>`,
    iconSize: [25, 25],
    iconAnchor: [12, 12],
    popupAnchor: [0, -12]
  })
}

const TechnicianMap = () => {
  const [technicians, setTechnicians] = useState([])
  const [tasks, setTasks] = useState([])
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState('')
  const [lastUpdate, setLastUpdate] = useState(null)

  const fetchTechnicians = async () => {
    // Prevent concurrent fetches
    if (isLoading) return
    
    setIsLoading(true)
    setError('')

    try {
      const response = await fetch(`${API_BASE_URL}/api/technicians`)

      if (!response.ok) {
        throw new Error('Failed to fetch technicians')
      }

      const data = await response.json()
      
      // Filter technicians with valid location data (domain invariant)
      const validTechnicians = data.filter(tech => 
        tech.currentLocation && 
        tech.currentLocation.latitude !== null && 
        tech.currentLocation.longitude !== null
      )

      setTechnicians(validTechnicians)
      setLastUpdate(new Date())
    } catch (err) {
      setError(err.message || 'An error occurred while fetching technicians')
    } finally {
      setIsLoading(false)
    }
  }

  const fetchTasks = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/api/tasks?status=Unassigned`)

      if (!response.ok) {
        throw new Error('Failed to fetch tasks')
      }

      const data = await response.json()
      
      // Filter tasks with valid location data (must have address with geocoded coordinates)
      const validTasks = data.filter(task => 
        task.address && 
        task.address.latitude !== null && 
        task.address.longitude !== null
      )

      setTasks(validTasks)
    } catch (err) {
      // Log error but don't override technician error state
      console.error('Error fetching tasks:', err)
    }
  }

  useEffect(() => {
    // Initial fetch
    fetchTechnicians()
    fetchTasks()

    // Auto-refresh every 30 seconds
    const interval = setInterval(() => {
      fetchTechnicians()
      fetchTasks()
    }, 30000)

    return () => clearInterval(interval)
  }, [])

  const handleRefresh = () => {
    fetchTechnicians()
    fetchTasks()
  }

  const getStatusLabel = (status) => {
    const labels = {
      AVAILABLE: 'Available',
      BUSY: 'Busy',
      OFFLINE: 'Offline'
    }
    return labels[status] || status
  }

  const getStatusClass = (status) => {
    return `status-${status.toLowerCase()}`
  }

  // Default center (can be adjusted based on business needs)
  const defaultCenter = [37.7749, -122.4194] // San Francisco

  // Calculate center based on technicians and tasks if available
  const mapCenter = (() => {
    const allLocations = [
      ...technicians.map(t => ({ lat: t.currentLocation.latitude, lng: t.currentLocation.longitude })),
      ...tasks.map(t => ({ lat: t.address.latitude, lng: t.address.longitude }))
    ]
    
    if (allLocations.length === 0) {
      return defaultCenter
    }
    
    const sum = allLocations.reduce(
      (acc, loc) => ({ lat: acc.lat + loc.lat, lng: acc.lng + loc.lng }),
      { lat: 0, lng: 0 }
    )
    
    return [sum.lat / allLocations.length, sum.lng / allLocations.length]
  })()

  return (
    <div className="technician-map-container">
      <div className="map-header">
        <h2>Technician Locations</h2>
        <div className="map-controls">
          <button 
            onClick={handleRefresh} 
            disabled={isLoading}
            className="refresh-button"
          >
            {isLoading ? 'Refreshing...' : 'Refresh'}
          </button>
          {lastUpdate && (
            <span className="last-update">
              Last updated: {lastUpdate.toLocaleTimeString()}
            </span>
          )}
        </div>
      </div>

      {error && (
        <div className="error-message" role="alert">
          {error}
        </div>
      )}

      <div className="legend">
        <div className="legend-section">
          <strong>Technicians:</strong>
          <span className="legend-item">
            <span className="legend-color technician-marker" style={{ backgroundColor: '#28a745' }}></span>
            Available
          </span>
          <span className="legend-item">
            <span className="legend-color technician-marker" style={{ backgroundColor: '#ffc107' }}></span>
            Busy
          </span>
          <span className="legend-item">
            <span className="legend-color technician-marker" style={{ backgroundColor: '#6c757d' }}></span>
            Offline
          </span>
        </div>
        <div className="legend-section">
          <strong>Tasks:</strong>
          <span className="legend-item">
            <span className="legend-color task-marker" style={{ backgroundColor: '#dc3545' }}></span>
            Critical
          </span>
          <span className="legend-item">
            <span className="legend-color task-marker" style={{ backgroundColor: '#fd7e14' }}></span>
            High
          </span>
          <span className="legend-item">
            <span className="legend-color task-marker" style={{ backgroundColor: '#ffc107' }}></span>
            Medium
          </span>
          <span className="legend-item">
            <span className="legend-color task-marker" style={{ backgroundColor: '#28a745' }}></span>
            Low
          </span>
        </div>
      </div>

      <div className="map-wrapper">
        <MapContainer 
          center={mapCenter} 
          zoom={13} 
          style={{ height: '100%', width: '100%' }}
          scrollWheelZoom={true}
        >
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />
          
          {technicians.map((technician) => (
            <Marker
              key={technician.id}
              position={[
                technician.currentLocation.latitude,
                technician.currentLocation.longitude
              ]}
              icon={createMarkerIcon(technician.status)}
            >
              <Popup>
                <div className="technician-popup">
                  <h3>{technician.name}</h3>
                  <p className={`status ${getStatusClass(technician.status)}`}>
                    Status: {getStatusLabel(technician.status)}
                  </p>
                  {technician.phone && (
                    <p className="phone">Phone: {technician.phone}</p>
                  )}
                  {technician.currentLocation.timestamp && (
                    <p className="location-time">
                      Location updated: {new Date(technician.currentLocation.timestamp).toLocaleString()}
                    </p>
                  )}
                </div>
              </Popup>
            </Marker>
          ))}
          
          {tasks.map((task) => (
            <Marker
              key={`task-${task.id}`}
              position={[
                task.address.latitude,
                task.address.longitude
              ]}
              icon={createTaskMarkerIcon(task.priority)}
            >
              <Popup>
                <div className="task-popup">
                  <h3>{task.title}</h3>
                  <p className={`priority priority-${task.priority.toLowerCase()}`}>
                    Priority: {task.priority}
                  </p>
                  {task.description && (
                    <p className="description">{task.description}</p>
                  )}
                  {task.address && (
                    <div className="address">
                      <p><strong>Location:</strong></p>
                      <p>{task.address.street}</p>
                      <p>{task.address.city}, {task.address.state} {task.address.zipCode}</p>
                    </div>
                  )}
                  {task.dueDate && (
                    <p className="due-date">
                      Due: {new Date(task.dueDate).toLocaleDateString()}
                    </p>
                  )}
                </div>
              </Popup>
            </Marker>
          ))}
        </MapContainer>
      </div>

      {technicians.length === 0 && !isLoading && !error && (
        <div className="no-data-message">
          No technicians with valid location data found.
        </div>
      )}
    </div>
  )
}

export default TechnicianMap
