package org.cytomine.repository.persistence;

import org.cytomine.repository.persistence.entity.CommandEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommandRepository extends JpaRepository<CommandEntity, Long> {
}
