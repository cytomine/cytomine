package org.cytomine.repository.persistence;

import java.util.Optional;
import java.util.Set;

import org.cytomine.repository.persistence.entity.OntologyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OntologyRepository extends JpaRepository<OntologyEntity, Long> {

    Optional<OntologyEntity> findByIdAndDeletedNull(long id);

    Set<OntologyEntity> findAllByUserId(long userId);
}
