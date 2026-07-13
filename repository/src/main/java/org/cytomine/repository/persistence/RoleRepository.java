package org.cytomine.repository.persistence;

import java.util.Optional;
import java.util.Set;

import org.cytomine.repository.persistence.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    Optional<RoleEntity> findByIdAndDeletedNull(long id);

    Optional<RoleEntity> findByAuthorityAndDeletedNull(String authority);

    Set<RoleEntity> findAllByAuthorityInAndDeletedNull(Set<String> authorities);

}
