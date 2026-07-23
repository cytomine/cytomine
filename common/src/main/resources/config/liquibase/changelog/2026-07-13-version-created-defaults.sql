--liquibase formatted sql
--changeset cytomine:2026-07-13-version-defaults
ALTER TABLE term ALTER COLUMN version SET DEFAULT 0;
ALTER TABLE term ALTER COLUMN created SET DEFAULT now();
ALTER TABLE relation_term ALTER COLUMN version SET DEFAULT 0;
ALTER TABLE relation_term ALTER COLUMN created SET DEFAULT now();
ALTER TABLE relation ALTER COLUMN version SET DEFAULT 0;
ALTER TABLE relation ALTER COLUMN created SET DEFAULT now();
ALTER TABLE ontology ALTER COLUMN version SET DEFAULT 0;
ALTER TABLE ontology ALTER COLUMN created SET DEFAULT now();
ALTER TABLE storage ALTER COLUMN version SET DEFAULT 0;
ALTER TABLE storage ALTER COLUMN created SET DEFAULT now();
ALTER TABLE uploaded_file ALTER COLUMN version SET DEFAULT 0;
ALTER TABLE uploaded_file ALTER COLUMN created SET DEFAULT now();
ALTER TABLE tag_domain_association ALTER COLUMN version SET DEFAULT 0;
ALTER TABLE tag_domain_association ALTER COLUMN created SET DEFAULT now();
ALTER TABLE sec_role ALTER COLUMN version SET DEFAULT 0;
ALTER TABLE sec_role ALTER COLUMN created SET DEFAULT now();
ALTER TABLE sec_user_sec_role ALTER COLUMN version SET DEFAULT 0;
ALTER TABLE sec_user_sec_role ALTER COLUMN created SET DEFAULT now();
ALTER TABLE sec_user ALTER COLUMN version SET DEFAULT 0;
ALTER TABLE sec_user ALTER COLUMN created SET DEFAULT now();
