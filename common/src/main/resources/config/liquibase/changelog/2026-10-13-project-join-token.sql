--liquibase formatted sql
--changeset cytomine:2026-10-13-project-join-token
ALTER TABLE project ADD COLUMN join_token VARCHAR(64);

-- A join token is a per-project grant: at most one project may own a given token.
-- Postgres treats NULLs as distinct, so projects without a token coexist freely.
CREATE UNIQUE INDEX idx_project_join_token ON project (join_token);