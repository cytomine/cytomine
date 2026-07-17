package org.cytomine.repository.persistence;

import java.util.Optional;
import java.util.Set;

import org.cytomine.repository.persistence.entity.TagDomainAssociationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TagDomainAssociationRepository extends JpaRepository<TagDomainAssociationEntity, Long> {
    Optional<TagDomainAssociationEntity> findByIdAndDeletedNull(long id);

    boolean existsByTagIdAndDeletedNull(long tagId);

    Set<Long> findAllIdsByTagIdAndDeletedNull(long tagId);

    Page<TagDomainAssociationEntity> findAllByDomainClassNameAndDomainIdAndDeletedNull(
        String domainClassName,
        long domainId,
        Pageable pageable
    );

    Page<TagDomainAssociationEntity> findAllByDeletedNull(Pageable pageable);

    @Query(
        value = """
            SELECT DISTINCT tda.* FROM tag_domain_association tda
            JOIN acl_object_identity aoi ON aoi.object_id_identity = tda.domain_id
            JOIN acl_class ac ON ac.id = aoi.object_id_class AND ac.class = tda.domain_class_name
            JOIN acl_entry ae ON ae.acl_object_identity = aoi.id
            JOIN acl_sid sid ON sid.id = ae.sid
            JOIN sec_user u ON u.username = sid.sid
            WHERE tda.deleted IS NULL AND u.id = :userId
            """,
        countQuery = """
            SELECT count(DISTINCT tda.id) FROM tag_domain_association tda
            JOIN acl_object_identity aoi ON aoi.object_id_identity = tda.domain_id
            JOIN acl_class ac ON ac.id = aoi.object_id_class AND ac.class = tda.domain_class_name
            JOIN acl_entry ae ON ae.acl_object_identity = aoi.id
            JOIN acl_sid sid ON sid.id = ae.sid
            JOIN sec_user u ON u.username = sid.sid
            WHERE tda.deleted IS NULL AND u.id = :userId
            """,
        nativeQuery = true
    )
    Page<TagDomainAssociationEntity> findAllReadableByUser(long userId, Pageable pageable);
}
