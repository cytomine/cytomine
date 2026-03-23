package be.cytomine.repositorynosql.social;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import be.cytomine.domain.social.PersistentImageConsultation;

@Repository
public interface PersistentImageConsultationRepository extends MongoRepository<PersistentImageConsultation, Long> {

    Long countByProject(Long project);

    Long countByProjectAndCreatedAfter(Long project, Date createdMin);

    Long countByProjectAndCreatedBefore(Long project, Date createdMax);

    Long countByProjectAndCreatedBetween(Long project, Date createdMin, Date createdMax);


    Page<PersistentImageConsultation> findAllByUserAndImageAndCreatedLessThan(
        Long user,
        Long image,
        Date before,
        PageRequest created
    );

    @Aggregation(pipeline = {
        "{$match: {project: ?0}}",
        "{$sort: {?1: ?2}}",
        "{$group: {_id: '$user', created: {$max: '$created'}}}",
        "{$sort: {?1: ?2}}"
    }
    )
    AggregationResults retrieve(Long project, String sortProperty, Integer sortDirection);


    @Aggregation(pipeline = {
        "{$match: {"
            + "project: ?0, "
            + "user: ?1, "
            + "image: ?2, "
            + "$and: ["
            + "    {created: {$gte: ?4}}, "
            + "    {created: {$lte: ?3}}"
            + "]"
            + "}},"
            + "{$sort: {created: 1}},"
            + "{$project: {dateInMillis: {$subtract: ['$created', ?5]}}}"
    }
    )
    AggregationResults retrieve(Long project, Long user, Long image, Date before, Date after, Date firstDate);

    Page<PersistentImageConsultation> findAllByProjectAndUser(Long project, Long user, PageRequest request);

    List<PersistentImageConsultation> findAllByCreatedGreaterThanAndProjectConnectionOrderByCreatedDesc(
        Date created,
        Long activityId
    );

    void deleteAllByImage(Long id);

    long countByProjectAndUser(Long project, Long user);
}
