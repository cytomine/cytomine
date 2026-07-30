package be.cytomine.repositorynosql.social;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import be.cytomine.domain.social.LastUserPosition;

@Repository
public interface LastUserPositionRepository extends MongoRepository<LastUserPosition, Long> {
    void deleteAllByImage(Long id);
}