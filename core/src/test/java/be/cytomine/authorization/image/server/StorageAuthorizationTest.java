package be.cytomine.authorization.image.server;

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
import be.cytomine.domain.image.server.Storage;
import be.cytomine.service.image.server.StorageService;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION;

@AutoConfigureMockMvc
@SpringBootTest(classes = CytomineCoreApplication.class)
@Transactional
public class StorageAuthorizationTest extends CRUDAuthorizationTest {

    // We need more flexibility:

    private Storage storage = null;

    @Autowired
    StorageService storageService;

    @Autowired
    BasicInstanceBuilder builder;

    @BeforeEach
    public void before() throws Exception {
        if (storage == null) {
            storage = builder.givenAStorage();
            initACL(storage);
        }
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void admin_can_list_storages() {
        assertThat(storageService.list()).contains(storage);
        Storage anotherStorage = builder.givenAStorage();
        assertThat(storageService.list()).contains(anotherStorage);
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void user_cannot_list_all_storages() {
        expectOK(() -> storageService.list());
        Storage anotherStorage = builder.givenAStorage();
        assertThat(storageService.list()).doesNotContain(anotherStorage);
    }


    @Override
    public void whenIGetDomain() {
        storageService.get(storage.getId());
    }

    @Override
    protected void whenIAddDomain() {
        storageService.add(builder.givenANotPersistedStorage().toJsonObject());
    }

    @Override
    public void whenIEditDomain() {
        storageService.update(storage, storage.toJsonObject());
    }

    @Override
    protected void whenIDeleteDomain() {
        Storage storageToDelete = storage;
        storageService.delete(storageToDelete, null, null, true);
    }

    @Override
    protected Optional<Permission> minimalPermissionForCreate() {
        return Optional.empty();
    }

    @Override
    protected Optional<Permission> minimalPermissionForDelete() {
        return Optional.of(ADMINISTRATION);
    }

    @Override
    protected Optional<Permission> minimalPermissionForEdit() {
        return Optional.of(ADMINISTRATION);
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
