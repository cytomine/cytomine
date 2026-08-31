package be.cytomine.repository.image;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import be.cytomine.domain.image.AbstractImage;
import be.cytomine.domain.image.CompanionFile;
import be.cytomine.domain.image.UploadedFile;

@Repository
public interface CompanionFileRepository
    extends JpaRepository<CompanionFile, Long>, JpaSpecificationExecutor<CompanionFile> {

    List<CompanionFile> findAllByImage(AbstractImage abstractImage);

    boolean existsByImage(AbstractImage abstractImage);

    List<CompanionFile> findAllByUploadedFile(UploadedFile uploadedFile);

    Optional<CompanionFile> findByImageAndUploadedFile(AbstractImage abstractImage, UploadedFile uploadedFile);

    int countByImageAndType(AbstractImage image, String type);
}
