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

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
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
        console.log('Task created successfully');
      } else {
        console.error('Failed to create task');
      }
    } catch (error) {
      console.error('Error creating task:', error);
    }
  };

  return (
    <div className="task-creation-form-container">
      <h2>Create New Service Task</h2>
      <form onSubmit={handleSubmit} className="task-creation-form">
        <div className="form-group">
          <label htmlFor="title">Title</label>
          <input
            type="text"
            id="title"
            name="title"
            value={formData.title}
            onChange={handleChange}
            className="form-input"
          />
        </div>

        <div className="form-group">
          <label htmlFor="description">Description</label>
          <textarea
            id="description"
            name="description"
            value={formData.description}
            onChange={handleChange}
            className="form-textarea"
            rows="4"
          />
        </div>

        <div className="form-group">
          <label htmlFor="clientAddress">Client Address</label>
          <input
            type="text"
            id="clientAddress"
            name="clientAddress"
            value={formData.clientAddress}
            onChange={handleChange}
            className="form-input"
          />
        </div>

        <div className="form-group">
          <label htmlFor="priority">Priority</label>
          <select
            id="priority"
            name="priority"
            value={formData.priority}
            onChange={handleChange}
            className="form-select"
          >
            <option value="LOW">Low</option>
            <option value="MEDIUM">Medium</option>
            <option value="HIGH">High</option>
            <option value="CRITICAL">Critical</option>
          </select>
        </div>

        <div className="form-group">
          <label htmlFor="estimatedDuration">Estimated Duration (minutes)</label>
          <input
            type="number"
            id="estimatedDuration"
            name="estimatedDuration"
            value={formData.estimatedDuration}
            onChange={handleChange}
            className="form-input"
          />
        </div>

        <button type="submit" className="submit-button">
          Create Task
        </button>
      </form>
    </div>
  );
};

export default TaskCreationForm;
