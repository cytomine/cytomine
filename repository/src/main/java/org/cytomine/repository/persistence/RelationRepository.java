package org.cytomine.repository.persistence;

import java.util.Optional;

import org.cytomine.repository.persistence.entity.RelationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RelationRepository extends JpaRepository<RelationEntity, Long> {

    Optional<RelationEntity> findByName(String name);
}
