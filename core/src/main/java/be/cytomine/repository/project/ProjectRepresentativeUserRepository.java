package be.cytomine.repository.project;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import be.cytomine.domain.project.Project;
import be.cytomine.domain.project.ProjectRepresentativeUser;

@Repository
public interface ProjectRepresentativeUserRepository extends JpaRepository<ProjectRepresentativeUser, Long> {


    Optional<ProjectRepresentativeUser> findByProjectAndUserId(Project project, long userId);

    List<ProjectRepresentativeUser> findAllByProject(Project project);

    List<ProjectRepresentativeUser> findAllByUserId(long userId);
}
