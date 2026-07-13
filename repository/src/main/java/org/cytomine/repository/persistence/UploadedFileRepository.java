package org.cytomine.repository.persistence;

import java.util.List;
import java.util.Optional;

import org.cytomine.repository.persistence.entity.UploadedFileEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UploadedFileRepository extends JpaRepository<UploadedFileEntity, Long> {

    Optional<UploadedFileEntity> findByIdAndDeletedNull(long id);

    @Query(
        """
            SELECT uf FROM uploaded_file uf
            WHERE uf.deleted IS NULL
            AND (:ignoreStorageFilter = TRUE OR uf.storageId IN (:storageIds))
            """
    )
    Page<UploadedFileEntity> search(boolean ignoreStorageFilter, List<Long> storageIds, Pageable pageable);

    default Page<UploadedFileEntity> search(List<Long> storageIds, Pageable pageable) {
        return search(storageIds == null, storageIds != null ? storageIds : List.of(-1L), pageable);
    }
}
