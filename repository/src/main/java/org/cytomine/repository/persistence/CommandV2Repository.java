package org.cytomine.repository.persistence;

import java.util.Optional;
import java.util.UUID;

import org.cytomine.repository.persistence.entity.CommandV2Entity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommandV2Repository extends JpaRepository<CommandV2Entity, UUID> {

    Optional<CommandV2Entity> findById(UUID id);
}
