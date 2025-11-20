import { useState } from 'react';
import './TaskCreationForm.css';

const TaskCreationForm = () => {
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    clientAddress: '',
    priority: 'MEDIUM',
    estimatedDuration: '',
  });

  const [errors, setErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');

  const validateField = (name, value) => {
    switch (name) {
      case 'title':
        return value.trim() === '' ? 'Title is required' : '';
      case 'description':
        return value.trim() === '' ? 'Description is required' : '';
      case 'clientAddress':
        return value.trim() === '' ? 'Client address is required' : '';
      case 'priority':
        return !['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'].includes(value)
          ? 'Valid priority is required'
          : '';
      case 'estimatedDuration':
        if (value === '') {
          return 'Estimated duration is required';
        }
        const duration = parseInt(value, 10);
        return isNaN(duration) || duration <= 0
          ? 'Estimated duration must be a positive number'
          : '';
      default:
        return '';
    }
  };

  const validateForm = () => {
    const newErrors = {};
    Object.keys(formData).forEach((key) => {
      const error = validateField(key, formData[key]);
      if (error) {
        newErrors[key] = error;
      }
    });
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const isFormValid = () => {
    return (
      formData.title.trim() !== '' &&
      formData.description.trim() !== '' &&
      formData.clientAddress.trim() !== '' &&
      ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'].includes(formData.priority) &&
      formData.estimatedDuration !== '' &&
      parseInt(formData.estimatedDuration, 10) > 0
    );
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));

    // Clear error for this field when user starts typing
    if (errors[name]) {
      setErrors((prev) => ({
        ...prev,
        [name]: '',
      }));
    }

    // Clear success message when user modifies form
    if (successMessage) {
      setSuccessMessage('');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    setIsSubmitting(true);

    try {
      const response = await fetch('http://localhost:8080/api/tasks', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          ...formData,
          estimatedDuration: parseInt(formData.estimatedDuration, 10),
        }),
      });

      if (response.ok) {
        setSuccessMessage('Task created successfully!');
        // Clear form after successful submission
        setFormData({
          title: '',
          description: '',
          clientAddress: '',
          priority: 'MEDIUM',
          estimatedDuration: '',
        });
        setErrors({});
      } else {
        console.error('Failed to create task');
      }
    } catch (error) {
      console.error('Error creating task:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="task-creation-form-container">
      <h2>Create New Service Task</h2>
      {successMessage && (
        <div className="success-message" role="alert">
          {successMessage}
        </div>
      )}
      <form onSubmit={handleSubmit} className="task-creation-form">
        <div className="form-group">
          <label htmlFor="title">Title</label>
          <input
            type="text"
            id="title"
            name="title"
            value={formData.title}
            onChange={handleChange}
            className={`form-input ${errors.title ? 'input-error' : ''}`}
            aria-invalid={!!errors.title}
            aria-describedby={errors.title ? 'title-error' : undefined}
          />
          {errors.title && (
            <span className="error-message" id="title-error" role="alert">
              {errors.title}
            </span>
          )}
        </div>

        <div className="form-group">
          <label htmlFor="description">Description</label>
          <textarea
            id="description"
            name="description"
            value={formData.description}
            onChange={handleChange}
            className={`form-textarea ${errors.description ? 'input-error' : ''}`}
            rows="4"
            aria-invalid={!!errors.description}
            aria-describedby={errors.description ? 'description-error' : undefined}
          />
          {errors.description && (
            <span className="error-message" id="description-error" role="alert">
              {errors.description}
            </span>
          )}
        </div>

        <div className="form-group">
          <label htmlFor="clientAddress">Client Address</label>
          <input
            type="text"
            id="clientAddress"
            name="clientAddress"
            value={formData.clientAddress}
            onChange={handleChange}
            className={`form-input ${errors.clientAddress ? 'input-error' : ''}`}
            aria-invalid={!!errors.clientAddress}
            aria-describedby={errors.clientAddress ? 'clientAddress-error' : undefined}
          />
          {errors.clientAddress && (
            <span className="error-message" id="clientAddress-error" role="alert">
              {errors.clientAddress}
            </span>
          )}
        </div>

        <div className="form-group">
          <label htmlFor="priority">Priority</label>
          <select
            id="priority"
            name="priority"
            value={formData.priority}
            onChange={handleChange}
            className={`form-select ${errors.priority ? 'input-error' : ''}`}
            aria-invalid={!!errors.priority}
            aria-describedby={errors.priority ? 'priority-error' : undefined}
          >
            <option value="LOW">Low</option>
            <option value="MEDIUM">Medium</option>
            <option value="HIGH">High</option>
            <option value="CRITICAL">Critical</option>
          </select>
          {errors.priority && (
            <span className="error-message" id="priority-error" role="alert">
              {errors.priority}
            </span>
          )}
        </div>

        <div className="form-group">
          <label htmlFor="estimatedDuration">Estimated Duration (minutes)</label>
          <input
            type="number"
            id="estimatedDuration"
            name="estimatedDuration"
            value={formData.estimatedDuration}
            onChange={handleChange}
            className={`form-input ${errors.estimatedDuration ? 'input-error' : ''}`}
            aria-invalid={!!errors.estimatedDuration}
            aria-describedby={errors.estimatedDuration ? 'estimatedDuration-error' : undefined}
          />
          {errors.estimatedDuration && (
            <span className="error-message" id="estimatedDuration-error" role="alert">
              {errors.estimatedDuration}
            </span>
          )}
        </div>

        <button
          type="submit"
          className="submit-button"
          disabled={!isFormValid() || isSubmitting}
        >
          {isSubmitting ? 'Creating...' : 'Create Task'}
        </button>
      </form>
    </div>
  );
};

export default TaskCreationForm;
