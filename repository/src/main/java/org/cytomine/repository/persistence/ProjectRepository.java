package org.cytomine.repository.persistence;

import java.util.Optional;

import org.cytomine.repository.persistence.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {
    Optional<ProjectEntity> findByIdAndDeletedNull(long id);
}
