package be.cytomine.authorization.image;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.acls.model.Permission;
import org.springframework.transaction.annotation.Transactional;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.authorization.CRUDAuthorizationTest;
import be.cytomine.domain.image.UploadedFile;
import be.cytomine.service.image.UploadedFileService;

import static org.springframework.security.acls.domain.BasePermission.WRITE;

@AutoConfigureMockMvc
@SpringBootTest(classes = CytomineCoreApplication.class)
@Transactional
public class UploadedFileAuthorizationTest extends CRUDAuthorizationTest {

    // We need more flexibility:

    private UploadedFile uploadedFile = null;

    @Autowired
    UploadedFileService uploadedFileService;

    @Autowired
    BasicInstanceBuilder builder;

    @BeforeEach
    public void before() throws Exception {
        if (uploadedFile == null) {
            uploadedFile = builder.givenAUploadedFile();
            initACL(uploadedFile.container());
        }
    }

    @Override
    public void whenIGetDomain() {
        uploadedFileService.get(uploadedFile.getId());
    }

    @Override
    protected void whenIAddDomain() {
        UploadedFile uploadedFileToCreate = builder.givenANotPersistedUploadedFile();
        uploadedFileToCreate.setStorage(uploadedFile.getStorage());
        uploadedFileService.add(uploadedFileToCreate.toJsonObject());
    }

    @Override
    public void whenIEditDomain() {
        uploadedFileService.update(uploadedFile, uploadedFile.toJsonObject());
    }

    @Override
    protected void whenIDeleteDomain() {
        UploadedFile uploadedFileToDelete = uploadedFile;
        uploadedFileService.delete(uploadedFileToDelete, null, null, true);
    }

    @Override
    protected Optional<Permission> minimalPermissionForCreate() {
        return Optional.of(WRITE);
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
