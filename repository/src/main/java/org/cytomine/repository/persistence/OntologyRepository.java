package org.cytomine.repository.persistence;

import java.util.Optional;

import org.cytomine.repository.persistence.entity.OntologyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OntologyRepository extends JpaRepository<OntologyEntity, Long> {
//    @Query(value = """
//        SELECT o FROM org.cytomine.repository.persistence.entity.OntologyEntity o
//        LEFT JOIN FETCH o.terms t ON t.deleted IS NULL
//        WHERE o.id = :id AND o.deleted IS NULL
//        """)
    Optional<OntologyEntity> findByIdAndDeletedNull(long id);
}
