package be.cytomine.authorization;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.test.context.support.WithMockUser;

@Transactional
public abstract class CRUDAuthorizationTest extends CRDAuthorizationTest {

    protected abstract void whenIEditDomain();

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void admin_can_edit_domain() {
        if (minimalRoleForEdit().isPresent() && minimalRoleForEdit().get().equals("CREATOR")) {
            expectOK(this::whenIEditDomain);
        } else if (isPermissionRoleForbidden(minimalRoleForEdit(), "ROLE_SUPERADMIN")) {
            expectForbidden(this::whenIEditDomain);
        } else {
            expectOK(this::whenIEditDomain);
        }
    }

    @Test
    @WithMockUser(username = USER_ACL_ADMIN)
    public void user_with_admin_permission_edit_domain() {
        if (minimalRoleForEdit().isPresent() && minimalRoleForEdit().get().equals("CREATOR")) {
            expectForbidden(this::whenIEditDomain);
        } else if (isPermissionForbidden(minimalPermissionForEdit(), BasePermission.ADMINISTRATION)) {
            expectForbidden(this::whenIEditDomain);
        } else {
            expectOK(this::whenIEditDomain);
        }
    }

    @Test
    @WithMockUser(username = USER_ACL_DELETE)
    public void user_with_delete_permission_edit_domain() {
        if (minimalRoleForEdit().isPresent() && minimalRoleForEdit().get().equals("CREATOR")) {
            expectForbidden(this::whenIEditDomain);
        } else if (isPermissionForbidden(minimalPermissionForEdit(), BasePermission.DELETE)) {
            expectForbidden(this::whenIEditDomain);
        } else {
            expectOK(this::whenIEditDomain);
        }
    }

    @Test
    @WithMockUser(username = USER_ACL_CREATE)
    public void user_with_create_permission_edit_domain() {
        if (minimalRoleForEdit().isPresent() && minimalRoleForEdit().get().equals("CREATOR")) {
            expectForbidden(this::whenIEditDomain);
        } else if (isPermissionForbidden(minimalPermissionForEdit(), BasePermission.CREATE)) {
            expectForbidden(this::whenIEditDomain);
        } else {
            expectOK(this::whenIEditDomain);
        }
    }

    @Test
    @WithMockUser(username = USER_ACL_WRITE)
    public void user_with_write_permission_edit_domain() {
        if (minimalRoleForEdit().isPresent() && minimalRoleForEdit().get().equals("CREATOR")) {
            expectForbidden(this::whenIEditDomain);
        } else if (isPermissionForbidden(minimalPermissionForEdit(), BasePermission.WRITE)) {
            expectForbidden(this::whenIEditDomain);
        } else {
            expectOK(this::whenIEditDomain);
        }
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void user_with_read_permission_edit_domain() {
        if (minimalRoleForEdit().isPresent() && minimalRoleForEdit().get().equals("CREATOR")) {
            expectForbidden(this::whenIEditDomain);
        } else if (isPermissionForbidden(minimalPermissionForEdit(), BasePermission.READ)) {
            expectForbidden(this::whenIEditDomain);
        } else {
            expectOK(this::whenIEditDomain);
        }
    }

    @Test
    @WithMockUser(username = USER_NO_ACL)
    public void user_without_permission_edit_domain() {
        if (minimalRoleForEdit().isPresent() && minimalRoleForEdit().get().equals("CREATOR")) {
            expectForbidden(this::whenIEditDomain);
        } else if (isPermissionForbidden(minimalPermissionForEdit(), null)) {
            expectForbidden(this::whenIEditDomain);
        } else {
            expectOK(this::whenIEditDomain);
        }
    }

    @Test
    @WithMockUser(username = GUEST)
    public void guest_edit_domain() {
        if (minimalRoleForEdit().isPresent() && minimalRoleForEdit().get().equals("CREATOR")) {
            expectForbidden(this::whenIEditDomain);
        } else if (isPermissionRoleForbidden(minimalRoleForEdit(), "ROLE_GUEST")) {
            expectForbidden(this::whenIEditDomain);
        } else {
            expectOK(this::whenIEditDomain);
        }
    }
}
