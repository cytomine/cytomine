package org.cytomine.repository.persistence;

import java.util.Optional;

import org.cytomine.repository.persistence.entity.UploadedFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadedFileRepository extends JpaRepository<UploadedFileEntity, Long> {
    Optional<UploadedFileEntity> findByIdAndDeletedNull(long id);
}
