--liquibase formatted sql
--changeset cytomine:2026-07-20-seed-roles
INSERT INTO sec_role (authority) SELECT 'ROLE_USER' WHERE NOT EXISTS (SELECT 1 FROM sec_role WHERE authority = 'ROLE_USER');
INSERT INTO sec_role (authority) SELECT 'ROLE_ADMIN' WHERE NOT EXISTS (SELECT 1 FROM sec_role WHERE authority = 'ROLE_ADMIN');
INSERT INTO sec_role (authority) SELECT 'ROLE_SUPER_ADMIN' WHERE NOT EXISTS (SELECT 1 FROM sec_role WHERE authority = 'ROLE_SUPER_ADMIN');
INSERT INTO sec_role (authority) SELECT 'ROLE_GUEST' WHERE NOT EXISTS (SELECT 1 FROM sec_role WHERE authority = 'ROLE_GUEST');
