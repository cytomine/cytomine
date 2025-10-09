package be.cytomine.appengine.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import be.cytomine.appengine.models.store.AppStore;

@Repository
public interface AppStoreRepository extends JpaRepository<AppStore, UUID> {
    Optional<AppStore> findByDefaultStoreIsTrue();

    Optional<AppStore> findByNameAndHost(String name, String host);

    void deleteById(UUID id);
}
