package be.cytomine.repository.ontology;

import java.util.List;

import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import be.cytomine.domain.ontology.AnnotationDomain;

public interface AnnotationDomainRepository
    extends JpaRepository<AnnotationDomain, Long>, JpaSpecificationExecutor<AnnotationDomain> {


    @Query(
        value =
            "SELECT annotation.id as annotation, user_id as user "
                + "FROM user_annotation annotation "
                + "WHERE annotation.image_id = :image "
                + "AND user_id IN (:layers) "
                + "AND ST_Intersects(annotation.location, ST_GeometryFromText(:location, 0))",
        nativeQuery = true
    )
    List<Tuple> findAllIntersectForUserAnnotations(Long image, List<Long> layers, String location);

    @Query(
        value =
            "SELECT annotation.id as annotation, user_id as user "
                + "FROM reviewed_annotation annotation "
                + "WHERE annotation.image_id = :image "
                + "AND ST_Intersects(annotation.location, ST_GeometryFromText(:location, 0))",
        nativeQuery = true
    )
    List<Tuple> findAllIntersectForReviewedAnnotations(Long image, String location);

    @Query(
        value = "SELECT count(annotation.id) FROM user_annotation annotation WHERE annotation.project_id = :projectId",
        nativeQuery = true)
    Long countAllUserAnnotationAndProject(Long projectId);

    @Query(
        value = "SELECT count(annotation.id) "
            + "FROM reviewed_annotation annotation "
            + "WHERE annotation.project_id = :projectId",
        nativeQuery = true
    )
    Long countAllReviewedAnnotationAndProject(Long projectId);

}
