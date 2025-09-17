package be.cytomine.appengine.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import be.cytomine.appengine.models.task.Checksum;

@Repository
public interface ChecksumRepository extends JpaRepository<Checksum, UUID> {
    Checksum findByReference(String reference);
}
