import { useState } from 'react'
import './CreateTaskForm.css'

const API_BASE_URL = 'http://localhost:8080'

// Validation constants per domain invariants
const VALIDATION_RULES = {
  title: {
    required: true,
    maxLength: 200,
  },
  description: {
    required: true,
    maxLength: 2000,
  },
  clientAddress: {
    required: true,
  },
  estimatedDuration: {
    required: true,
    min: 1,
  },
}

const CreateTaskForm = () => {
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    clientAddress: '',
    latitude: null,
    longitude: null,
    priority: 'MEDIUM',
    estimatedDuration: '',
  })
  const [validationErrors, setValidationErrors] = useState({})
  const [successMessage, setSuccessMessage] = useState('')
  const [errorMessage, setErrorMessage] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const validateField = (name, value) => {
    const rules = VALIDATION_RULES[name]
    if (!rules) return null

    // Required validation
    if (rules.required && (!value || value.trim() === '')) {
      return `${name.charAt(0).toUpperCase() + name.slice(1).replace(/([A-Z])/g, ' $1').trim()} is required`
    }

    // Max length validation
    if (rules.maxLength && value && value.length > rules.maxLength) {
      return `${name.charAt(0).toUpperCase() + name.slice(1).replace(/([A-Z])/g, ' $1').trim()} must not exceed ${rules.maxLength} characters`
    }

    // Min value validation
    if (rules.min !== undefined && value && Number(value) < rules.min) {
      return `${name.charAt(0).toUpperCase() + name.slice(1).replace(/([A-Z])/g, ' $1').trim()} must be at least ${rules.min}`
    }

    return null
  }

  const validateForm = () => {
    const errors = {}
    Object.keys(VALIDATION_RULES).forEach((field) => {
      const error = validateField(field, formData[field])
      if (error) {
        errors[field] = error
      }
    })
    return errors
  }

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }))

    // Validate the field on change to provide immediate feedback
    const error = validateField(name, value)
    if (error) {
      setValidationErrors((prev) => ({
        ...prev,
        [name]: error,
      }))
    } else if (validationErrors[name]) {
      // Clear validation error for this field when it becomes valid
      setValidationErrors((prev) => {
        const newErrors = { ...prev }
        delete newErrors[name]
        return newErrors
      })
    }

    // Clear messages when user starts typing
    setSuccessMessage('')
    setErrorMessage('')
  }

  const handleBlur = (e) => {
    const { name, value } = e.target
    const error = validateField(name, value)
    if (error) {
      setValidationErrors((prev) => ({
        ...prev,
        [name]: error,
      }))
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    
    // Validate all fields
    const errors = validateForm()
    if (Object.keys(errors).length > 0) {
      setValidationErrors(errors)
      setErrorMessage('Please fix the validation errors before submitting')
      return
    }

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
        // Handle validation errors from server
        if (error.validationErrors) {
          throw new Error(error.validationErrors.join('; '))
        } else {
          throw new Error(error.message || 'Failed to create task')
        }
      }

      const result = await response.json()
      setSuccessMessage(`Task "${result.title}" created successfully!`)
      
      // Reset form
      setFormData({
        title: '',
        description: '',
        clientAddress: '',
        latitude: null,
        longitude: null,
        priority: 'MEDIUM',
        estimatedDuration: '',
      })
      setValidationErrors({})
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

      <form onSubmit={handleSubmit} noValidate>
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
            onBlur={handleBlur}
            required
            maxLength="200"
            placeholder="Enter task title"
            className={validationErrors.title ? 'error' : ''}
            aria-invalid={validationErrors.title ? 'true' : 'false'}
            aria-describedby={validationErrors.title ? 'title-error' : undefined}
          />
          {validationErrors.title && (
            <span id="title-error" className="error-message" role="alert">
              {validationErrors.title}
            </span>
          )}
        </div>

        <div className="form-group">
          <label htmlFor="description">
            Description <span className="required">*</span>
          </label>
          <textarea
            id="description"
            name="description"
            value={formData.description}
            onChange={handleChange}
            onBlur={handleBlur}
            required
            maxLength="2000"
            rows="4"
            placeholder="Enter task description"
            className={validationErrors.description ? 'error' : ''}
            aria-invalid={validationErrors.description ? 'true' : 'false'}
            aria-describedby={validationErrors.description ? 'description-error' : undefined}
          />
          {validationErrors.description && (
            <span id="description-error" className="error-message" role="alert">
              {validationErrors.description}
            </span>
          )}
          <small className="char-count">
            {formData.description.length} / 2000 characters
          </small>
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
            onBlur={handleBlur}
            required
            placeholder="Enter client address"
            className={validationErrors.clientAddress ? 'error' : ''}
            aria-invalid={validationErrors.clientAddress ? 'true' : 'false'}
            aria-describedby={validationErrors.clientAddress ? 'clientAddress-error' : undefined}
          />
          {validationErrors.clientAddress && (
            <span id="clientAddress-error" className="error-message" role="alert">
              {validationErrors.clientAddress}
            </span>
          )}
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
            onBlur={handleBlur}
            required
            min="1"
            step="0.5"
            placeholder="Enter estimated duration"
            className={validationErrors.estimatedDuration ? 'error' : ''}
            aria-invalid={validationErrors.estimatedDuration ? 'true' : 'false'}
            aria-describedby={validationErrors.estimatedDuration ? 'estimatedDuration-error' : undefined}
          />
          {validationErrors.estimatedDuration && (
            <span id="estimatedDuration-error" className="error-message" role="alert">
              {validationErrors.estimatedDuration}
            </span>
          )}
        </div>

        <button
          type="submit"
          className="submit-button"
          disabled={isSubmitting || Object.keys(validationErrors).length > 0}
        >
          {isSubmitting ? 'Creating...' : 'Create Task'}
        </button>
      </form>
    </div>
  )
}

export default CreateTaskForm
