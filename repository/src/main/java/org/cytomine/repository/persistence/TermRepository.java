package org.cytomine.repository.persistence;

import java.util.Optional;

import org.cytomine.repository.persistence.entity.TermEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TermRepository extends JpaRepository<TermEntity, Long> {

    @Query(value = "SELECT t.* FROM term t JOIN project p ON t.ontology_id = p.ontology_id WHERE p.id = :id",
        countQuery = "SELECT count(*) FROM term t JOIN project p ON t.ontology_id = p.ontology_id WHERE p.id = :id",
        nativeQuery = true)
    Page<TermEntity> findAllByProjectId(long id, Pageable pageable);

    Page<TermEntity> findAllByOntologyId(long id, Pageable pageable);

    Optional<TermEntity> deleteTermById(long id);

}
