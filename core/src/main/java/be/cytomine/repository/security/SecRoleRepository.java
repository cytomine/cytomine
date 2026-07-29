package be.cytomine.repository.security;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import be.cytomine.domain.security.SecRole;

import static be.cytomine.common.repository.model.Role.ROLE_ADMIN;
import static be.cytomine.common.repository.model.Role.ROLE_GUEST;
import static be.cytomine.common.repository.model.Role.ROLE_SUPER_ADMIN;
import static be.cytomine.common.repository.model.Role.ROLE_USER;

/**
 * Spring Data JPA repository for the user entity.
 */
@Repository
public interface SecRoleRepository extends JpaRepository<SecRole, Long>, JpaSpecificationExecutor<SecRole> {

    default SecRole getGuest() {
        return getByAuthority(ROLE_GUEST.toString());
    }

    default SecRole getUser() {
        return getByAuthority(ROLE_USER.toString());
    }

    default SecRole getAdmin() {
        return getByAuthority(ROLE_ADMIN.toString());
    }

    default SecRole getSuperAdmin() {
        return getByAuthority(ROLE_SUPER_ADMIN.toString());
    }

    SecRole getByAuthority(String authority);

    Optional<SecRole> findByAuthority(String authority);

    default SecRole createIfNotExist(String authority) {
        return findByAuthority(authority).orElseGet(() -> {
            SecRole role = new SecRole();
            role.setAuthority(authority);
            return save(role);
        });
    }

}
