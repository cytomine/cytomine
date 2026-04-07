package org.cytomine.repository.persistence;

import org.cytomine.repository.persistence.entity.TermRelationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermRelationRepository extends JpaRepository<TermRelationEntity, Long> {
}
