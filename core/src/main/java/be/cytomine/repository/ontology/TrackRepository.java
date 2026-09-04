package be.cytomine.repository.ontology;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import be.cytomine.domain.image.ImageInstance;
import be.cytomine.domain.ontology.Track;
import be.cytomine.domain.project.Project;

public interface TrackRepository extends JpaRepository<Track, Long>, JpaSpecificationExecutor<Track> {

    List<Track> findAllByImage(ImageInstance imageInstance);

    List<Track> findAllByProject(Project project);

    Long countByProject(Project project);

    Long countByProjectAndCreatedAfter(Project project, Date createdMin);

    Long countByProjectAndCreatedBefore(Project project, Date createdMax);

    Long countByProjectAndCreatedBetween(Project project, Date createdMin, Date createdMax);

    Optional<Track> findByNameAndImage(String name, ImageInstance imageInstance);
}
