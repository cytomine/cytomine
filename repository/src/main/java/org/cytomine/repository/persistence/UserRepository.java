package org.cytomine.repository.persistence;

import java.util.Optional;
import java.util.Set;

import org.cytomine.repository.persistence.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import be.cytomine.common.repository.model.command.payload.response.UserResponse;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByIdAndDeletedNull(long id);

    Optional<UserEntity> findByUsernameLikeIgnoreCase(String query);

    Page<UserResponse> findByIdsIn(Set<Long> ids, Pageable pageable);
}
