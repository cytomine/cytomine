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
import be.cytomine.domain.image.CompanionFile;
import be.cytomine.service.image.CompanionFileService;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.security.acls.domain.BasePermission.WRITE;

@AutoConfigureMockMvc
@SpringBootTest(classes = CytomineCoreApplication.class)
@Transactional
public class CompanionFileAuthorizationTest extends CRUDAuthorizationTest {

    private CompanionFile companionFile = null;

    @Autowired
    CompanionFileService companionFileService;

    @Autowired
    BasicInstanceBuilder builder;

    @BeforeEach
    public void before() throws Exception {
        if (companionFile == null) {
            companionFile = builder.givenACompanionFile(builder.givenAnAbstractImage());
            initACL(companionFile.container());
        }
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void admin_can_list() {
        assertThat(companionFileService.list(companionFile.getImage())).contains(companionFile);
        AbstractImage anotherAbstractImage = builder.givenAnAbstractImage();
        assertThat(companionFileService.list(companionFile.getUploadedFile())).contains(companionFile);
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void user_can_list() {
        assertThat(companionFileService.list(companionFile.getImage())).contains(companionFile);
        AbstractImage anotherAbstractImage = builder.givenAnAbstractImage();
        assertThat(companionFileService.list(companionFile.getUploadedFile())).contains(companionFile);
    }

    @Test
    @WithMockUser(username = USER_NO_ACL)
    public void user_with_no_read_cannot_list() {
        expectForbidden(() -> companionFileService.list(companionFile.getImage()));
        expectForbidden(() -> companionFileService.list(companionFile.getUploadedFile()));
    }


    @Override
    public void whenIGetDomain() {
        companionFileService.get(companionFile.getId());
    }

    @Override
    protected void whenIAddDomain() {
        AbstractImage abstractImage = builder.givenAnAbstractImage();
        abstractImage.getUploadedFile().setStorage(companionFile.getImage().getUploadedFile().getStorage());
        companionFileService.add(builder.givenANotPersistedCompanionFile(abstractImage).toJsonObject());
    }

    @Override
    public void whenIEditDomain() {
        companionFileService.update(companionFile, companionFile.toJsonObject());
    }

    @Override
    protected void whenIDeleteDomain() {
        CompanionFile companionFileToDelete = companionFile;
        companionFileService.delete(companionFileToDelete, null, null, true);
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
