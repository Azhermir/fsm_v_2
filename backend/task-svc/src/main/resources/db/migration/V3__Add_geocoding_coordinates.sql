-- Add latitude and longitude columns for geocoding support
-- These fields store the geocoded coordinates from the client address

ALTER TABLE service_tasks ADD COLUMN latitude DOUBLE PRECISION;
ALTER TABLE service_tasks ADD COLUMN longitude DOUBLE PRECISION;

-- Create index on latitude/longitude for location-based queries
CREATE INDEX idx_service_tasks_location ON service_tasks(latitude, longitude);
