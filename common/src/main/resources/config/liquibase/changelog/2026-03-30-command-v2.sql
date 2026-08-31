--liquibase formatted sql

--changeset cytomine:create-command-v2-table
CREATE TABLE command_v2 (
    id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data    JSONB NOT NULL,
    user_id BIGINT NOT NULL REFERENCES sec_user(id)
);
