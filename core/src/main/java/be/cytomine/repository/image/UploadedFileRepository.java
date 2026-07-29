package be.cytomine.repository.image;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import be.cytomine.domain.image.UploadedFile;
import be.cytomine.domain.image.server.Storage;
import be.cytomine.domain.security.User;

/**
 * Spring Data JPA repository for the user entity.
 */
@Repository
public interface UploadedFileRepository
    extends JpaRepository<UploadedFile, Long>, JpaSpecificationExecutor<UploadedFile> {

    @Query(
        value = "SELECT uf.* "
            + "FROM uploaded_file uf "
            + "WHERE true "
            + "AND (:ignoreUserIdFilter IS TRUE OR uf.user_id = :userId) "
            + "AND (:ignoreParentIdFilter IS TRUE OR uf.parent_id = :parentId) "
            + "AND (:ignoreOnlyRootFilter IS TRUE OR (:onlyRoot IS FALSE OR uf.parent_id IS NULL)) "
            + "AND (:ignoreStorageIdsFilter IS TRUE OR (uf.storage_id IN (:storagesIds)))",
        nativeQuery = true
    )
    Page<UploadedFile> search(
        Boolean ignoreUserIdFilter,
        Long userId,
        Boolean ignoreParentIdFilter,
        Long parentId,
        Boolean ignoreOnlyRootFilter,
        Boolean onlyRoot,
        Boolean ignoreStorageIdsFilter,
        List<Long> storagesIds,
        Pageable pageable
    );

    default Page<UploadedFile> search(
        Long userId,
        Long parentId,
        Boolean onlyRoot,
        List<Long> storagesIds,
        Pageable pageable
    ) {
        return search(
            userId == null,
            Optional.ofNullable(userId).orElse(-1L),
            parentId == null,
            Optional.ofNullable(parentId).orElse(-1L),
            onlyRoot == null,
            Optional.ofNullable(onlyRoot).orElse(false),
            storagesIds == null,
            Optional.ofNullable(storagesIds).orElse(new ArrayList<>()),
            pageable
        );
    }

    Integer countByParent(UploadedFile domain);

    List<UploadedFile> findAllByParent(UploadedFile uploadedFile);

    void deleteAllByUser(User user);

    long countByStorage(Storage storage);
}
