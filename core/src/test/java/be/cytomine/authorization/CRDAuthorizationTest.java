package be.cytomine.authorization;

import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.test.context.support.WithMockUser;

import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.config.MongoTestConfiguration;

@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
@Transactional
public abstract class CRDAuthorizationTest extends AbstractAuthorizationTest {
    protected static List<String> rolePerOrder = List.of(
        "ROLE_GUEST",
        "ROLE_USER",
        "CREATOR",
        "ROLE_ADMIN",
        "ROLE_SUPERADMIN"
    );


    boolean isPermissionForbidden(Optional<Permission> permissionRequired, Permission permission) {
        return permissionRequired.isPresent()
            && (permission == null || permissionRequired.get().getMask() > permission.getMask());
    }

    boolean isPermissionRoleForbidden(Optional<String> roleRequired, String currentRole) {
        if (roleRequired.isEmpty()) {
            return false;
        } else {
            int indexRoleRequired = rolePerOrder.indexOf(roleRequired.get());
            int indexCurrenRole = rolePerOrder.indexOf(currentRole);
            if (indexRoleRequired == -1 || indexCurrenRole == -1) {
                throw new RuntimeException("Cannot find index for role " + roleRequired.get() + " or " + currentRole);
            }
            return indexCurrenRole < indexRoleRequired;
        }
    }

    protected abstract void whenIGetDomain();

    protected abstract void whenIAddDomain();

    protected abstract void whenIDeleteDomain();

    protected abstract Optional<Permission> minimalPermissionForCreate();

    protected abstract Optional<Permission> minimalPermissionForDelete();

    protected abstract Optional<Permission> minimalPermissionForEdit();

    protected abstract Optional<String> minimalRoleForCreate();

    protected abstract Optional<String> minimalRoleForDelete();

    protected abstract Optional<String> minimalRoleForEdit();

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void adminGetDomain() {
        expectOK(this::whenIGetDomain);
    }

    @Test
    @WithMockUser(username = USER_ACL_ADMIN)
    public void userWithAdminPermissionGetDomain() {
        expectOK(this::whenIGetDomain);
    }

    @Test
    @WithMockUser(username = USER_ACL_DELETE)
    public void userWithDeletePermissionGetDomain() {
        expectOK(this::whenIGetDomain);
    }

    @Test
    @WithMockUser(username = USER_ACL_CREATE)
    public void userWithCreatePermissionGetDomain() {
        expectOK(this::whenIGetDomain);
    }

    @Test
    @WithMockUser(username = USER_ACL_WRITE)
    public void userWithWritePermissionDomain() {
        expectOK(this::whenIGetDomain);
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userWithReadPermissionGetDomain() {
        expectOK(this::whenIGetDomain);
    }

    @Test
    @WithMockUser(username = USER_NO_ACL)
    public void userWithoutPermissionGetDomain() {
        expectForbidden(this::whenIGetDomain);
    }

    @Test
    @WithMockUser(username = GUEST)
    public void guestWithPermissionGetDomain() {
        expectOK(this::whenIGetDomain);
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    @Disabled("This test does not work, the returned entity is a 500, but expectOK() ignores that")
    public void adminAddDomain() {
        expectOK(this::whenIAddDomain);
    }

    @Test
    @Disabled("This test does not work, the returned entity is a 500, but expectOK() ignores that")
    @WithMockUser(username = USER_ACL_ADMIN)
    public void userWithAdminPermissionAddDomain() {
        if (isPermissionForbidden(minimalPermissionForCreate(), BasePermission.ADMINISTRATION)) {
            expectForbidden(this::whenIAddDomain);
        } else {
            expectOK(this::whenIAddDomain);
        }
    }

    @Test
    @WithMockUser(username = USER_ACL_DELETE)
    @Disabled("This test does not work, the returned entity is a 500, but expectOK() ignores that")
    public void userWithDeletePermissionAddDomain() {
        if (isPermissionForbidden(minimalPermissionForCreate(), BasePermission.DELETE)) {
            expectForbidden(this::whenIAddDomain);
        } else {
            expectOK(this::whenIAddDomain);
        }
    }

    @Test
    @WithMockUser(username = USER_ACL_WRITE)
    @Disabled("This test does not work, the returned entity is a 500, but expectOK() ignores that")
    public void userWithWritePermissionAddDomain() {
        if (isPermissionForbidden(minimalPermissionForCreate(), BasePermission.WRITE)) {
            expectForbidden(this::whenIAddDomain);
        } else {
            expectOK(this::whenIAddDomain);
        }
    }

    @Test
    @WithMockUser(username = USER_ACL_CREATE)
    @Disabled("This test does not work, the returned entity is a 500, but expectOK() ignores that")
    public void userWithCreatePermissionAddDomain() {
        if (isPermissionForbidden(minimalPermissionForCreate(), BasePermission.CREATE)) {
            expectForbidden(this::whenIAddDomain);
        } else {
            expectOK(this::whenIAddDomain);
        }
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    @Disabled("This test does not work, the returned entity is a 500, but expectOK() ignores that")
    public void userWithReadPermissionAddDomain() {
        if (isPermissionForbidden(minimalPermissionForCreate(), BasePermission.READ)) {
            expectForbidden(this::whenIAddDomain);
        } else {
            expectOK(this::whenIAddDomain);
        }
    }

    @Test
    @WithMockUser(username = USER_NO_ACL)
    public void userWithoutPermissionAddDomain() {
        if (isPermissionForbidden(minimalPermissionForCreate(), null)) {
            expectForbidden(this::whenIAddDomain);
        } else {
            expectOK(this::whenIAddDomain);
        }
    }

    @Test
    @WithMockUser(username = GUEST)
    public void guestAddDomain() {
        if (isPermissionRoleForbidden(minimalRoleForCreate(), "ROLE_GUEST")) {
            expectForbidden(this::whenIAddDomain);
        } else {
            expectOK(this::whenIAddDomain);
        }
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    @Disabled("This test does not work, the returned entity is a 500, but expectOK() ignores that")
    public void adminDeleteDomain() {
        if (minimalRoleForDelete().isPresent() && minimalRoleForDelete().get().equals("CREATOR")) {
            expectOK(this::whenIDeleteDomain);
        } else if (isPermissionRoleForbidden(minimalRoleForDelete(), "ROLE_SUPERADMIN")) {
            expectForbidden(this::whenIDeleteDomain);
        } else {
            expectOK(this::whenIDeleteDomain);
        }
    }

    @Test
    @Disabled("This test does not work, the returned entity is a 500, but expectOK() ignores that")
    @WithMockUser(username = USER_ACL_ADMIN)
    public void userWithAdminPermissionDeleteDomain() {
        if (minimalRoleForDelete().isPresent() && minimalRoleForDelete().get().equals("CREATOR")) {
            expectForbidden(this::whenIDeleteDomain);
        } else if (isPermissionForbidden(minimalPermissionForDelete(), BasePermission.ADMINISTRATION)) {
            expectForbidden(this::whenIDeleteDomain);
        } else {
            expectOK(this::whenIDeleteDomain);
        }
    }

    @Test
    @WithMockUser(username = USER_ACL_DELETE)
    public void userWithDeletePermissionDeleteDomain() {
        if (minimalRoleForDelete().isPresent() && minimalRoleForDelete().get().equals("CREATOR")) {
            expectForbidden(this::whenIDeleteDomain);
        } else if (isPermissionForbidden(minimalPermissionForDelete(), BasePermission.DELETE)) {
            expectForbidden(this::whenIDeleteDomain);
        } else {
            expectOK(this::whenIDeleteDomain);
        }
    }

    @Test
    @WithMockUser(username = USER_ACL_CREATE)
    public void userWithCreatePermissionDeleteDomain() {
        if (minimalRoleForDelete().isPresent() && minimalRoleForDelete().get().equals("CREATOR")) {
            expectForbidden(this::whenIDeleteDomain);
        } else if (isPermissionForbidden(minimalPermissionForDelete(), BasePermission.CREATE)) {
            expectForbidden(this::whenIDeleteDomain);
        } else {
            expectOK(this::whenIDeleteDomain);
        }
    }

    @Test
    @WithMockUser(username = USER_ACL_WRITE)
    public void userWithWritePermissionDeleteDomain() {
        if (minimalRoleForDelete().isPresent() && minimalRoleForDelete().get().equals("CREATOR")) {
            expectForbidden(this::whenIDeleteDomain);
        } else if (isPermissionForbidden(minimalPermissionForDelete(), BasePermission.WRITE)) {
            expectForbidden(this::whenIDeleteDomain);
        } else {
            expectOK(this::whenIDeleteDomain);
        }
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userWithReadPermissionDeleteDomain() {
        if (minimalRoleForDelete().isPresent() && minimalRoleForDelete().get().equals("CREATOR")) {
            expectForbidden(this::whenIDeleteDomain);
        } else if (isPermissionForbidden(minimalPermissionForDelete(), BasePermission.READ)) {
            expectForbidden(this::whenIDeleteDomain);
        } else {
            expectOK(this::whenIDeleteDomain);
        }
    }

    @Test
    @WithMockUser(username = USER_NO_ACL)
    public void userWithoutPermissionDeleteDomain() {
        if (isPermissionForbidden(minimalPermissionForDelete(), null)) {
            expectForbidden(this::whenIDeleteDomain);
        } else {
            expectOK(this::whenIDeleteDomain);
        }
    }

    @Test
    @WithMockUser(username = GUEST)
    public void guestDeleteDomain() {
        if (isPermissionRoleForbidden(minimalRoleForDelete(), "ROLE_GUEST")) {
            expectForbidden(this::whenIDeleteDomain);
        } else {
            expectOK(this::whenIDeleteDomain);
        }
    }
}
