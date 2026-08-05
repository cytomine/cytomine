--liquibase formatted sql
--changeset cytomine:2026-08-05-annotation-index-fk-cascade
ALTER TABLE annotation_index DROP CONSTRAINT IF EXISTS fk_q00ycisk5ula612n81sfx3ud2;
ALTER TABLE annotation_index ADD CONSTRAINT fk_q00ycisk5ula612n81sfx3ud2
    FOREIGN KEY (slice_id) REFERENCES slice_instance (id) ON DELETE CASCADE;
ALTER TABLE annotation_index DROP CONSTRAINT IF EXISTS fk_kba5x5ktp61txg1wpagqibsok;
ALTER TABLE annotation_index ADD CONSTRAINT fk_kba5x5ktp61txg1wpagqibsok
    FOREIGN KEY (user_id) REFERENCES sec_user (id) ON DELETE CASCADE;
