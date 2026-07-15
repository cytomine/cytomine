package org.cytomine.repository.persistence;

import org.cytomine.repository.persistence.entity.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<TagEntity, Long> {}
