package be.cytomine.authorization.image;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.authorization.CRUDAuthorizationTest;
import be.cytomine.domain.image.NestedImageInstance;
import be.cytomine.service.image.NestedImageInstanceService;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.security.acls.domain.BasePermission.READ;

@AutoConfigureMockMvc
@SpringBootTest(classes = CytomineCoreApplication.class)
@Transactional
public class NestedImageInstanceAuthorizationTest extends CRUDAuthorizationTest {

    private NestedImageInstance nestedImageInstance = null;

    @Autowired
    NestedImageInstanceService nestedImageInstanceService;

    @Autowired
    BasicInstanceBuilder builder;

    @BeforeEach
    public void before() throws Exception {
        if (nestedImageInstance == null) {
            nestedImageInstance = builder.givenANestedImageInstance();
            initACL(nestedImageInstance.container());
        }
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void admin_can_list() {
        assertThat(nestedImageInstanceService.list(nestedImageInstance.getParent())).contains(nestedImageInstance);
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void user_can_list() {
        assertThat(nestedImageInstanceService.list(nestedImageInstance.getParent())).contains(nestedImageInstance);
    }

    @Test
    @WithMockUser(username = USER_NO_ACL)
    public void user_with_no_read_cannot_list() {
        expectForbidden(() -> nestedImageInstanceService.list(nestedImageInstance.getParent()));
    }


    @Override
    public void whenIGetDomain() {
        nestedImageInstanceService.get(nestedImageInstance.getId());
    }

    @Override
    protected void whenIAddDomain() {
        NestedImageInstance nestedImageInstance = builder.givenANotPersistedNestedImageInstance();
        nestedImageInstance.setProject(this.nestedImageInstance.getProject());
        nestedImageInstance.setBaseImage(builder.givenAnAbstractImage());
        nestedImageInstanceService.add(nestedImageInstance.toJsonObject());
    }

    @Override
    public void whenIEditDomain() {
        nestedImageInstanceService.update(nestedImageInstance, nestedImageInstance.toJsonObject());
    }

    @Override
    protected void whenIDeleteDomain() {
        NestedImageInstance nestedImageInstanceToDelete = nestedImageInstance;
        nestedImageInstanceService.delete(nestedImageInstanceToDelete, null, null, true);
    }

    @Override
    protected Optional<Permission> minimalPermissionForCreate() {
        return Optional.of(READ);
    }

    @Override
    protected Optional<Permission> minimalPermissionForDelete() {
        return Optional.of(READ);
    }

    @Override
    protected Optional<Permission> minimalPermissionForEdit() {
        return Optional.of(READ);
    }


    @Override
    protected Optional<String> minimalRoleForCreate() {
        return Optional.of("ROLE_USER");
    }

    @Override
    protected Optional<String> minimalRoleForDelete() {
        return Optional.of("ROLE_USER");
    }

    @Override
    protected Optional<String> minimalRoleForEdit() {
        return Optional.of("ROLE_USER");
    }
}
