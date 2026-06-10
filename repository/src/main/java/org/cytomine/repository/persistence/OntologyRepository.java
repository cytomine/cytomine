package org.cytomine.repository.persistence;

import java.util.Optional;

import org.cytomine.repository.persistence.entity.OntologyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OntologyRepository extends JpaRepository<OntologyEntity, Long> {
    @Query(value = """
        SELECT * FROM ontology WHERE ontology.id = :id AND ontology.deleted = null
        LEFT JOIN term t on t.ontology_id = :id AND t.deleted = null
        """, nativeQuery = true)
    Optional<OntologyEntity> findByIdAndDeletedNull(long id);
}
