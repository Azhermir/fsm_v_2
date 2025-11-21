-- Add timestamps for task status transitions
-- startedAt: timestamp when task status changes to IN_PROGRESS
-- completedAt: timestamp when task status changes to COMPLETED
-- workSummary: summary of work completed (required for COMPLETED tasks)

ALTER TABLE service_tasks
    ADD COLUMN started_at TIMESTAMP,
    ADD COLUMN completed_at TIMESTAMP,
    ADD COLUMN work_summary TEXT;

-- Create index on started_at for reporting queries
CREATE INDEX idx_service_tasks_started_at ON service_tasks(started_at);

-- Create index on completed_at for reporting queries
CREATE INDEX idx_service_tasks_completed_at ON service_tasks(completed_at);
