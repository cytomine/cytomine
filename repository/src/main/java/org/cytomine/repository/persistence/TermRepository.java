package org.cytomine.repository.persistence;

import org.cytomine.repository.persistence.entity.TermEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermRepository extends JpaRepository<TermEntity, Long> {

    Page<TermEntity> findAllByProjectId(long id, Pageable pageable);

    Page<TermEntity> findAllByOntologyId(long id, Pageable pageable);

}
