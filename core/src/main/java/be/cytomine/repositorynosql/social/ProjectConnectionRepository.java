package be.cytomine.repositorynosql.social;

// CHECKSTYLE:OFF
// TODO: This file will be refactored - see https://github.com/cytomine/cytomine/issues/625

import java.util.Date;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import be.cytomine.domain.social.PersistentConnection;

/**
 * Spring Data JPA repository for the user entity.
 */
@Repository
public interface ProjectConnectionRepository extends MongoRepository<PersistentConnection, String> {

    Optional<PersistentConnection> findByUserAndProjectAndCreatedLessThan(
        Long user,
        Long project,
        Date created,
        PageRequest pageRequest
    );


    @Aggregation(pipeline = {
        "{$match: {project: ?0, user: ?1, $and : [{created: {$gte: ?3}},{created: {$lte: ?2}}]}},{$sort: {created: 1}},{$project: {dateInMillis: {$subtract: {'$created', ?4}}}}"
    })
    AggregationResults retrieve(Long project, Long user, Date before, Date after, Date firstDate);

}
