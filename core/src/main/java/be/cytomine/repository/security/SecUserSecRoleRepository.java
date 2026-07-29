package be.cytomine.repository.security;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import be.cytomine.domain.security.SecRole;
import be.cytomine.domain.security.SecUserSecRole;
import be.cytomine.domain.security.User;

/**
 * Spring Data JPA repository for the user entity.
 */
@Repository
public interface SecUserSecRoleRepository
    extends JpaRepository<SecUserSecRole, Long>, JpaSpecificationExecutor<SecUserSecRole> {

    @Query("select distinct s.secRole from SecUserSecRole s where s.secUser = ?1")
    Set<SecRole> findAllRoleByUser(User user);

    List<SecUserSecRole> findAllBySecUser(User user);

    Optional<SecUserSecRole> findBySecUserAndSecRole(User user, SecRole secRole);
}
