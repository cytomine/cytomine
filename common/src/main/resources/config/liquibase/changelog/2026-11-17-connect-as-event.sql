--liquibase formatted sql
--changeset cytomine:2026-11-17-connect-as-event
-- Audit trail of "connect as" (admin impersonation) sessions. No FK on purpose:
-- rows must survive the deletion of the admin or the target user.
CREATE TABLE connect_as_event (
    id              BIGINT PRIMARY KEY,
    acting_admin_id BIGINT       NOT NULL,
    target_user_id  BIGINT       NOT NULL,
    target_username VARCHAR(255) NOT NULL,
    started_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at        TIMESTAMP,
    created         TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated         TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    version         INT          DEFAULT 0
);
CREATE INDEX idx_connect_as_event_acting_admin ON connect_as_event(acting_admin_id);
CREATE INDEX idx_connect_as_event_target_user ON connect_as_event(target_user_id);
