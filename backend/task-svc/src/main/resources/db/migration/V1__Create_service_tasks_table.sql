-- Create service_tasks table
CREATE TABLE service_tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    client_address VARCHAR(255) NOT NULL,
    priority VARCHAR(20) NOT NULL CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    estimated_duration INTEGER NOT NULL CHECK (estimated_duration > 0),
    status VARCHAR(20) NOT NULL CHECK (status IN ('UNASSIGNED', 'ASSIGNED', 'IN_PROGRESS', 'COMPLETED')),
    created_at TIMESTAMP NOT NULL
);

-- Create index on status for faster queries
CREATE INDEX idx_service_tasks_status ON service_tasks(status);

-- Create index on priority for faster queries
CREATE INDEX idx_service_tasks_priority ON service_tasks(priority);

-- Create index on created_at for faster time-based queries
CREATE INDEX idx_service_tasks_created_at ON service_tasks(created_at);
