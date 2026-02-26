--liquibase formatted sql

--changeset luca:hibernate6-per-table-sequences
-- Migration to Hibernate 6 per-table sequences
-- Each entity now uses its own sequence instead of the shared hibernate_sequence
-- The hibernate_sequence is kept for MongoDB document ID generation via SequenceService

-- Create sequences for all JPA entities and initialize them to max(id) + 1
-- Using DO block to handle tables that may be empty

DO $$
DECLARE
    seq_start BIGINT;
BEGIN
    -- ontology domain
    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM term;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS term_seq START WITH %s INCREMENT BY 1', seq_start);

    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM ontology;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS ontology_seq START WITH %s INCREMENT BY 1', seq_start);

    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM relation;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS relation_seq START WITH %s INCREMENT BY 1', seq_start);

    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM relation_term;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS relation_term_seq START WITH %s INCREMENT BY 1', seq_start);

    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM track;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS track_seq START WITH %s INCREMENT BY 1', seq_start);

    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM user_annotation;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS user_annotation_seq START WITH %s INCREMENT BY 1', seq_start);

    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM reviewed_annotation;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS reviewed_annotation_seq START WITH %s INCREMENT BY 1', seq_start);

    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM annotation_index;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS annotation_index_seq START WITH %s INCREMENT BY 1', seq_start);

    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM annotation_term;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS annotation_term_seq START WITH %s INCREMENT BY 1', seq_start);

    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM annotation_track;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS annotation_track_seq START WITH %s INCREMENT BY 1', seq_start);

    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM annotation_link;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS annotation_link_seq START WITH %s INCREMENT BY 1', seq_start);

    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM annotation_group;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS annotation_group_seq START WITH %s INCREMENT BY 1', seq_start);

    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM shared_annotation;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS shared_annotation_seq START WITH %s INCREMENT BY 1', seq_start);

    -- annotation domain (new)
    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM annotation;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS annotation_seq START WITH %s INCREMENT BY 1', seq_start);

    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM annotation_layer;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS annotation_layer_seq START WITH %s INCREMENT BY 1', seq_start);

    -- project domain
    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM project;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS project_seq START WITH %s INCREMENT BY 1', seq_start);

    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM project_default_layer;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS project_default_layer_seq START WITH %s INCREMENT BY 1', seq_start);

    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM project_representative_user;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS project_representative_user_seq START WITH %s INCREMENT BY 1', seq_start);

    -- image domain
    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM abstract_image;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS abstract_image_seq START WITH %s INCREMENT BY 1', seq_start);

    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM abstract_slice;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS abstract_slice_seq START WITH %s INCREMENT BY 1', seq_start);

    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM image_instance;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS image_instance_seq START WITH %s INCREMENT BY 1', seq_start);

    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM slice_instance;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS slice_instance_seq START WITH %s INCREMENT BY 1', seq_start);

    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM uploaded_file;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS uploaded_file_seq START WITH %s INCREMENT BY 1', seq_start);

    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM companion_file;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS companion_file_seq START WITH %s INCREMENT BY 1', seq_start);

    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM image_group;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS image_group_seq START WITH %s INCREMENT BY 1', seq_start);

    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM image_group_image_instance;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS image_group_image_instance_seq START WITH %s INCREMENT BY 1', seq_start);

    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM storage;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS storage_seq START WITH %s INCREMENT BY 1', seq_start);

    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM mime;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS mime_seq START WITH %s INCREMENT BY 1', seq_start);

    -- meta domain
    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM description;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS description_seq START WITH %s INCREMENT BY 1', seq_start);

    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM tag;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS tag_seq START WITH %s INCREMENT BY 1', seq_start);

    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM tag_domain_association;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS tag_domain_association_seq START WITH %s INCREMENT BY 1', seq_start);

    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM property;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS property_seq START WITH %s INCREMENT BY 1', seq_start);

    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM attached_file;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS attached_file_seq START WITH %s INCREMENT BY 1', seq_start);

    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM configuration;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS configuration_seq START WITH %s INCREMENT BY 1', seq_start);

    -- security domain
    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM sec_user;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS sec_user_seq START WITH %s INCREMENT BY 1', seq_start);

    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM sec_role;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS sec_role_seq START WITH %s INCREMENT BY 1', seq_start);

    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM sec_user_sec_role;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS sec_user_sec_role_seq START WITH %s INCREMENT BY 1', seq_start);

    -- ACL domain
    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM acl_class;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS acl_class_seq START WITH %s INCREMENT BY 1', seq_start);

    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM acl_sid;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS acl_sid_seq START WITH %s INCREMENT BY 1', seq_start);

    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM acl_object_identity;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS acl_object_identity_seq START WITH %s INCREMENT BY 1', seq_start);

    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM acl_entry;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS acl_entry_seq START WITH %s INCREMENT BY 1', seq_start);

    -- command domain
    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM command;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS command_seq START WITH %s INCREMENT BY 1', seq_start);

    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM command_history;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS command_history_seq START WITH %s INCREMENT BY 1', seq_start);

    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM undo_stack_item;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS undo_stack_item_seq START WITH %s INCREMENT BY 1', seq_start);

    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM redo_stack_item;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS redo_stack_item_seq START WITH %s INCREMENT BY 1', seq_start);

    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM transaction;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS transaction_seq START WITH %s INCREMENT BY 1', seq_start);

    -- processing domain
    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM image_filter;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS image_filter_seq START WITH %s INCREMENT BY 1', seq_start);

    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM image_filter_project;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS image_filter_project_seq START WITH %s INCREMENT BY 1', seq_start);

    -- appengine domain
    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM task_run;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS task_run_seq START WITH %s INCREMENT BY 1', seq_start);

    SELECT COALESCE(MAX(id), 0) + 1 INTO seq_start FROM task_run_layer;
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS task_run_layer_seq START WITH %s INCREMENT BY 1', seq_start);

END $$;

--changeset luca:update-triggers-for-annotation-index-seq
-- Update trigger functions to use annotation_index_seq instead of hibernate_sequence

CREATE OR REPLACE FUNCTION afterInsertUserAnnotation() RETURNS TRIGGER AS $incUserAnnAfter$
DECLARE
    alreadyExist INTEGER;
BEGIN
    UPDATE image_instance
    SET count_image_annotations = count_image_annotations + 1
    WHERE image_instance.id = NEW.image_id;

    UPDATE project
    SET count_annotations = count_annotations + 1
    WHERE project.id = NEW.project_id;

    SELECT count(*) INTO alreadyExist FROM annotation_index WHERE user_id = NEW.user_id AND slice_id = NEW.slice_id;
    IF (alreadyExist=0) THEN
        INSERT INTO annotation_index(user_id, slice_id, count_annotation, count_reviewed_annotation, version, id)
        VALUES(NEW.user_id, NEW.slice_id, 0, 0, 0, nextval('annotation_index_seq'));
    END IF;
    UPDATE annotation_index SET count_annotation = count_annotation+1, version = version+1 WHERE user_id = NEW.user_id AND slice_id = NEW.slice_id;
    RETURN NEW;
END;
$incUserAnnAfter$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION afterUpdateUserAnnotation() RETURNS TRIGGER AS $incUserAnnAfter$
DECLARE
    alreadyExist INTEGER;
    current_ai_id annotation_index.id%TYPE;
    current_project_id image_instance.id%TYPE;
    current_image_id project.id%TYPE;
BEGIN
    IF(NEW.user_id<>OLD.user_id) THEN
        SELECT count(*) INTO alreadyExist FROM annotation_index WHERE user_id = NEW.user_id AND slice_id = NEW.slice_id;
        IF (alreadyExist=0) THEN
            INSERT INTO annotation_index(user_id, slice_id, count_annotation, count_reviewed_annotation, version, id)
            VALUES(NEW.user_id, NEW.slice_id, 0, 0, 0, nextval('annotation_index_seq'));
        END IF;
        UPDATE annotation_index SET count_annotation = count_annotation+1, version = version+1 WHERE user_id = NEW.user_id AND slice_id = NEW.slice_id;
        UPDATE annotation_index SET count_annotation = count_annotation-1, version = version+1 WHERE user_id = OLD.user_id AND slice_id = OLD.slice_id;
    END IF;
    IF NEW.deleted IS NULL AND OLD.deleted IS NOT NULL THEN
        UPDATE project
        SET count_annotations = count_annotations + 1
        WHERE project.id = OLD.project_id;

        UPDATE image_instance
        SET count_image_annotations = count_image_annotations + 1
        WHERE image_instance.id = OLD.image_id;

        UPDATE annotation_index SET count_annotation = count_annotation+1, version = version+1 WHERE user_id = OLD.user_id AND slice_id = OLD.slice_id;
    ELSEIF NEW.deleted IS NOT NULL AND OLD.deleted IS NULL THEN
        UPDATE project
        SET count_annotations = count_annotations - 1
        WHERE project.id = OLD.project_id;

        UPDATE image_instance
        SET count_image_annotations = count_image_annotations - 1
        WHERE image_instance.id = OLD.image_id;

        UPDATE annotation_index SET count_annotation = count_annotation-1, version = version+1 WHERE user_id = OLD.user_id AND slice_id = OLD.slice_id;
    END IF;
    RETURN NEW;
END;
$incUserAnnAfter$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION afterInsertAlgoAnnotation() RETURNS TRIGGER AS $incAlgoAnnAfter$
DECLARE
    alreadyExist INTEGER;
BEGIN
    UPDATE image_instance
    SET count_image_job_annotations = count_image_job_annotations + 1
    WHERE image_instance.id = NEW.image_id;

    UPDATE project
    SET count_job_annotations = count_job_annotations + 1
    WHERE project.id = NEW.project_id;

    SELECT count(*) INTO alreadyExist FROM annotation_index WHERE user_id = NEW.user_id AND slice_id = NEW.slice_id;
    IF (alreadyExist=0) THEN
        INSERT INTO annotation_index(user_id, slice_id, count_annotation, count_reviewed_annotation, version, id)
        VALUES(NEW.user_id, NEW.slice_id, 0, 0, 0, nextval('annotation_index_seq'));
    END IF;
    UPDATE annotation_index SET count_annotation = count_annotation+1, version = version+1 WHERE user_id = NEW.user_id AND slice_id = NEW.slice_id;
    RETURN NEW;
END;
$incAlgoAnnAfter$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION incrementAnnotationReviewedAnnotation() RETURNS trigger as $incAnnRevAnn$
DECLARE
    current_class reviewed_annotation.parent_class_name%TYPE;
    algo_class reviewed_annotation.parent_class_name%TYPE := 'be.cytomine.domain.ontology.AlgoAnnotation';
    user_class reviewed_annotation.parent_class_name%TYPE := 'be.cytomine.domain.ontology.UserAnnotation';
    alreadyExist INTEGER;
    current_id annotation_index.id%TYPE;
BEGIN
    UPDATE image_instance
    SET count_image_reviewed_annotations = count_image_reviewed_annotations + 1
    WHERE image_instance.id = NEW.image_id;

    UPDATE project
    SET count_reviewed_annotations = count_reviewed_annotations + 1
    WHERE project.id = NEW.project_id;

    SELECT count(*) INTO alreadyExist FROM annotation_index WHERE user_id = NEW.review_user_id AND slice_id = NEW.slice_id;
    IF (alreadyExist=0) THEN
        INSERT INTO annotation_index(user_id, slice_id, count_annotation, count_reviewed_annotation, version, id)
        VALUES(NEW.review_user_id, NEW.slice_id, 0, 0, 0, nextval('annotation_index_seq'));
    END IF;
    UPDATE annotation_index SET count_reviewed_annotation = count_reviewed_annotation+1, version = version+1 WHERE user_id = NEW.review_user_id AND slice_id = NEW.slice_id;

    SELECT parent_class_name INTO current_class from reviewed_annotation where id = NEW.id;
    IF current_class = user_class THEN
        UPDATE user_annotation
        SET count_reviewed_annotations = count_reviewed_annotations + 1
        WHERE user_annotation.id = NEW.parent_ident;
    ELSEIF current_class = algo_class THEN
        UPDATE algo_annotation
        SET count_reviewed_annotations = count_reviewed_annotations + 1
        WHERE algo_annotation.id = NEW.parent_ident;
    END IF;
    RETURN NEW;
END;
$incAnnRevAnn$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION updateAnnotationReviewedAnnotation() RETURNS trigger as $incAnnRevAnn$
DECLARE
    current_class reviewed_annotation.parent_class_name%TYPE;
    algo_class reviewed_annotation.parent_class_name%TYPE := 'be.cytomine.domain.ontology.AlgoAnnotation';
    user_class reviewed_annotation.parent_class_name%TYPE := 'be.cytomine.domain.ontology.UserAnnotation';
    alreadyExist INTEGER;
    current_id annotation_index.id%TYPE;
BEGIN
    IF NEW.deleted IS NULL AND OLD.deleted IS NOT NULL THEN
        UPDATE image_instance
        SET count_image_reviewed_annotations = count_image_reviewed_annotations + 1
        WHERE image_instance.id = NEW.image_id;

        UPDATE project
        SET count_reviewed_annotations = count_reviewed_annotations + 1
        WHERE project.id = NEW.project_id;

        SELECT count(*) INTO alreadyExist FROM annotation_index WHERE user_id = NEW.review_user_id AND slice_id = NEW.slice_id;
        IF (alreadyExist=0) THEN
            INSERT INTO annotation_index(user_id, slice_id, count_annotation, count_reviewed_annotation, version, id)
            VALUES(NEW.review_user_id, NEW.slice_id, 0, 0, 0, nextval('annotation_index_seq'));
        END IF;
        UPDATE annotation_index SET count_reviewed_annotation = count_reviewed_annotation+1, version = version+1 WHERE user_id = NEW.review_user_id AND slice_id = NEW.slice_id;

        SELECT parent_class_name INTO current_class from reviewed_annotation where id = NEW.id;
        IF current_class = user_class THEN
            UPDATE user_annotation
            SET count_reviewed_annotations = count_reviewed_annotations + 1
            WHERE user_annotation.id = NEW.parent_ident;
        ELSEIF current_class = algo_class THEN
            UPDATE algo_annotation
            SET count_reviewed_annotations = count_reviewed_annotations + 1
            WHERE algo_annotation.id = NEW.parent_ident;
        END IF;

    ELSEIF NEW.deleted IS NOT NULL AND OLD.deleted IS NULL THEN
        UPDATE image_instance
        SET count_image_reviewed_annotations = count_image_reviewed_annotations - 1
        WHERE image_instance.id = NEW.image_id;

        UPDATE project
        SET count_reviewed_annotations = count_reviewed_annotations - 1
        WHERE project.id = NEW.project_id;

        SELECT count(*) INTO alreadyExist FROM annotation_index WHERE user_id = NEW.review_user_id AND slice_id = NEW.slice_id;
        IF (alreadyExist=0) THEN
            INSERT INTO annotation_index(user_id, slice_id, count_annotation, count_reviewed_annotation, version, id)
            VALUES(NEW.review_user_id, NEW.slice_id, 0, 0, 0, nextval('annotation_index_seq'));
        END IF;
        UPDATE annotation_index SET count_reviewed_annotation = count_reviewed_annotation-1, version = version+1 WHERE user_id = NEW.review_user_id AND slice_id = NEW.slice_id;

        SELECT parent_class_name INTO current_class from reviewed_annotation where id = NEW.id;
        IF current_class = user_class THEN
            UPDATE user_annotation
            SET count_reviewed_annotations = count_reviewed_annotations - 1
            WHERE user_annotation.id = NEW.parent_ident;
        ELSEIF current_class = algo_class THEN
            UPDATE algo_annotation
            SET count_reviewed_annotations = count_reviewed_annotations - 1
            WHERE algo_annotation.id = NEW.parent_ident;
        END IF;
    END IF;

    RETURN NEW;
END;
$incAnnRevAnn$ LANGUAGE plpgsql;
