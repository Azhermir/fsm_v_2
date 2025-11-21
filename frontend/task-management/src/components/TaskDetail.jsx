import { useState } from 'react'
import PropTypes from 'prop-types'
import ReassignModal from './ReassignModal'
import './TaskDetail.css'

const TaskDetail = ({ task, onClose, onReassign }) => {
  const [showReassignModal, setShowReassignModal] = useState(false)

  const getPriorityClass = (priority) => {
    return `priority-${priority.toLowerCase()}`
  }

  const getStatusClass = (status) => {
    return `status-${status.toLowerCase()}`
  }

  const formatDate = (dateString) => {
    const date = new Date(dateString)
    return date.toLocaleString()
  }

  const handleReassign = async (technicianId, reason) => {
    await onReassign(task.id, technicianId, reason)
    setShowReassignModal(false)
  }

  const canReassign = task.status === 'ASSIGNED'

  return (
    <div className="task-detail-overlay" onClick={onClose}>
      <div className="task-detail" onClick={(e) => e.stopPropagation()}>
        <div className="task-detail-header">
          <h2>Task Details</h2>
          <button className="close-button" onClick={onClose} aria-label="Close">
            ×
          </button>
        </div>

        <div className="task-detail-content">
          <div className="detail-section">
            <h3>Title</h3>
            <p>{task.title}</p>
          </div>

          {task.description && (
            <div className="detail-section">
              <h3>Description</h3>
              <p>{task.description}</p>
            </div>
          )}

          <div className="detail-section">
            <h3>Client Address</h3>
            <p>{task.clientAddress}</p>
          </div>

          <div className="detail-row">
            <div className="detail-section">
              <h3>Priority</h3>
              <span className={`priority-badge ${getPriorityClass(task.priority)}`}>
                {task.priority}
              </span>
            </div>

            <div className="detail-section">
              <h3>Status</h3>
              <span className={`status-badge ${getStatusClass(task.status)}`}>
                {task.status}
              </span>
            </div>
          </div>

          <div className="detail-row">
            <div className="detail-section">
              <h3>Estimated Duration</h3>
              <p>{(task.estimatedDuration / 60).toFixed(1)} hours</p>
            </div>

            <div className="detail-section">
              <h3>Created At</h3>
              <p>{formatDate(task.createdAt)}</p>
            </div>
          </div>

          {task.assignedTechnicianId && (
            <div className="detail-section">
              <h3>Assigned Technician</h3>
              <p>Technician #{task.assignedTechnicianId}</p>
            </div>
          )}
        </div>

        <div className="task-detail-actions">
          {canReassign && (
            <button
              className="reassign-button"
              onClick={() => setShowReassignModal(true)}
            >
              Reassign
            </button>
          )}
          <button className="close-action-button" onClick={onClose}>
            Close
          </button>
        </div>

        {showReassignModal && (
          <ReassignModal
            task={task}
            onReassign={handleReassign}
            onClose={() => setShowReassignModal(false)}
          />
        )}
      </div>
    </div>
  )
}

TaskDetail.propTypes = {
  task: PropTypes.shape({
    id: PropTypes.number.isRequired,
    title: PropTypes.string.isRequired,
    description: PropTypes.string,
    clientAddress: PropTypes.string.isRequired,
    priority: PropTypes.string.isRequired,
    status: PropTypes.string.isRequired,
    estimatedDuration: PropTypes.number.isRequired,
    createdAt: PropTypes.string.isRequired,
    assignedTechnicianId: PropTypes.number,
  }).isRequired,
  onClose: PropTypes.func.isRequired,
  onReassign: PropTypes.func.isRequired,
}

export default TaskDetail
