package be.cytomine.repositorynosql.social;

import java.util.Date;

import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import be.cytomine.domain.social.PersistentUserPosition;

@SuppressWarnings("checkstyle:all") // This file will be refactored in https://github.com/cytomine/cytomine/issues/625
@Repository
public interface PersistentUserPositionRepository extends MongoRepository<PersistentUserPosition, Long> {

    @Aggregation(pipeline = {"{$match: {project: ?0, user: ?1, image: ?2, $and : [{created: {$gte: ?4}},{created: {$lte: ?3}}]}},{$sort: {created: 1}},{$project: {dateInMillis: {$subtract: {'$created', ?5}}}}"})
    AggregationResults retrieve(Long project, Long user, Long image, Date before, Date after, Date firstDate);

    void deleteAllByImage(Long id);
}
