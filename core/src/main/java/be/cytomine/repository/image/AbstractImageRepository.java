package be.cytomine.repository.image;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import be.cytomine.domain.image.AbstractImage;
import be.cytomine.domain.image.UploadedFile;
import be.cytomine.domain.project.Project;

@Repository
public interface AbstractImageRepository
    extends JpaRepository<AbstractImage, Long>, JpaSpecificationExecutor<AbstractImage> {

    record AbstractImageIds(Long uploadedFileId, Long abstractImageId) {}

    @Override
    @EntityGraph(attributePaths = {"uploadedFile"})
    Page<AbstractImage> findAll(@Nullable Specification<AbstractImage> spec, Pageable pageable);

    List<AbstractImage> findAllByUploadedFile(UploadedFile uploadedFile);

    @Query(value = "SELECT DISTINCT ii.baseImage.id FROM ImageInstance ii WHERE ii.project = :project")
    Set<Long> findAllIdsByProject(Project project);

    Optional<AbstractImage> findByOriginalFilename(String originalFileName);

    @Query("""
        SELECT new be.cytomine.repository.image.AbstractImageRepository$AbstractImageIds(ai.uploadedFile.id, ai.id)
        FROM AbstractImage ai
        WHERE ai.uploadedFile.id IN :uploadedFileIds
        """)
    List<AbstractImageIds> findIdsByUploadedFileIds(Set<Long> uploadedFileIds);

    @Query("SELECT ai.id FROM AbstractImage ai WHERE ai.uploadedFile.id = :uploadedFileId")
    Optional<Long> findIdByUploadedFileId(Long uploadedFileId);
}
