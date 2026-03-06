--liquibase formatted sql

--changeset bathienle:add-roi-to-task-run-layer
ALTER TABLE task_run_layer
    ADD COLUMN roi_id BIGINT REFERENCES user_annotation(id);

ALTER TABLE task_run_layer
    ALTER COLUMN x_offset SET DEFAULT 0,
    ALTER COLUMN x_offset SET NOT NULL,
    ALTER COLUMN y_offset SET DEFAULT 0,
    ALTER COLUMN y_offset SET NOT NULL;
