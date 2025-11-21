import { useState } from 'react'
import './CreateTaskForm.css'

const API_BASE_URL = 'http://localhost:8080'

const CreateTaskForm = () => {
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    clientAddress: '',
    priority: 'MEDIUM',
    estimatedDuration: '',
  })
  const [successMessage, setSuccessMessage] = useState('')
  const [errorMessage, setErrorMessage] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }))
    // Clear messages when user starts typing
    setSuccessMessage('')
    setErrorMessage('')
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setIsSubmitting(true)
    setSuccessMessage('')
    setErrorMessage('')

    try {
      const response = await fetch(`${API_BASE_URL}/api/tasks`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          ...formData,
          estimatedDuration: parseInt(formData.estimatedDuration, 10),
        }),
      })

      if (!response.ok) {
        const error = await response.json()
        throw new Error(error.message || 'Failed to create task')
      }

      const result = await response.json()
      setSuccessMessage(`Task "${result.title}" created successfully!`)
      
      // Reset form
      setFormData({
        title: '',
        description: '',
        clientAddress: '',
        priority: 'MEDIUM',
        estimatedDuration: '',
      })
    } catch (error) {
      setErrorMessage(error.message || 'An error occurred while creating the task')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="create-task-form">
      <h2>Create New Service Task</h2>
      
      {successMessage && (
        <div className="message success" role="alert">
          {successMessage}
        </div>
      )}
      
      {errorMessage && (
        <div className="message error" role="alert">
          {errorMessage}
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="title">
            Title <span className="required">*</span>
          </label>
          <input
            type="text"
            id="title"
            name="title"
            value={formData.title}
            onChange={handleChange}
            required
            placeholder="Enter task title"
          />
        </div>

        <div className="form-group">
          <label htmlFor="description">Description</label>
          <textarea
            id="description"
            name="description"
            value={formData.description}
            onChange={handleChange}
            rows="4"
            placeholder="Enter task description"
          />
        </div>

        <div className="form-group">
          <label htmlFor="clientAddress">
            Client Address <span className="required">*</span>
          </label>
          <input
            type="text"
            id="clientAddress"
            name="clientAddress"
            value={formData.clientAddress}
            onChange={handleChange}
            required
            placeholder="Enter client address"
          />
        </div>

        <div className="form-group">
          <label htmlFor="priority">
            Priority <span className="required">*</span>
          </label>
          <select
            id="priority"
            name="priority"
            value={formData.priority}
            onChange={handleChange}
            required
          >
            <option value="LOW">Low</option>
            <option value="MEDIUM">Medium</option>
            <option value="HIGH">High</option>
            <option value="CRITICAL">Critical</option>
          </select>
        </div>

        <div className="form-group">
          <label htmlFor="estimatedDuration">
            Estimated Duration (hours) <span className="required">*</span>
          </label>
          <input
            type="number"
            id="estimatedDuration"
            name="estimatedDuration"
            value={formData.estimatedDuration}
            onChange={handleChange}
            required
            min="1"
            step="0.5"
            placeholder="Enter estimated duration"
          />
        </div>

        <button
          type="submit"
          className="submit-button"
          disabled={isSubmitting}
        >
          {isSubmitting ? 'Creating...' : 'Create Task'}
        </button>
      </form>
    </div>
  )
}

export default CreateTaskForm
