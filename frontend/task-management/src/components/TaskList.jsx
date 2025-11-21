import { useState, useEffect } from 'react'
import TaskDetail from './TaskDetail'
import './TaskList.css'

const API_BASE_URL = 'http://localhost:8080'

const TaskList = () => {
  const [tasks, setTasks] = useState([])
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState('')
  const [selectedTask, setSelectedTask] = useState(null)
  const [successMessage, setSuccessMessage] = useState('')

  const fetchTasks = async () => {
    setIsLoading(true)
    setError('')

    try {
      const response = await fetch(`${API_BASE_URL}/api/tasks`)

      if (!response.ok) {
        throw new Error('Failed to fetch tasks')
      }

      const data = await response.json()
      
      // Sort tasks by priority (CRITICAL > HIGH > MEDIUM > LOW) and then by creation date (newest first)
      const priorityOrder = { CRITICAL: 0, HIGH: 1, MEDIUM: 2, LOW: 3 }
      const sortedTasks = [...data].sort((a, b) => {
        const priorityDiff = priorityOrder[a.priority] - priorityOrder[b.priority]
        if (priorityDiff !== 0) return priorityDiff
        return new Date(b.createdAt) - new Date(a.createdAt)
      })

      setTasks(sortedTasks)
    } catch (err) {
      setError(err.message || 'An error occurred while fetching tasks')
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    fetchTasks()
  }, [])

  const handleRefresh = () => {
    fetchTasks()
  }

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

  const handleTaskClick = (task) => {
    setSelectedTask(task)
    setSuccessMessage('')
  }

  const handleCloseDetail = () => {
    setSelectedTask(null)
  }

  const handleReassign = async (taskId, technicianId, reason) => {
    try {
      const response = await fetch(`${API_BASE_URL}/api/tasks/${taskId}/reassign`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          technicianId,
          reason,
        }),
      })

      if (!response.ok) {
        const error = await response.json()
        throw new Error(error.message || 'Failed to reassign task')
      }

      // Refresh tasks and close modal
      await fetchTasks()
      setSuccessMessage('Task reassigned successfully!')
      setSelectedTask(null)
      
      // Clear success message after 5 seconds
      setTimeout(() => setSuccessMessage(''), 5000)
    } catch (err) {
      throw new Error(err.message || 'An error occurred during reassignment')
    }
  }

  return (
    <div className="task-list">
      <div className="task-list-header">
        <h2>Service Tasks</h2>
        <button
          onClick={handleRefresh}
          className="refresh-button"
          disabled={isLoading}
        >
          {isLoading ? 'Loading...' : 'Refresh'}
        </button>
      </div>

      {error && (
        <div className="message error" role="alert">
          {error}
        </div>
      )}

      {successMessage && (
        <div className="message success" role="alert">
          {successMessage}
        </div>
      )}

      {isLoading && tasks.length === 0 ? (
        <div className="loading">Loading tasks...</div>
      ) : tasks.length === 0 ? (
        <div className="no-tasks">No tasks found</div>
      ) : (
        <div className="task-table-container">
          <table className="task-table">
            <thead>
              <tr>
                <th>Title</th>
                <th>Priority</th>
                <th>Address</th>
                <th>Status</th>
                <th>Duration (hrs)</th>
                <th>Created</th>
              </tr>
            </thead>
            <tbody>
              {tasks.map((task) => (
                <tr 
                  key={task.id}
                  onClick={() => handleTaskClick(task)}
                  className="task-row"
                  role="button"
                  tabIndex={0}
                  onKeyPress={(e) => {
                    if (e.key === 'Enter' || e.key === ' ') {
                      handleTaskClick(task)
                    }
                  }}
                >
                  <td>
                    <div className="task-title">{task.title}</div>
                    {task.description && (
                      <div className="task-description">{task.description}</div>
                    )}
                  </td>
                  <td>
                    <span className={`priority-badge ${getPriorityClass(task.priority)}`}>
                      {task.priority}
                    </span>
                  </td>
                  <td>{task.clientAddress}</td>
                  <td>
                    <span className={`status-badge ${getStatusClass(task.status)}`}>
                      {task.status}
                    </span>
                  </td>
                  <td>{(task.estimatedDuration / 60).toFixed(1)}</td>
                  <td className="task-date">{formatDate(task.createdAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {selectedTask && (
        <TaskDetail
          task={selectedTask}
          onClose={handleCloseDetail}
          onReassign={handleReassign}
        />
      )}
    </div>
  )
}

export default TaskList
