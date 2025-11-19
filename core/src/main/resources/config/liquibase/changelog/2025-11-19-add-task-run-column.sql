--liquibase formatted sql

--changeset bathien:add-task-id-column-to-task-run
-- Add task name and version
ALTER TABLE task_run ADD COLUMN task_name VARCHAR(255);
ALTER TABLE task_run ADD COLUMN task_version VARCHAR(255);

-- Update existing rows with default values
UPDATE task_run SET task_name = 'unknown' WHERE task_name IS NULL;
UPDATE task_run SET task_version = '1.0.0' WHERE task_version IS NULL;

-- Add NOT NULL constraints
ALTER TABLE task_run ALTER COLUMN task_name SET NOT NULL;
ALTER TABLE task_run ALTER COLUMN task_version SET NOT NULL;
