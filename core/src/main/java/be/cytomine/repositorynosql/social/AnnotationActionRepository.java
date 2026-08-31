package be.cytomine.repositorynosql.social;

import java.util.Date;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import be.cytomine.domain.social.AnnotationAction;

@Repository
public interface AnnotationActionRepository extends MongoRepository<AnnotationAction, Long> {

    Long countByProject(Long project);

    Long countByProjectAndCreatedAfter(Long project, Date createdMin);

    Long countByProjectAndCreatedBefore(Long project, Date createdMax);

    Long countByProjectAndCreatedBetween(Long project, Date createdMin, Date createdMax);

    void deleteAllByImage(Long id);

    Long countByProjectAndUserAndAction(Long project, Long user, String action);
}
