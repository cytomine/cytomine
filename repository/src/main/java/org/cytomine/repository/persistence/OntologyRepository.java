package org.cytomine.repository.persistence;

import java.util.List;
import java.util.Optional;

import org.cytomine.repository.persistence.entity.OntologyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OntologyRepository extends JpaRepository<OntologyEntity, Long> {

    Optional<OntologyEntity> findByName(String name);
    Optional<OntologyEntity> findById(long id);
}
