--liquibase formatted sql
--changeset cytomine:2026-07-14-remove-ontology-name-constraint
ALTER TABLE ontology DROP CONSTRAINT IF EXISTS uk_pber6ij6n37dx73gt6pnpu1yc;
