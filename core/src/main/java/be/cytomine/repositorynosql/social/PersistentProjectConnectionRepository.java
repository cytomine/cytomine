package be.cytomine.repositorynosql.social;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import be.cytomine.domain.social.PersistentProjectConnection;

@SuppressWarnings("checkstyle:all") // This file will be refactored in https://github.com/cytomine/cytomine/issues/625
@Repository
public interface PersistentProjectConnectionRepository extends MongoRepository<PersistentProjectConnection, Long> {

    Page<PersistentProjectConnection> findAllByUserAndProjectAndCreatedLessThan(Long user, Long project, Date created, PageRequest pageRequest);

    
    @Aggregation(pipeline = {"{$match: {project: ?0, user: ?1, $and : [{created: {$gte: ?3}},{created: {$lte: ?2}}]}},{$sort: {created: 1}},{$project: {dateInMillis: {$subtract: {'$created', ?4}}}}"})
    AggregationResults test(Long project, Long user, Date before, Date after, Date firstDate);

    Page<PersistentProjectConnection> findAllByUserAndProject(Long user, Long project, PageRequest pageRequest);

    @Aggregation(pipeline = {"{$match: {project: ?0}},{$sort: {?1: ?2}},{$group: {_id : '$user', created : {$max :'$created'}}}"})
    AggregationResults retrieve(Long project, String sortProperty, Integer sortDirection);

    Long countAllByProjectAndUser(Long project, Long user);

    @Aggregation(pipeline = {"{$group: {_id : '$project', total : {$sum :1}}}"})
    AggregationResults countConnectionByProject();

    Long countByProject(Long project);

    Long countByProjectAndCreatedAfter(Long project, Date createdMin);

    Long countByProjectAndCreatedBefore(Long project, Date createdMax);

    Long countByProjectAndCreatedBetween(Long project, Date createdMin, Date createdMax);
}
