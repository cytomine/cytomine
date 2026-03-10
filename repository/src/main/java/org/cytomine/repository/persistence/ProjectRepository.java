package org.cytomine.repository.persistence;

import org.cytomine.common.repository.persistence.ProjectEntity;

import java.util.Optional;

public interface ProjectRepository {
    Optional<ProjectEntity> findById(long id);
}
