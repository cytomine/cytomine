-- liquibase formatted sql

-- changeset bathienle:add-image-slice-columns-nullable
ALTER TABLE annotation_layer
ADD COLUMN image_id BIGINT,
ADD CONSTRAINT fk_annotation_layer_image_instance FOREIGN KEY (image_id) REFERENCES image_instance(id);

ALTER TABLE annotation
ADD COLUMN slice_id BIGINT,
ADD CONSTRAINT fk_annotation_slice_instance FOREIGN KEY (slice_id) REFERENCES slice_instance(id);

-- changeset bathienle:populate-image-slice-data
-- Populate image_id for annotation_layer from task_run_layer
UPDATE annotation_layer al
SET image_id = (
    SELECT trl.image_instance_id
    FROM task_run_layer trl
    WHERE trl.layer_id = al.id
)
WHERE image_id IS NULL;

-- Populate slice_id for annotation
UPDATE annotation a
SET slice_id = (
    SELECT si.id
    FROM annotation_layer al
    JOIN slice_instance si ON si.image_id = al.image_id
    WHERE al.id = a.layer_id
    ORDER BY si.id
    LIMIT 1
)
WHERE slice_id IS NULL;

-- changeset bathienle:make-image-slice-columns-not-null
ALTER TABLE annotation_layer ALTER COLUMN image_id SET NOT NULL;
ALTER TABLE annotation ALTER COLUMN slice_id SET NOT NULL;

-- changeset bathienle:add-output-to-task-run-layer
ALTER TABLE task_run_layer
ADD COLUMN output_name VARCHAR(255);
