-- liquibase formatted sql

-- changeset bathienle:add-image-slice-to-annotations
ALTER TABLE annotation_layer
ADD COLUMN image_id BIGINT NOT NULL,
ADD CONSTRAINT fk_annotation_layer_image_instance FOREIGN KEY (image_id) REFERENCES image_instance(id);

ALTER TABLE annotation
ADD COLUMN slice_id BIGINT NOT NULL,
ADD CONSTRAINT fk_annotation_slice_instance FOREIGN KEY (slice_id) REFERENCES slice_instance(id);
