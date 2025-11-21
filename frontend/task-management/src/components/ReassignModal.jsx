import { useState, useEffect, useCallback } from 'react'
import PropTypes from 'prop-types'
import './ReassignModal.css'

const API_BASE_URL = 'http://localhost:8080'

const REASON_OPTIONS = [
  { value: 'technician_unavailable', label: 'Technician Unavailable' },
  { value: 'urgent_priority', label: 'Urgent Priority' },
  { value: 'closer_location', label: 'Closer Location' },
  { value: 'skill_mismatch', label: 'Skill Mismatch' },
  { value: 'other', label: 'Other' },
]

const ReassignModal = ({ task, onReassign, onClose }) => {
  const [technicians, setTechnicians] = useState([])
  const [selectedTechnicianId, setSelectedTechnicianId] = useState('')
  const [reason, setReason] = useState('')
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
      // Filter out the currently assigned technician
      const availableTechnicians = data.filter(
        (tech) => tech.id !== task.assignedTechnicianId
      )
      setTechnicians(availableTechnicians)
    } catch (err) {
      setError(err.message || 'An error occurred while fetching technicians')
    } finally {
      setIsFetchingTechnicians(false)
    }
  }, [task.assignedTechnicianId])

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
      await onReassign(parseInt(selectedTechnicianId, 10), reason || null)
    } catch (err) {
      setError(err.message || 'An error occurred during reassignment')
      setIsLoading(false)
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h3>Reassign Task</h3>
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
          </div>

          {error && (
            <div className="modal-error" role="alert">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label htmlFor="technician">
                Select New Technician <span className="required">*</span>
              </label>
              {isFetchingTechnicians ? (
                <div className="loading-text">Loading technicians...</div>
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
                      {tech.name} {tech.skillLevel && `(${tech.skillLevel})`}
                    </option>
                  ))}
                </select>
              )}
            </div>

            <div className="form-group">
              <label htmlFor="reason">Reason (Optional)</label>
              <select
                id="reason"
                value={reason}
                onChange={(e) => setReason(e.target.value)}
                disabled={isLoading}
              >
                <option value="">-- Select Reason --</option>
                {REASON_OPTIONS.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </div>

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
                disabled={isLoading || !selectedTechnicianId || isFetchingTechnicians}
              >
                {isLoading ? 'Reassigning...' : 'Reassign'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}

ReassignModal.propTypes = {
  task: PropTypes.shape({
    id: PropTypes.number.isRequired,
    title: PropTypes.string.isRequired,
    assignedTechnicianId: PropTypes.number,
  }).isRequired,
  onReassign: PropTypes.func.isRequired,
  onClose: PropTypes.func.isRequired,
}

export default ReassignModal
