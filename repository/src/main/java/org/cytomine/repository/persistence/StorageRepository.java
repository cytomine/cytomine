package org.cytomine.repository.persistence;

import org.cytomine.repository.persistence.entity.StorageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StorageRepository extends JpaRepository<StorageEntity, Long> {}
