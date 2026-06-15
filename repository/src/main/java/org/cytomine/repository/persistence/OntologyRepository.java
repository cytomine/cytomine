package org.cytomine.repository.persistence;

import java.util.Optional;

import org.cytomine.repository.persistence.entity.OntologyEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OntologyRepository extends JpaRepository<OntologyEntity, Long> {

    Optional<OntologyEntity> findByIdAndDeletedNull(long id);

    Page<OntologyEntity> findAllByUserId(long userId, Pageable pageable);
}
