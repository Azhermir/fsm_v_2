-- Add createdBy field to service_tasks table
-- Supports authentication requirement for task creation (Issue #107)

ALTER TABLE service_tasks
ADD COLUMN created_by BIGINT;

-- Update existing records to have a default createdBy value (system user ID 0)
UPDATE service_tasks SET created_by = 0 WHERE created_by IS NULL;

-- Make created_by NOT NULL after setting default values
ALTER TABLE service_tasks
ALTER COLUMN created_by SET NOT NULL;

-- Add comment for documentation
COMMENT ON COLUMN service_tasks.created_by IS 'ID of the user who created this task. Required by domain invariant - only authenticated dispatchers can create tasks.';
