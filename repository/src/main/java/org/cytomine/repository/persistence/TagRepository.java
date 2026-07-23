package org.cytomine.repository.persistence;

import java.util.Optional;

import org.cytomine.repository.persistence.entity.TagEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TagRepository extends JpaRepository<TagEntity, Long> {
    Optional<TagEntity> findByIdAndDeletedNull(long id);

    Page<TagEntity> findAllByDeletedNull(Pageable pageable);

    @Query(
        value = """
            SELECT DISTINCT t.* FROM tag t
            JOIN acl_object_identity aoi ON aoi.object_id_identity = t.id
            JOIN acl_class ac ON ac.id = aoi.object_id_class AND ac.class = 'be.cytomine.domain.meta.Tag'
            JOIN acl_entry ae ON ae.acl_object_identity = aoi.id
            JOIN acl_sid sid ON sid.id = ae.sid
            JOIN sec_user u ON u.username = sid.sid
            WHERE t.deleted IS NULL AND u.id = :userId
            """,
        countQuery = """
            SELECT count(DISTINCT t.id) FROM tag t
            JOIN acl_object_identity aoi ON aoi.object_id_identity = t.id
            JOIN acl_class ac ON ac.id = aoi.object_id_class AND ac.class = 'be.cytomine.domain.meta.Tag'
            JOIN acl_entry ae ON ae.acl_object_identity = aoi.id
            JOIN acl_sid sid ON sid.id = ae.sid
            JOIN sec_user u ON u.username = sid.sid
            WHERE t.deleted IS NULL AND u.id = :userId
            """,
        nativeQuery = true
    )
    Page<TagEntity> findAllReadableByUser(long userId, Pageable pageable);
}
