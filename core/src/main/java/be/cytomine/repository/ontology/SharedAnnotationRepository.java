package be.cytomine.repository.ontology;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import be.cytomine.domain.ontology.SharedAnnotation;

public interface SharedAnnotationRepository
    extends JpaRepository<SharedAnnotation, Long>, JpaSpecificationExecutor<SharedAnnotation> {
    List<SharedAnnotation> findAllByAnnotationIdentOrderByCreatedDesc(Long annotationId);
}
