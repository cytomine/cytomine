--liquibase formatted sql
--changeset cytomine:2026-06-26-command-parent

ALTER TABLE command_v2 ADD COLUMN parent_command_id UUID NULL;
ALTER TABLE command_v2 ADD FOREIGN KEY (parent_command_id) REFERENCES command_v2(id);

