package be.cytomine.repository.annotation;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import be.cytomine.domain.annotation.Annotation;
import be.cytomine.domain.annotation.AnnotationLayer;

@Repository
public interface AnnotationRepository extends JpaRepository<Annotation, Long> {
    Set<Annotation> findAllByAnnotationLayer(AnnotationLayer layer);
}
