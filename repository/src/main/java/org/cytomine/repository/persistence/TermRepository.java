package org.cytomine.repository.persistence;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

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
        + " WHERE p.id = :id and t.deleted is null",
        countQuery = "SELECT count(*) FROM term t JOIN project p ON t.ontology_id = p.ontology_id"
            + " WHERE p.id = :id and t.deleted = :deleted", nativeQuery = true)
    Page<TermEntity> findAllByProjectId(long id, Pageable pageable);

    Page<TermEntity> findAllByOntologyIdAndDeletedNull(long id, Pageable pageable);

    @Query(value = "SELECT t.id FROM term t WHERE t.ontology_id = :ontologyId AND t.deleted IS NULL",
        nativeQuery = true)
    Set<Long> findAllIdsByOntologyId(long ontologyId);

    @Query(value = """
        SELECT t.id FROM term t JOIN project p
        ON t.ontology_id = p.ontology_id
        WHERE p.id = :projectId AND t.deleted IS NULL
        """, nativeQuery = true)
    Set<Long> findAllIdsByProjectId(long projectId);

    @Query(
        value = """
            SELECT t.id AS id, t.name AS name, count(DISTINCT ua.image_id) AS count, t.color AS color
            FROM user_annotation ua
            LEFT JOIN annotation_term at ON at.user_annotation_id = ua.id
            LEFT JOIN term t ON t.id = at.term_id
            WHERE ua.project_id = :projectId
            AND (CAST(:startDate AS timestamp) IS NULL OR at.created > :startDate)
            AND (CAST(:endDate AS timestamp) IS NULL OR at.created < :endDate)
            GROUP BY at.term_id, t.id, t.name, t.color
            """,
        countQuery = """
            SELECT count(*) FROM (
                SELECT at.term_id
                FROM user_annotation ua
                LEFT JOIN annotation_term at ON at.user_annotation_id = ua.id
                WHERE ua.project_id = :projectId
                AND (CAST(:startDate AS timestamp) IS NULL OR at.created > :startDate)
                AND (CAST(:endDate AS timestamp) IS NULL OR at.created < :endDate)
                GROUP BY at.term_id
            ) AS _count
            """,
        nativeQuery = true
    )
    Page<StatTermProjection> findAllByProjectForStats(
        long projectId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    );

    @Query(
        value = """
            SELECT u.id AS userId, u.username AS username, t.id AS termId, t.name AS termName,
                   count(at.id) AS termCount, t.color AS termColor
            FROM annotation_term at
            JOIN user_annotation ua ON ua.id = at.user_annotation_id
            JOIN sec_user u ON u.id = at.user_id
            JOIN term t ON t.id = at.term_id
            JOIN project p ON p.id = :projectId AND ua.project_id = p.id
            WHERE t.ontology_id = p.ontology_id
            AND t.deleted IS NULL
            GROUP BY u.id, u.username, t.id, t.name, t.color
            """,
        countQuery = """
            SELECT count(*) FROM (
                SELECT at.user_id, at.term_id
                FROM annotation_term at
                JOIN user_annotation ua ON ua.id = at.user_annotation_id
                JOIN term t ON t.id = at.term_id
                JOIN project p ON p.id = :projectId AND ua.project_id = p.id
                WHERE t.ontology_id = p.ontology_id
                AND t.deleted IS NULL
                GROUP BY at.user_id, at.term_id
            ) AS _count
            """,
        nativeQuery = true
    )
    Page<StatUserTermProjection> findAllByUsersByProjectForStats(long projectId, Pageable pageable);

    @Query(
        value = """
            SELECT ua.image_id AS imageId, at.term_id AS termId, COUNT(ua.id) AS countAnnotations
            FROM user_annotation ua
            LEFT JOIN annotation_term at ON at.user_annotation_id = ua.id
            WHERE ua.deleted IS NULL
            AND at.deleted IS NULL
            AND ua.project_id = :projectId
            AND (CAST(:startDate AS timestamp) IS NULL OR at.created > :startDate)
            AND (CAST(:endDate AS timestamp) IS NULL OR at.created < :endDate)
            GROUP BY ua.image_id, at.term_id
            ORDER BY ua.image_id, at.term_id
            """,
        countQuery = """
            SELECT count(*) FROM (
                SELECT ua.image_id, at.term_id
                FROM user_annotation ua
                LEFT JOIN annotation_term at ON at.user_annotation_id = ua.id
                WHERE ua.deleted IS NULL
                AND at.deleted IS NULL
                AND ua.project_id = :projectId
                AND (CAST(:startDate AS timestamp) IS NULL OR at.created > :startDate)
                AND (CAST(:endDate AS timestamp) IS NULL OR at.created < :endDate)
                GROUP BY ua.image_id, at.term_id
            ) AS _count
            """,
        nativeQuery = true
    )
    Page<StatPerTermAndImageProjection> findAllPerTermAndImageByProjectForStats(
        long projectId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    );

    Optional<TermEntity> findByIdAndDeletedNull(long id);
}
