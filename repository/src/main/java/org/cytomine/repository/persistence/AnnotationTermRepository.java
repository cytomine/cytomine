package org.cytomine.repository.persistence;

import java.util.List;
import java.util.Optional;

import org.cytomine.repository.persistence.entity.AnnotationTermEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnnotationTermRepository extends JpaRepository<AnnotationTermEntity, Long> {

    List<AnnotationTermEntity> findAllByUserAnnotationId(long userAnnotationId);

    List<AnnotationTermEntity> findAllByUserAnnotationIdAndUserId(long userAnnotationId, long userId);

    Optional<AnnotationTermEntity> findByUserAnnotationIdAndTermIdAndUserId(long userAnnotationId, long termId,
                                                                            long userId);

    @Query(value = "SELECT at.* FROM annotation_term at "
        + "JOIN user_annotation ua ON ua.id = at.user_annotation_id "
        + "WHERE ua.project_id = :projectId AND at.deleted IS NULL", nativeQuery = true)
    List<AnnotationTermEntity> findAllByProjectId(@Param("projectId") long projectId);

    @Query(value = "SELECT COUNT(*) FROM annotation_term WHERE term_id = :termId AND deleted IS NULL",
        nativeQuery = true)
    long countByTermId(@Param("termId") long termId);
}
