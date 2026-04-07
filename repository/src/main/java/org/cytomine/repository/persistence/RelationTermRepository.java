package org.cytomine.repository.persistence;

import org.cytomine.repository.persistence.entity.RelationTermEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RelationTermRepository extends JpaRepository<RelationTermEntity, Long> {
}
