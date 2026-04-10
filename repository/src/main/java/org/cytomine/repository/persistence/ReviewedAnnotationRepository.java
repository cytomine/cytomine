package org.cytomine.repository.persistence;

import java.util.Optional;

import org.cytomine.repository.persistence.entity.ReviewedAnnotationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewedAnnotationRepository extends JpaRepository<ReviewedAnnotationEntity, Long> {

    Optional<ReviewedAnnotationEntity> findByParentIdent(long parentIdent);

    @Query(value = "SELECT COUNT(*) FROM reviewed_annotation ra "
        + "JOIN reviewed_annotation_term rat ON rat.reviewed_annotation_terms_id = ra.id "
        + "WHERE rat.terms_id = :termId AND ra.deleted IS NULL", nativeQuery = true)
    long countByTermId(@Param("termId") long termId);

}
