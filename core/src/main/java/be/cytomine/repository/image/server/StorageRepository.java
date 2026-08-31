package be.cytomine.repository.image.server;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import be.cytomine.domain.image.server.Storage;
import be.cytomine.domain.security.User;

/**
 * Spring Data JPA repository for the user entity.
 */
@Repository
public interface StorageRepository extends JpaRepository<Storage, Long>, JpaSpecificationExecutor<Storage> {

    List<Storage> findAllByUser(User user);

}
