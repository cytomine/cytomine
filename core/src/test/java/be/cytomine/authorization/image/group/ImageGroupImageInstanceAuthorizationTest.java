package be.cytomine.authorization.image.group;

import java.util.Optional;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.test.context.support.WithMockUser;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.authorization.CRDAuthorizationTest;
import be.cytomine.domain.image.group.ImageGroup;
import be.cytomine.domain.image.group.ImageGroupImageInstance;
import be.cytomine.domain.project.EditingMode;
import be.cytomine.service.image.group.ImageGroupImageInstanceService;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.security.acls.domain.BasePermission.READ;

@AutoConfigureMockMvc
@SpringBootTest(classes = CytomineCoreApplication.class)
@Transactional
public class ImageGroupImageInstanceAuthorizationTest extends CRDAuthorizationTest {

    private ImageGroupImageInstance igii = null;

    @Autowired
    BasicInstanceBuilder builder;

    @Autowired
    ImageGroupImageInstanceService imageGroupImageInstanceService;

    @BeforeEach
    public void before() throws Exception {
        if (igii == null) {
            igii = builder.givenAnImageGroupImageInstance();
            initACL(igii.container());
        }
        ImageGroup imageGroup = igii.getGroup();
        imageGroup.getProject().setMode(EditingMode.CLASSIC);
    }

    @Override
    protected void whenIGetDomain() {
        imageGroupImageInstanceService.get(igii.getGroup(), igii.getImage());
    }

    @Override
    protected void whenIAddDomain() {
        imageGroupImageInstanceService.add(builder.givenANotPersistedImageGroupImageInstance(
            igii.getGroup(),
            igii.getImage()
        ).toJsonObject());
    }

    @Override
    protected void whenIDeleteDomain() {
        imageGroupImageInstanceService.delete(igii, null, null, true);
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

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void adminCanListImagegroupImageinstanceByImagegroup() {
        assertThat(imageGroupImageInstanceService.list(igii.getGroup())).contains(igii);
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void adminCanListImagegroupImageinstanceByImageinstance() {
        assertThat(imageGroupImageInstanceService.list(igii.getImage())).contains(igii);
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userCanListImagegroupImageinstanceByImagegroup() {
        assertThat(imageGroupImageInstanceService.list(igii.getGroup())).contains(igii);
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userCanListImagegroupImageinstanceByImageinstance() {
        assertThat(imageGroupImageInstanceService.list(igii.getImage())).contains(igii);
    }
}
