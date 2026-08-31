package org.cytomine.repository.persistence;

import java.util.Optional;

import org.cytomine.repository.persistence.entity.StorageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StorageRepository extends JpaRepository<StorageEntity, Long> {
    Optional<StorageEntity> findByIdAndDeletedNull(long id);

    Page<StorageEntity> findAllByDeletedNull(Pageable pageable);

    @Query(
        value = """
            SELECT DISTINCT s.* FROM storage s
            JOIN acl_object_identity aoi ON aoi.object_id_identity = s.id
            JOIN acl_class ac ON ac.id = aoi.object_id_class AND ac.class = 'be.cytomine.domain.image.server.Storage'
            JOIN acl_entry ae ON ae.acl_object_identity = aoi.id
            JOIN acl_sid sid ON sid.id = ae.sid
            JOIN sec_user u ON u.username = sid.sid
            WHERE s.deleted IS NULL AND u.id = :userId
            """,
        countQuery = """
            SELECT count(DISTINCT s.id) FROM storage s
            JOIN acl_object_identity aoi ON aoi.object_id_identity = s.id
            JOIN acl_class ac ON ac.id = aoi.object_id_class AND ac.class = 'be.cytomine.domain.image.server.Storage'
            JOIN acl_entry ae ON ae.acl_object_identity = aoi.id
            JOIN acl_sid sid ON sid.id = ae.sid
            JOIN sec_user u ON u.username = sid.sid
            WHERE s.deleted IS NULL AND u.id = :userId
            """,
        nativeQuery = true
    )
    Page<StorageEntity> findAllReadableByUser(long userId, Pageable pageable);
}
