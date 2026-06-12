package be.cytomine.repositorynosql.social;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import be.cytomine.domain.social.LastConnection;


@Repository
public interface LastConnectionRepository extends MongoRepository<LastConnection, Long> {

    List<LastConnection> findByProjectAndUser(Long project, Long user);

    List<LastConnection> findByUserOrderByCreatedDesc(Long user);

    List<LastConnection> findAllByCreatedAfter(Date date);

    List<LastConnection> findAllByProjectAndCreatedAfter(Long project, Date date);
}
