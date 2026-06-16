package org.cytomine.repository.persistence;

import java.util.Set;

import org.cytomine.repository.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    @Query(
        value = """
            SELECT DISTINCT u.* FROM sec_user u
            JOIN acl_sid sid ON sid.sid = u.username
            JOIN acl_entry ae ON ae.sid = sid.id
            JOIN acl_object_identity aoi ON aoi.id = ae.acl_object_identity
            JOIN acl_class ac ON ac.id = aoi.object_id_class AND ac.class = 'be.cytomine.domain.image.server.Storage'
            WHERE aoi.object_id_identity = :storageId
            """,
        nativeQuery = true
    )
    Set<UserEntity> findAllByStorageId(long storageId);
}
