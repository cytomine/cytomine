package be.cytomine.repository.ontology;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import be.cytomine.domain.image.ImageInstance;
import be.cytomine.domain.ontology.ReviewedAnnotation;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.User;
import be.cytomine.dto.ReviewedAnnotationStatsEntry;


public interface ReviewedAnnotationRepository
    extends JpaRepository<ReviewedAnnotation, Long>, JpaSpecificationExecutor<ReviewedAnnotation> {

    Long countByProject(Project project);

    long countByUserId(long userId);

    Long countByProjectAndCreatedAfter(Project project, Date createdMin);

    Long countByProjectAndCreatedBefore(Project project, Date createdMax);

    Long countByProjectAndCreatedBetween(Project project, Date createdMin, Date createdMax);

    @Query(
        value = "SELECT user_id, count(*), sum(count_reviewed_annotations) as total "
            + "FROM user_annotation ua "
            + "WHERE ua.image_id = :imageId "
            + "GROUP BY user_id "
            + "ORDER BY total DESC",
        nativeQuery = true
    )
    List<Tuple> stats(Long imageId);

    default List<ReviewedAnnotationStatsEntry> stats(ImageInstance imageInstance) {
        List<ReviewedAnnotationStatsEntry> reviewedAnnotationStatsEntries = new ArrayList<>();
        for (Tuple tuple : stats(imageInstance.getId())) {
            reviewedAnnotationStatsEntries.add(new ReviewedAnnotationStatsEntry(
                    (Long) tuple.get(0),
                    (Long) tuple.get(1),
                    (Long) tuple.get(2)
                )
            );
        }
        return reviewedAnnotationStatsEntries;
    }

    @Query(
        value = "SELECT count_reviewed_annotations as total "
            + "FROM user_annotation ua "
            + "WHERE ua.id = :userAnnotationId",
        nativeQuery = true
    )
    long countReviewedAnnotation(Long userAnnotationId);

    Optional<ReviewedAnnotation> findByParentIdent(Long parentIdent);

    List<ReviewedAnnotation> findAllByImage(ImageInstance image);

    long countAllByTermsId(Long termId);

    long countAllByProjectAndTermsEmpty(Project project);

    List<ReviewedAnnotation> findAllByUserId(Long userId);
}
