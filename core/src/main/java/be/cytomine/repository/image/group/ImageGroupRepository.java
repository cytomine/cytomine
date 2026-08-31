package be.cytomine.repository.image.group;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import be.cytomine.domain.image.group.ImageGroup;
import be.cytomine.domain.project.Project;

@Repository
public interface ImageGroupRepository extends JpaRepository<ImageGroup, Long>, JpaSpecificationExecutor<ImageGroup> {

    List<ImageGroup> findAllByProject(Project project);
}
