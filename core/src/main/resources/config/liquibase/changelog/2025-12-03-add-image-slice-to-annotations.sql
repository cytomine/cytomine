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

-- changeset bathienle:add-task-run-output-geometry
-- Add task run output geometry table
CREATE TABLE task_run_output_geometry (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    task_run_id BIGINT NOT NULL,
    image_instance_id BIGINT NOT NULL,
    created TIMESTAMP,
    updated TIMESTAMP,
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_task_run_output_geometry_task_run FOREIGN KEY (task_run_id) REFERENCES task_run(id),
    CONSTRAINT fk_task_run_output_geometry_image_instance FOREIGN KEY (image_instance_id) REFERENCES image_instance(id)
);

--changeset bathienle:add-index-task-run-output-geometry
-- Create indexes for task_run_output_geometry
CREATE INDEX idx_task_run_output_geometry_task_run
    ON task_run_output_geometry(task_run_id);

CREATE INDEX idx_task_run_output_geometry_image_instance
    ON task_run_output_geometry(image_instance_id);
