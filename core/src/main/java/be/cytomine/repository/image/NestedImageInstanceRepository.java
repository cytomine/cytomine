package be.cytomine.repository.image;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import be.cytomine.domain.image.AbstractImage;
import be.cytomine.domain.image.ImageInstance;
import be.cytomine.domain.image.NestedImageInstance;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.User;

@Repository
public interface NestedImageInstanceRepository
    extends JpaRepository<NestedImageInstance, Long>, JpaSpecificationExecutor<NestedImageInstance> {

    @EntityGraph(attributePaths = {"baseImage"})
    List<NestedImageInstance> findAllByParent(ImageInstance image);

    @EntityGraph(attributePaths = {"baseImage"})
    List<NestedImageInstance> findAllByBaseImage(AbstractImage image);

    Optional<NestedImageInstance> findByBaseImageAndParentAndProject(
        AbstractImage baseImage,
        ImageInstance parent,
        Project project
    );

    void deleteAllByUser(User user);
}
