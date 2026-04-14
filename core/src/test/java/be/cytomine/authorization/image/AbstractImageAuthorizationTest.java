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
import be.cytomine.domain.image.AbstractImage;
import be.cytomine.service.image.AbstractImageService;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.security.acls.domain.BasePermission.WRITE;

@AutoConfigureMockMvc
@SpringBootTest(classes = CytomineCoreApplication.class)
@Transactional
public class AbstractImageAuthorizationTest extends CRUDAuthorizationTest {

    // We need more flexibility:

    private AbstractImage abstractImage = null;

    @Autowired
    AbstractImageService abstractImageService;

    @Autowired
    BasicInstanceBuilder builder;

    @BeforeEach
    public void before() throws Exception {
        if (abstractImage == null) {
            Long start = System.currentTimeMillis();
            abstractImage = builder.givenAnAbstractImage();
            System.out.println("EXECUTION given_an_abstract_image:" + (System.currentTimeMillis() - start));
            start = System.currentTimeMillis();
            System.out.println("EXECUTION initUser:" + (System.currentTimeMillis() - start));
            start = System.currentTimeMillis();
            initACL(abstractImage.container());
            System.out.println("EXECUTION initACL:" + (System.currentTimeMillis() - start));
        }
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void admin_can_list_abstractImages() {
        assertThat(abstractImageService.list()).contains(abstractImage);
        AbstractImage anotherAbstractImage = builder.givenAnAbstractImage();
        assertThat(abstractImageService.list()).contains(anotherAbstractImage);
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void user_cannot_list_all_abstractImages() {
        expectOK(() -> abstractImageService.list());
        AbstractImage anotherAbstractImage = builder.givenAnAbstractImage();
        assertThat(abstractImageService.list()).doesNotContain(anotherAbstractImage);
    }


    @Override
    public void whenIGetDomain() {
        abstractImageService.get(abstractImage.getId());
    }

    @Override
    protected void whenIAddDomain() {
        abstractImageService.add(builder.givenANotPersistedAbstractImage().toJsonObject());
    }

    @Override
    public void whenIEditDomain() {
        abstractImageService.update(abstractImage, abstractImage.toJsonObject());
    }

    @Override
    protected void whenIDeleteDomain() {
        AbstractImage abstractImageToDelete = abstractImage;
        abstractImageService.delete(abstractImageToDelete, null, null, true);
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
