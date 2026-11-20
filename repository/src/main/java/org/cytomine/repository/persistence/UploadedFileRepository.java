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
            AND (:ignoreIdFilter = TRUE OR uf.id IN (:ids))
            AND (:ignoreFilenameFilter = TRUE OR LOWER(COALESCE(uf.originalFilename, '')) LIKE
                LOWER(CONCAT('%', :originalFilename, '%')))
            """
    )
    Page<UploadedFileEntity> search(
        boolean ignoreStorageFilter,
        List<Long> storageIds,
        boolean ignoreIdFilter,
        List<Long> ids,
        boolean ignoreFilenameFilter,
        String originalFilename,
        Pageable pageable
    );

    default Page<UploadedFileEntity> search(List<Long> storageIds, List<Long> ids, Pageable pageable) {
        return search(storageIds, ids, null, pageable);
    }

    default Page<UploadedFileEntity> search(List<Long> storageIds, List<Long> ids, String originalFilename,
        Pageable pageable) {
        boolean hasFilename = originalFilename != null && !originalFilename.isBlank();
        return search(
            storageIds == null,
            storageIds != null ? storageIds : List.of(-1L),
            ids == null || ids.isEmpty(),
            ids != null && !ids.isEmpty() ? ids : List.of(-1L),
            !hasFilename,
            hasFilename ? originalFilename : "",
            pageable
        );
    }
}
