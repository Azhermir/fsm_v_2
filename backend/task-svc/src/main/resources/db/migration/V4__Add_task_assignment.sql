-- Add assigned_to column to service_tasks table
ALTER TABLE service_tasks
ADD COLUMN assigned_to BIGINT;

-- Create task_assignments table
CREATE TABLE task_assignments (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    technician_id BIGINT NOT NULL,
    assigned_at TIMESTAMP NOT NULL,
    assigned_by VARCHAR(255) NOT NULL,
    CONSTRAINT fk_task_assignments_task FOREIGN KEY (task_id) REFERENCES service_tasks(id)
);

-- Create index for faster lookups
CREATE INDEX idx_task_assignments_task_id ON task_assignments(task_id);
CREATE INDEX idx_task_assignments_technician_id ON task_assignments(technician_id);
