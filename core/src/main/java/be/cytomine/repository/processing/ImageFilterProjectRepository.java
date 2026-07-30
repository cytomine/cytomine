package be.cytomine.repository.processing;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import be.cytomine.domain.processing.ImageFilter;
import be.cytomine.domain.processing.ImageFilterProject;
import be.cytomine.domain.project.Project;

@Repository
public interface ImageFilterProjectRepository
    extends JpaRepository<ImageFilterProject, Long>, JpaSpecificationExecutor<ImageFilterProject> {

    List<ImageFilterProject> findAllByProject(Project project);

    Optional<ImageFilterProject> findByImageFilterAndProject(ImageFilter imageFilter, Project project);
}
