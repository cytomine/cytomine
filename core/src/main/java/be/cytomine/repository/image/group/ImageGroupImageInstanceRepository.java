package be.cytomine.repository.image.group;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import be.cytomine.domain.image.ImageInstance;
import be.cytomine.domain.image.group.ImageGroup;
import be.cytomine.domain.image.group.ImageGroupImageInstance;

@Repository
public interface ImageGroupImageInstanceRepository
    extends JpaRepository<ImageGroupImageInstance, Long>, JpaSpecificationExecutor<ImageGroupImageInstance> {

    List<ImageGroupImageInstance> findAllByImage(ImageInstance image);

    List<ImageGroupImageInstance> findAllByGroup(ImageGroup group);

    Optional<ImageGroupImageInstance> findByGroupAndImage(ImageGroup group, ImageInstance imageInstance);

    void deleteAllByGroup(ImageGroup group);
}
