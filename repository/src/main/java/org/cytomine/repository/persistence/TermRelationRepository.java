package org.cytomine.repository.persistence;

import java.util.Optional;

import org.cytomine.repository.persistence.entity.TermRelationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermRelationRepository extends JpaRepository<TermRelationEntity, Long> {

     Optional<TermRelationEntity> findByTerm1IdAndTerm2Id(Long term1Id,Long term2Id);
}
