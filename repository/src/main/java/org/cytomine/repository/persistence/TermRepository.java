package org.cytomine.repository.persistence;

import java.time.LocalDateTime;

import org.cytomine.repository.persistence.entity.TermEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TermRepository extends JpaRepository<TermEntity, Long> {

    @Query(value = "SELECT t.* FROM term t JOIN project p ON t.ontology_id = p.ontology_id" +
        " WHERE p.id = :id and t.deleted is null", countQuery =
        "SELECT count(*) FROM term t JOIN project p ON t.ontology_id = p.ontology_id" +
            " WHERE p.id = :id and t.deleted = :deleted", nativeQuery = true)
    Page<TermEntity> findAllByProjectId(long id, Pageable pageable);

    Page<TermEntity> findAllByOntologyIdAndDeletedNull(long id, Pageable pageable);


    @Query(value = """
        SELECT t.id AS id, t.name AS key, count(DISTINCT ua.image_id) AS value, t.color AS color
        FROM user_annotation ua
        LEFT JOIN annotation_term at ON at.user_annotation_id = ua.id
        LEFT JOIN term t ON t.id = at.term_id
        WHERE ua.project_id = :projectId
        AND (:startDate IS NULL OR at.created > :startDate)
        AND (:endDate IS NULL OR at.created < :endDate)
        GROUP BY at.term_id, t.id, t.name, t.color
        """,
        countQuery = """
        SELECT count(*) FROM (
            SELECT at.term_id
            FROM user_annotation ua
            LEFT JOIN annotation_term at ON at.user_annotation_id = ua.id
            WHERE ua.project_id = :projectId
            AND (:startDate IS NULL OR at.created > :startDate)
            AND (:endDate IS NULL OR at.created < :endDate)
            GROUP BY at.term_id
        ) AS _count
        """,
        nativeQuery = true)
    Page<StatTermProjection> findAllByProjectForStats(long projectId, LocalDateTime startDate, LocalDateTime endDate,
                                                      Pageable pageable);

}
