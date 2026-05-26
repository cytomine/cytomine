package org.cytomine.repository.persistence;

import java.util.List;
import java.util.Optional;

import org.cytomine.repository.persistence.entity.TermRelationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TermRelationRepository extends JpaRepository<TermRelationEntity, Long> {

    Optional<TermRelationEntity> findByTerm1IdAndTerm2Id(Long term1Id, Long term2Id);

    Optional<TermRelationEntity> findByRelationIdAndTerm1IdAndTerm2Id(Long relationId, Long term1Id, Long term2Id);

    List<TermRelationEntity> findAllByTerm1IdOrTerm2Id(Long term1Id, Long term2Id);

    @Query(value = "SELECT tr.* FROM relation_term tr JOIN term t ON t.id = tr.term1_id "
        + "WHERE t.ontology_id = :ontologyId AND tr.deleted IS NULL", nativeQuery = true)
    List<TermRelationEntity> findAllByOntologyId(@Param("ontologyId") long ontologyId);
}
