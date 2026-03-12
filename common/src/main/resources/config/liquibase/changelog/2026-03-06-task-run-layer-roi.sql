--liquibase formatted sql

--changeset bathienle:add-roi-to-task-run-layer
ALTER TABLE task_run_layer
    ADD COLUMN parameter_name VARCHAR(255),
    ADD COLUMN derived_from VARCHAR(255);

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
    created           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version           INT DEFAULT 0,
    CONSTRAINT fk_crop_offset_task_run_layer
        FOREIGN KEY (task_run_layer_id)
        REFERENCES task_run_layer(id)
);
