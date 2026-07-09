package org.cytomine.repository.persistence;

import java.util.Optional;
import java.util.Set;

import org.cytomine.repository.persistence.entity.UserRoleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRoleEntity, Long> {

    Optional<UserRoleEntity> findByIdAndDeletedNull(long id);

    Page<UserRoleEntity> findAllBySecUserIdAndDeletedNull(long secUserId, Pageable pageable);

    Set<UserRoleEntity> findAllBySecUserIdAndDeletedNull(long secUserId);

    Optional<UserRoleEntity> findBySecUserIdAndSecRoleIdAndDeletedNull(long secUserId, long secRoleId);

}
