package org.cytomine.repository.persistence;

import org.cytomine.repository.persistence.entity.TermEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermRepository extends JpaRepository<TermEntity, Long> {
}
