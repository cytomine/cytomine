package org.cytomine.repository.persistence;

import java.util.List;

import org.cytomine.repository.persistence.entity.TermEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermRepository extends JpaRepository<TermEntity, Long> {

    List<TermEntity> findAll();

    TermEntity save(TermEntity termEntity);

    List<TermEntity> findAllByProjectId(long id);

    List<TermEntity> findAllByOntologyId(long id);

}
