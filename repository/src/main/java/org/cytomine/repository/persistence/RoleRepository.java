package org.cytomine.repository.persistence;

import java.util.Optional;
import java.util.Set;

import org.cytomine.repository.persistence.entity.RoleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByIdAndDeletedNull(long id);

    Page<RoleEntity> findAllRoles(Pageable pageable);
}
