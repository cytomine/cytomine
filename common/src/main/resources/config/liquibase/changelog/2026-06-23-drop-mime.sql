--liquibase formatted sql
--changeset cytomine:2026-06-23-drop-mime
ALTER TABLE abstract_slice DROP CONSTRAINT IF EXISTS fk_1v05hrtjnvce5jyomlmtit7sk;
ALTER TABLE abstract_slice DROP COLUMN IF EXISTS mime_id;
DROP TABLE IF EXISTS mime;
