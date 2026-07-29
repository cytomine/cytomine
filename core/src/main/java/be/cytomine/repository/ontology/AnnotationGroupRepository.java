package be.cytomine.repository.ontology;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import be.cytomine.domain.image.group.ImageGroup;
import be.cytomine.domain.ontology.AnnotationGroup;
import be.cytomine.domain.project.Project;

@Repository
public interface AnnotationGroupRepository
    extends JpaRepository<AnnotationGroup, Long>, JpaSpecificationExecutor<AnnotationGroup> {

    List<AnnotationGroup> findAllByImageGroup(ImageGroup group);

    List<AnnotationGroup> findAllByProject(Project project);

    void deleteAllByImageGroup(ImageGroup group);
}
