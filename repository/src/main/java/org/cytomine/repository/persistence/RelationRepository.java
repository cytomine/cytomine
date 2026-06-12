package org.cytomine.repository.persistence;

import org.cytomine.repository.persistence.entity.RelationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RelationRepository extends JpaRepository<RelationEntity, Long> {

    @Query(nativeQuery = true, value = "SELECT * FROM relation WHERE name = 'parent';")
    RelationEntity findParent();
}
