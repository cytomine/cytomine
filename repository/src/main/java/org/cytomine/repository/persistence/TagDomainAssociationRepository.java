package org.cytomine.repository.persistence;

import java.util.Optional;

import org.cytomine.repository.persistence.entity.TagDomainAssociationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagDomainAssociationRepository extends JpaRepository<TagDomainAssociationEntity, Long> {
    Optional<TagDomainAssociationEntity> findByIdAndDeletedNull(long id);
}
