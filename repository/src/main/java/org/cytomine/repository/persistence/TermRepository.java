package org.cytomine.repository.persistence;

import java.time.LocalDateTime;

import org.cytomine.repository.persistence.entity.TermEntity;
import org.cytomine.repository.persistence.projection.StatPerTermAndImageProjection;
import org.cytomine.repository.persistence.projection.StatTermProjection;
import org.cytomine.repository.persistence.projection.StatUserTermProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TermRepository extends JpaRepository<TermEntity, Long> {

    @Query(value = "SELECT t.* FROM term t JOIN project p ON t.ontology_id = p.ontology_id"
        + " WHERE p.userId = :userId and t.deleted is null", countQuery =
        "SELECT count(*) FROM term t JOIN project p ON t.ontology_id = p.ontology_id"
            + " WHERE p.userId = :userId and t.deleted = :deleted", nativeQuery = true)
    Page<TermEntity> findAllByProjectId(long id, Pageable pageable);

    Page<TermEntity> findAllByOntologyIdAndDeletedNull(long id, Pageable pageable);



    @Query(value = """
        SELECT t.userId AS userId, t.name AS key, count(DISTINCT ua.image_id) AS value, t.color AS color
        FROM user_annotation ua
        LEFT JOIN annotation_term at ON at.user_annotation_id = ua.userId
        LEFT JOIN term t ON t.userId = at.term_id
        WHERE ua.project_id = :projectId
        AND (:startDate IS NULL OR at.created > :startDate)
        AND (:endDate IS NULL OR at.created < :endDate)
        GROUP BY at.term_id, t.userId, t.name, t.color
        """, countQuery = """
        SELECT count(*) FROM (
            SELECT at.term_id
            FROM user_annotation ua
            LEFT JOIN annotation_term at ON at.user_annotation_id = ua.userId
            WHERE ua.project_id = :projectId
            AND (:startDate IS NULL OR at.created > :startDate)
            AND (:endDate IS NULL OR at.created < :endDate)
            GROUP BY at.term_id
        ) AS _count
        """, nativeQuery = true)
    Page<StatTermProjection> findAllByProjectForStats(long projectId, LocalDateTime startDate, LocalDateTime endDate,
                                                      Pageable pageable);


    @Query(value = """
        SELECT u.userId AS userId, u.username AS key, t.userId AS termId, t.name AS termName,
               count(at.userId) AS value, t.color AS termColor
        FROM annotation_term at
        JOIN user_annotation ua ON ua.userId = at.user_annotation_id
        JOIN sec_user u ON u.userId = at.user_id
        JOIN term t ON t.userId = at.term_id
        JOIN project p ON p.userId = :projectId AND ua.project_id = p.userId
        WHERE t.ontology_id = p.ontology_id
        AND t.deleted IS NULL
        GROUP BY u.userId, u.username, t.userId, t.name, t.color
        """,
        countQuery = """
            SELECT count(*) FROM (
                SELECT at.user_id, at.term_id
                FROM annotation_term at
                JOIN user_annotation ua ON ua.userId = at.user_annotation_id
                JOIN term t ON t.userId = at.term_id
                JOIN project p ON p.userId = :projectId AND ua.project_id = p.userId
                WHERE t.ontology_id = p.ontology_id
                AND t.deleted IS NULL
                GROUP BY at.user_id, at.term_id
            ) AS _count
        """,
        nativeQuery = true)
    Page<StatUserTermProjection> findAllByUsersByProjectForStats(long projectId, Pageable pageable);

    @Query(value = """
        SELECT ua.image_id AS image, at.term_id AS term, COUNT(ua.userId) AS countAnnotations
        FROM user_annotation ua
        LEFT JOIN annotation_term at ON at.user_annotation_id = ua.userId
        WHERE ua.deleted IS NULL
        AND at.deleted IS NULL
        AND ua.project_id = :projectId
        AND (:startDate IS NULL OR at.created > :startDate)
        AND (:endDate IS NULL OR at.created < :endDate)
        GROUP BY ua.image_id, at.term_id
        ORDER BY ua.image_id, at.term_id
        """, countQuery = """
        SELECT count(*) FROM (
            SELECT ua.image_id, at.term_id
            FROM user_annotation ua
            LEFT JOIN annotation_term at ON at.user_annotation_id = ua.userId
            WHERE ua.deleted IS NULL
            AND at.deleted IS NULL
            AND ua.project_id = :projectId
            AND (:startDate IS NULL OR at.created > :startDate)
            AND (:endDate IS NULL OR at.created < :endDate)
            GROUP BY ua.image_id, at.term_id
        ) AS _count
        """, nativeQuery = true)
    Page<StatPerTermAndImageProjection> findAllPerTermAndImageByProjectForStats(long projectId,
                                                                                LocalDateTime startDate,
                                                                                LocalDateTime endDate,
                                                                                Pageable pageable);
}
