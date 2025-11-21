import { useState, useEffect, useCallback } from 'react'
import PropTypes from 'prop-types'
import './AssignModal.css'

const API_BASE_URL = 'http://localhost:8080'

// Helper function to calculate distance between two points using Haversine formula
const calculateDistance = (lat1, lon1, lat2, lon2) => {
  const R = 6371 // Radius of Earth in kilometers
  const dLat = (lat2 - lat1) * Math.PI / 180
  const dLon = (lon2 - lon1) * Math.PI / 180
  const a = 
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
    Math.sin(dLon / 2) * Math.sin(dLon / 2)
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  return R * c // Distance in kilometers
}

const AssignModal = ({ task, onAssign, onClose }) => {
  const [technicians, setTechnicians] = useState([])
  const [selectedTechnicianId, setSelectedTechnicianId] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState('')
  const [isFetchingTechnicians, setIsFetchingTechnicians] = useState(false)

  const fetchTechnicians = useCallback(async () => {
    setIsFetchingTechnicians(true)
    setError('')

    try {
      const response = await fetch(`${API_BASE_URL}/api/technicians`)

      if (!response.ok) {
        throw new Error('Failed to fetch technicians')
      }

      const data = await response.json()
      
      // Domain invariant: Only show available technicians
      const availableTechnicians = data.filter(
        (tech) => tech.status === 'AVAILABLE'
      )
      
      // Calculate distance from task location and sort by distance
      const techniciansWithDistance = availableTechnicians
        .filter(tech => 
          tech.currentLocation && 
          tech.currentLocation.latitude !== null && 
          tech.currentLocation.longitude !== null
        )
        .map(tech => ({
          ...tech,
          distanceToTask: calculateDistance(
            task.address.latitude,
            task.address.longitude,
            tech.currentLocation.latitude,
            tech.currentLocation.longitude
          )
        }))
        .sort((a, b) => a.distanceToTask - b.distanceToTask)
      
      setTechnicians(techniciansWithDistance)
    } catch (err) {
      setError(err.message || 'An error occurred while fetching technicians')
    } finally {
      setIsFetchingTechnicians(false)
    }
  }, [task.address.latitude, task.address.longitude])

  useEffect(() => {
    fetchTechnicians()
  }, [fetchTechnicians])

  const handleSubmit = async (e) => {
    e.preventDefault()

    if (!selectedTechnicianId) {
      setError('Please select a technician')
      return
    }

    setIsLoading(true)
    setError('')

    try {
      await onAssign(parseInt(selectedTechnicianId, 10))
    } catch (err) {
      setError(err.message || 'An error occurred during assignment')
      setIsLoading(false)
    }
  }

  const formatDistance = (km) => {
    if (km < 1) {
      return `${Math.round(km * 1000)}m`
    }
    return `${km.toFixed(1)}km`
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h3>Assign Task</h3>
          <button
            className="modal-close-button"
            onClick={onClose}
            aria-label="Close"
          >
            ×
          </button>
        </div>

        <div className="modal-body">
          <div className="task-info">
            <strong>Task:</strong> {task.title}
            <br />
            <strong>Priority:</strong> {task.priority}
          </div>

          {error && (
            <div className="modal-error" role="alert">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label htmlFor="technician">
                Select Technician <span className="required">*</span>
              </label>
              {isFetchingTechnicians ? (
                <div className="loading-text">Loading technicians...</div>
              ) : technicians.length === 0 ? (
                <div className="no-technicians">
                  No available technicians found for assignment.
                </div>
              ) : (
                <select
                  id="technician"
                  value={selectedTechnicianId}
                  onChange={(e) => setSelectedTechnicianId(e.target.value)}
                  required
                  disabled={isLoading}
                >
                  <option value="">-- Select Technician --</option>
                  {technicians.map((tech) => (
                    <option key={tech.id} value={tech.id}>
                      {tech.name} - {formatDistance(tech.distanceToTask)} away
                      {tech.skillLevel && ` (${tech.skillLevel})`}
                    </option>
                  ))}
                </select>
              )}
            </div>

            {selectedTechnicianId && (
              <div className="technician-details">
                {(() => {
                  const selectedTech = technicians.find(
                    t => t.id === parseInt(selectedTechnicianId, 10)
                  )
                  if (!selectedTech) return null
                  
                  return (
                    <>
                      <h4>Technician Details</h4>
                      <div className="detail-row">
                        <span className="detail-label">Name:</span>
                        <span className="detail-value">{selectedTech.name}</span>
                      </div>
                      <div className="detail-row">
                        <span className="detail-label">Distance:</span>
                        <span className="detail-value">{formatDistance(selectedTech.distanceToTask)}</span>
                      </div>
                      <div className="detail-row">
                        <span className="detail-label">Status:</span>
                        <span className={`detail-value status-badge status-${selectedTech.status.toLowerCase()}`}>
                          {selectedTech.status}
                        </span>
                      </div>
                      {selectedTech.skillLevel && (
                        <div className="detail-row">
                          <span className="detail-label">Skill Level:</span>
                          <span className="detail-value">{selectedTech.skillLevel}</span>
                        </div>
                      )}
                      {selectedTech.phone && (
                        <div className="detail-row">
                          <span className="detail-label">Phone:</span>
                          <span className="detail-value">{selectedTech.phone}</span>
                        </div>
                      )}
                    </>
                  )
                })()}
              </div>
            )}

            <div className="modal-actions">
              <button
                type="button"
                className="cancel-button"
                onClick={onClose}
                disabled={isLoading}
              >
                Cancel
              </button>
              <button
                type="submit"
                className="submit-button"
                disabled={isLoading || !selectedTechnicianId || isFetchingTechnicians || technicians.length === 0}
              >
                {isLoading ? 'Assigning...' : 'Assign'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}

AssignModal.propTypes = {
  task: PropTypes.shape({
    id: PropTypes.number.isRequired,
    title: PropTypes.string.isRequired,
    priority: PropTypes.string.isRequired,
    address: PropTypes.shape({
      latitude: PropTypes.number.isRequired,
      longitude: PropTypes.number.isRequired,
    }).isRequired,
  }).isRequired,
  onAssign: PropTypes.func.isRequired,
  onClose: PropTypes.func.isRequired,
}

export default AssignModal
