--liquibase formatted sql
--changeset cytomine:2026-09-09-user-key-defaults
ALTER TABLE sec_user ALTER COLUMN public_key SET DEFAULT gen_random_uuid()::text;
ALTER TABLE sec_user ALTER COLUMN private_key SET DEFAULT gen_random_uuid()::text;
UPDATE sec_user SET public_key = gen_random_uuid()::text WHERE public_key IS NULL;
UPDATE sec_user SET private_key = gen_random_uuid()::text WHERE private_key IS NULL;
ALTER TABLE sec_user ALTER COLUMN public_key SET NOT NULL;
ALTER TABLE sec_user ALTER COLUMN private_key SET NOT NULL;
