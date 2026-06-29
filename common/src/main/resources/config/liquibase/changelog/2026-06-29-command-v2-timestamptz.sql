--liquibase formatted sql

--changeset cytomine:command-v2-migrate-local-date-time-to-instant
UPDATE command_v2
SET data = regexp_replace(
    data::text,
    '"(\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(\.\d+)?)"',
    '"\1Z"',
    'g'
)::jsonb
WHERE data::text ~ '\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?!\d|Z)';

--changeset cytomine:command-v2-timestamp-to-timestamptz
ALTER TABLE command_v2 ALTER COLUMN created TYPE TIMESTAMP WITH TIME ZONE USING created AT TIME ZONE 'UTC';
ALTER TABLE command_v2 ALTER COLUMN updated TYPE TIMESTAMP WITH TIME ZONE USING updated AT TIME ZONE 'UTC';
