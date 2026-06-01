package org.cytomine.repository.persistence;

import org.cytomine.repository.persistence.entity.OntologyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OntologyRepository extends JpaRepository<OntologyEntity, Long> {
}
