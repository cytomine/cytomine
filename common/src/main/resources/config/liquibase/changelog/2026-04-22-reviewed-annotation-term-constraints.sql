--liquibase formatted sql

--changeset cytomine:2026-04-22-reviewed-annotation-term-dedup
DELETE FROM reviewed_annotation_term
WHERE ctid NOT IN (
    SELECT MIN(ctid)
    FROM reviewed_annotation_term
    GROUP BY reviewed_annotation_terms_id, term_id
);

--changeset cytomine:2026-04-22-reviewed-annotation-term-pk
ALTER TABLE reviewed_annotation_term
    ADD CONSTRAINT reviewed_annotation_term_pkey
        PRIMARY KEY (reviewed_annotation_terms_id, term_id);
