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
import be.cytomine.domain.image.AbstractSlice;
import be.cytomine.service.image.AbstractSliceService;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.security.acls.domain.BasePermission.WRITE;

@AutoConfigureMockMvc
@SpringBootTest(classes = CytomineCoreApplication.class)
@Transactional
public class AbstractSliceAuthorizationTest extends CRUDAuthorizationTest {

    // We need more flexibility:

    private AbstractSlice abstractSlice = null;

    @Autowired
    AbstractSliceService abstractSliceService;

    @Autowired
    BasicInstanceBuilder builder;

    @BeforeEach
    public void before() throws Exception {
        if (abstractSlice == null) {
            abstractSlice = builder.givenAnAbstractSlice();
            initACL(abstractSlice.container());
        }
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void admin_can_list_abstract_slices() {
        assertThat(abstractSliceService.list(abstractSlice.getImage())).contains(abstractSlice);
        assertThat(abstractSliceService.list(abstractSlice.getUploadedFile())).contains(abstractSlice);
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void user_can_list_all_abstract_slices() {
        assertThat(abstractSliceService.list(abstractSlice.getImage())).contains(abstractSlice);
        assertThat(abstractSliceService.list(abstractSlice.getUploadedFile())).contains(abstractSlice);
    }

    @Test
    @WithMockUser(username = USER_NO_ACL)
    public void user_no_acl_cannot_list_all_abstract_slices() {
        expectForbidden(() -> abstractSliceService.list(abstractSlice.getImage()));
        expectForbidden(() -> abstractSliceService.list(abstractSlice.getUploadedFile()));
    }


    @Override
    public void whenIGetDomain() {
        abstractSliceService.get(abstractSlice.getId());
    }

    @Override
    protected void whenIAddDomain() {
        abstractSliceService.add(builder.givenANotPersistedAbstractSlice().toJsonObject());
    }

    @Override
    public void whenIEditDomain() {
        abstractSliceService.update(abstractSlice, abstractSlice.toJsonObject());
    }

    @Override
    protected void whenIDeleteDomain() {
        AbstractSlice abstractSliceToDelete = abstractSlice;
        abstractSliceService.delete(abstractSliceToDelete, null, null, true);
    }

    @Override
    protected Optional<Permission> minimalPermissionForCreate() {
        return Optional.empty();
    }

    @Override
    protected Optional<Permission> minimalPermissionForDelete() {
        return Optional.of(WRITE);
    }

    @Override
    protected Optional<Permission> minimalPermissionForEdit() {
        return Optional.of(WRITE);
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
