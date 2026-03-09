--liquibase formatted sql

--changeset bathienle:add-roi-to-task-run-layer
ALTER TABLE task_run_layer
    ADD COLUMN roi_id BIGINT REFERENCES user_annotation(id);

ALTER TABLE task_run_layer
    DROP COLUMN x_offset,
    DROP COLUMN y_offset;

-- changeset bathienle:add-crop-offset-table
CREATE TABLE crop_offset (
    id                BIGSERIAL PRIMARY KEY,
    x                 INT    NOT NULL,
    y                 INT    NOT NULL,
    task_run_layer_id BIGINT NOT NULL,
    order_index       INT    NOT NULL,
    CONSTRAINT fk_crop_offset_task_run_layer
        FOREIGN KEY (task_run_layer_id)
        REFERENCES task_run_layer(id)
);
