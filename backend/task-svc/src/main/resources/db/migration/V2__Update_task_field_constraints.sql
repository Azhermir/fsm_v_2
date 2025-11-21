-- Update field constraints for domain invariants
-- Title: max 200 characters
-- Description: required, max 2000 characters

-- Update title column length
ALTER TABLE service_tasks ALTER COLUMN title TYPE VARCHAR(200);

-- Update description column: make it NOT NULL and increase length
ALTER TABLE service_tasks ALTER COLUMN description TYPE VARCHAR(2000);
ALTER TABLE service_tasks ALTER COLUMN description SET NOT NULL;
