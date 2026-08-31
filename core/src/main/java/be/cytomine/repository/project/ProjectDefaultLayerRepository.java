package be.cytomine.repository.project;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import be.cytomine.domain.project.Project;
import be.cytomine.domain.project.ProjectDefaultLayer;
import be.cytomine.domain.security.User;

@Repository
public interface ProjectDefaultLayerRepository extends JpaRepository<ProjectDefaultLayer, Long> {


    Optional<ProjectDefaultLayer> findByProjectAndUser(Project project, User user);

    List<ProjectDefaultLayer> findAllByProject(Project project);

    List<ProjectDefaultLayer> findAllByUser(User user);
}
