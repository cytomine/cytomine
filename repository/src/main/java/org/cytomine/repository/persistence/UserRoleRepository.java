package org.cytomine.repository.persistence;

import java.util.List;
import java.util.Optional;

import org.cytomine.repository.persistence.entity.UserRoleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRoleRepository extends JpaRepository<UserRoleEntity, Long> {

    Optional<UserRoleEntity> findByIdAndDeletedNull(long id);

    Page<UserRoleEntity> findAllBySecUserIdAndDeletedNull(long secUserId, Pageable pageable);

    List<UserRoleEntity> findAllBySecUserIdAndDeletedNull(long secUserId);

    Optional<UserRoleEntity> findBySecUserIdAndSecRoleIdAndDeletedNull(long secUserId, long secRoleId);

    @Query(value = """
        SELECT ur.* FROM sec_user_sec_role ur
        JOIN sec_role r ON r.id = ur.sec_role_id
        WHERE ur.sec_user_id = :userId AND ur.deleted IS NULL
        ORDER BY CASE r.authority
            WHEN 'ROLE_SUPER_ADMIN' THEN 4
            WHEN 'ROLE_ADMIN' THEN 3
            WHEN 'ROLE_USER' THEN 2
            WHEN 'ROLE_GUEST' THEN 1
            ELSE 0
        END DESC
        LIMIT 1
        """, nativeQuery = true)
    Optional<UserRoleEntity> findHighestBySecUserId(@Param("userId") long userId);
}
