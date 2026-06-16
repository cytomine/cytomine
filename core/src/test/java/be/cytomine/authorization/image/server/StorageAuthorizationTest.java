package be.cytomine.authorization.image.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.authorization.AbstractAuthorizationTest;
import be.cytomine.domain.image.server.Storage;
import be.cytomine.service.image.server.StorageService;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@AutoConfigureMockMvc
@SpringBootTest(classes = CytomineCoreApplication.class)
@Transactional
public class StorageAuthorizationTest extends AbstractAuthorizationTest {

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
    public void adminCanListStorages() {
        assertThat(storageService.list()).contains(storage);
        Storage anotherStorage = builder.givenAStorage();
        assertThat(storageService.list()).contains(anotherStorage);
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userCannotListAllStorages() {
        expectOK(() -> storageService.list());
        Storage anotherStorage = builder.givenAStorage();
        assertThat(storageService.list()).doesNotContain(anotherStorage);
    }
}
