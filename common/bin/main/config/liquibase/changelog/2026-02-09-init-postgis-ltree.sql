--liquibase formatted sql

--changeset bathienle:create-ltree-extension-for-postgis
CREATE EXTENSION IF NOT EXISTS ltree;
