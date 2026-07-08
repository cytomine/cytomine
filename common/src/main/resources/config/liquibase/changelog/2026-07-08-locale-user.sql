--liquibase formatted sql
--changeset cytomine:2026-07-08-locale-user
ALTER TABLE sec_user ADD COLUMN locale TEXT NULL;

