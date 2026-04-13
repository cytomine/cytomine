package org.cytomine.repository.persistence;

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

    @Modifying
    @Query(nativeQuery = true, value = "UPDATE user_annotation "
        + "SET location = ST_GeomFromText(:wktLocation, 0), wkt_location = :wktLocation, "
        + "geometry_compression = :geometryCompression, updated = :updated "
        + "WHERE id = :id")
    void updateGeometry(@Param("id") long id,
                        @Param("wktLocation") String wktLocation,
                        @Param("geometryCompression") double geometryCompression,
                        @Param("updated") java.time.LocalDateTime updated);
}
