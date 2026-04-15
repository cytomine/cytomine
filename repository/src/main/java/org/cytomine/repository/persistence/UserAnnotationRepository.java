package org.cytomine.repository.persistence;

import java.time.LocalDateTime;
import java.util.List;

import org.cytomine.repository.persistence.entity.UserAnnotationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAnnotationRepository extends JpaRepository<UserAnnotationEntity, Long> {

    List<UserAnnotationEntity> findAllByUserIdAndImageId(long userId, long imageId);

    long countByProjectId(long projectId);

    long countByUserId(long userId);

    long countByUserIdAndProjectId(long userId, long projectId);

    @Query(value = "INSERT INTO user_annotation "
        + "(version, created, updated, user_id, image_id, slice_id, project_id, "
        + "location, wkt_location, geometry_compression, count_reviewed_annotations, count_comments) "
        + "VALUES (0, :created, :updated, :userId, :imageId, :sliceId, :projectId, "
        + "ST_GeomFromText(:wktLocation, 0), :wktLocation, :geometryCompression, 0, 0) RETURNING id",
        nativeQuery = true)
    Long insertWithGeometry(@Param("created") LocalDateTime created,
                            @Param("updated") LocalDateTime updated,
                            @Param("userId") long userId,
                            @Param("imageId") long imageId,
                            @Param("sliceId") long sliceId,
                            @Param("projectId") long projectId,
                            @Param("wktLocation") String wktLocation,
                            @Param("geometryCompression") double geometryCompression);

    @Modifying(clearAutomatically = true)
    @Query(nativeQuery = true, value = "UPDATE user_annotation "
        + "SET location = ST_GeomFromText(:wktLocation, 0), wkt_location = :wktLocation, "
        + "geometry_compression = :geometryCompression, updated = :updated "
        + "WHERE id = :id")
    void updateGeometry(@Param("id") long id,
                        @Param("wktLocation") String wktLocation,
                        @Param("geometryCompression") double geometryCompression,
                        @Param("updated") LocalDateTime updated);
}
