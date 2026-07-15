package org.cytomine.repository.persistence;

import java.util.Optional;

import org.cytomine.repository.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByIdAndDeletedNull(long id);

    Optional<UserEntity> findByUsernameLikeIgnoreCase(String query);
}
