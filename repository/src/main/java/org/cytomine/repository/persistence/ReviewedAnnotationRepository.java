package org.cytomine.repository.persistence;

import java.time.LocalDateTime;
import java.util.List;
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
        + "WHERE rat.term_id = :termId AND ra.deleted IS NULL", nativeQuery = true)
    long countByTermId(@Param("termId") long termId);

    @Query(value = "INSERT INTO reviewed_annotation "
        + "(version, created, updated, user_id, review_user_id, image_id, slice_id, project_id, "
        + "parent_ident, parent_class_name, status, location, wkt_location, geometry_compression, count_comments) "
        + "VALUES (0, :created, :updated, :userId, :reviewUserId, :imageId, :sliceId, :projectId, "
        + ":parentIdent, :parentClassName, :status, ST_GeomFromText(:wktLocation, 0), :wktLocation, "
        + ":geometryCompression, 0) RETURNING id", nativeQuery = true)
    Long insertWithGeometry(@Param("created") LocalDateTime created,
                            @Param("updated") LocalDateTime updated,
                            @Param("userId") long userId,
                            @Param("reviewUserId") long reviewUserId,
                            @Param("imageId") long imageId,
                            @Param("sliceId") long sliceId,
                            @Param("projectId") long projectId,
                            @Param("parentIdent") long parentIdent,
                            @Param("parentClassName") String parentClassName,
                            @Param("status") int status,
                            @Param("wktLocation") String wktLocation,
                            @Param("geometryCompression") double geometryCompression);

    @Modifying(clearAutomatically = true)
    @Query(nativeQuery = true, value = "UPDATE reviewed_annotation "
        + "SET location = ST_GeomFromText(:wktLocation, 0), wkt_location = :wktLocation, "
        + "geometry_compression = :geometryCompression, updated = :updated "
        + "WHERE id = :id")
    void updateGeometry(@Param("id") long id,
                        @Param("wktLocation") String wktLocation,
                        @Param("geometryCompression") double geometryCompression,
                        @Param("updated") LocalDateTime updated);

    @Query(value = "SELECT term_id FROM reviewed_annotation_term "
        + "WHERE reviewed_annotation_terms_id = :id", nativeQuery = true)
    List<Long> findTermIds(@Param("id") long reviewedAnnotationId);

    @Modifying
    @Query(nativeQuery = true, value = "INSERT INTO reviewed_annotation_term "
        + "(reviewed_annotation_terms_id, term_id) VALUES (:reviewedAnnotationId, :termId)")
    void insertTermLink(@Param("reviewedAnnotationId") long reviewedAnnotationId,
                        @Param("termId") long termId);
}
