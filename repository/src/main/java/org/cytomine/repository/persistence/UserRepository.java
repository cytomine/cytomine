package org.cytomine.repository.persistence;

import java.util.Optional;
import java.util.Set;

import org.cytomine.repository.persistence.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByIdAndDeletedNull(long id);

    Page<UserEntity> findByIdIn(Set<Long> ids, Pageable pageable);

    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByPublicKeyAndEnabled(String publicKey, boolean enabled);
}
