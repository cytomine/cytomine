package be.cytomine.repositorynosql.social;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import be.cytomine.domain.social.PersistentConnection;

@Repository
public interface PersistentConnectionRepository extends MongoRepository<PersistentConnection, Long> {}
