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
    public void admin_get_domain() {
        expectOK(this::whenIGetDomain);
    }

    @Test
    @WithMockUser(username = USER_ACL_ADMIN)
    public void user_with_admin_permission_get_domain() {
        expectOK(this::whenIGetDomain);
    }

    @Test
    @WithMockUser(username = USER_ACL_DELETE)
    public void user_with_delete_permission_get_domain() {
        expectOK(this::whenIGetDomain);
    }

    @Test
    @WithMockUser(username = USER_ACL_CREATE)
    public void user_with_create_permission_get_domain() {
        expectOK(this::whenIGetDomain);
    }

    @Test
    @WithMockUser(username = USER_ACL_WRITE)
    public void user_with_write_permission_domain() {
        expectOK(this::whenIGetDomain);
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void user_with_read_permission_get_domain() {
        expectOK(this::whenIGetDomain);
    }

    @Test
    @WithMockUser(username = USER_NO_ACL)
    public void user_without_permission_get_domain() {
        expectForbidden(this::whenIGetDomain);
    }

    @Test
    @WithMockUser(username = GUEST)
    public void guest_with_permission_get_domain() {
        expectOK(this::whenIGetDomain);
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    @Disabled("This test does not work, the returned entity is a 500, but expectOK() ignores that")
    public void admin_add_domain() {
        expectOK(this::whenIAddDomain);
    }

    @Test
    @Disabled("This test does not work, the returned entity is a 500, but expectOK() ignores that")
    @WithMockUser(username = USER_ACL_ADMIN)
    public void user_with_admin_permission_add_domain() {
        if (isPermissionForbidden(minimalPermissionForCreate(), BasePermission.ADMINISTRATION)) {
            expectForbidden(this::whenIAddDomain);
        } else {
            expectOK(this::whenIAddDomain);
        }
    }

    @Test
    @WithMockUser(username = USER_ACL_DELETE)
    @Disabled("This test does not work, the returned entity is a 500, but expectOK() ignores that")
    public void user_with_delete_permission_add_domain() {
        if (isPermissionForbidden(minimalPermissionForCreate(), BasePermission.DELETE)) {
            expectForbidden(this::whenIAddDomain);
        } else {
            expectOK(this::whenIAddDomain);
        }
    }

    @Test
    @WithMockUser(username = USER_ACL_WRITE)
    @Disabled("This test does not work, the returned entity is a 500, but expectOK() ignores that")
    public void user_with_write_permission_add_domain() {
        if (isPermissionForbidden(minimalPermissionForCreate(), BasePermission.WRITE)) {
            expectForbidden(this::whenIAddDomain);
        } else {
            expectOK(this::whenIAddDomain);
        }
    }

    @Test
    @WithMockUser(username = USER_ACL_CREATE)
    @Disabled("This test does not work, the returned entity is a 500, but expectOK() ignores that")
    public void user_with_create_permission_add_domain() {
        if (isPermissionForbidden(minimalPermissionForCreate(), BasePermission.CREATE)) {
            expectForbidden(this::whenIAddDomain);
        } else {
            expectOK(this::whenIAddDomain);
        }
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    @Disabled("This test does not work, the returned entity is a 500, but expectOK() ignores that")
    public void user_with_read_permission_add_domain() {
        if (isPermissionForbidden(minimalPermissionForCreate(), BasePermission.READ)) {
            expectForbidden(this::whenIAddDomain);
        } else {
            expectOK(this::whenIAddDomain);
        }
    }

    @Test
    @WithMockUser(username = USER_NO_ACL)
    public void user_without_permission_add_domain() {
        if (isPermissionForbidden(minimalPermissionForCreate(), null)) {
            expectForbidden(this::whenIAddDomain);
        } else {
            expectOK(this::whenIAddDomain);
        }
    }

    @Test
    @WithMockUser(username = GUEST)
    public void guest_add_domain() {
        if (isPermissionRoleForbidden(minimalRoleForCreate(), "ROLE_GUEST")) {
            expectForbidden(this::whenIAddDomain);
        } else {
            expectOK(this::whenIAddDomain);
        }
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    @Disabled("This test does not work, the returned entity is a 500, but expectOK() ignores that")
    public void admin_delete_domain() {
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
    public void user_with_admin_permission_delete_domain() {
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
    public void user_with_delete_permission_delete_domain() {
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
    public void user_with_create_permission_delete_domain() {
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
    public void user_with_write_permission_delete_domain() {
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
    public void user_with_read_permission_delete_domain() {
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
    public void user_without_permission_delete_domain() {
        if (isPermissionForbidden(minimalPermissionForDelete(), null)) {
            expectForbidden(this::whenIDeleteDomain);
        } else {
            expectOK(this::whenIDeleteDomain);
        }
    }

    @Test
    @WithMockUser(username = GUEST)
    public void guest_delete_domain() {
        if (isPermissionRoleForbidden(minimalRoleForDelete(), "ROLE_GUEST")) {
            expectForbidden(this::whenIDeleteDomain);
        } else {
            expectOK(this::whenIDeleteDomain);
        }
    }
}
