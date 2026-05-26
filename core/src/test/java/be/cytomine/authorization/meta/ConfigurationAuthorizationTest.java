package be.cytomine.authorization.meta;

import java.util.UUID;

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
import be.cytomine.domain.meta.Configuration;
import be.cytomine.domain.meta.ConfigurationReadingRole;
import be.cytomine.service.meta.ConfigurationService;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureMockMvc
@SpringBootTest(classes = CytomineCoreApplication.class)
@Transactional
public class ConfigurationAuthorizationTest extends AbstractAuthorizationTest {

    @Autowired
    ConfigurationService configurationService;

    @Autowired
    BasicInstanceBuilder builder;

    Configuration configForAdmin;

    Configuration configForUser;

    Configuration configForAll;

    @BeforeEach
    public void before() throws Exception {
        if (configForAdmin == null) {
            configForAdmin = builder.givenANotPersistedConfiguration("ADMIN");
            configForAdmin.setReadingRole(ConfigurationReadingRole.ADMIN);
            builder.persistAndReturn(configForAdmin);
        }
        if (configForUser == null) {
            configForUser = builder.givenANotPersistedConfiguration("USER");
            configForUser.setReadingRole(ConfigurationReadingRole.USER);
            builder.persistAndReturn(configForUser);
        }
        if (configForAll == null) {
            configForAll = builder.givenANotPersistedConfiguration("ALL");
            configForAll.setReadingRole(ConfigurationReadingRole.ALL);
            builder.persistAndReturn(configForAll);
        }
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void adminCanListConfig() {
        assertThat(configurationService.list()).contains(configForAdmin, configForUser, configForAll);
    }

    @Test
    @WithMockUser(username = USER_NO_ACL)
    public void userCanListConfig() {
        assertThat(configurationService.list()).contains(configForUser, configForAll).doesNotContain(configForAdmin);
    }

    @Test
    @WithMockUser(username = GUEST)
    public void guestCanListConfig() {
        assertThat(configurationService.list()).contains(configForAll).doesNotContain(configForUser, configForAdmin);
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void adminReadConfig() {
        expectOK(() -> configurationService.findByKey(configForAdmin.getKey()));
        expectOK(() -> configurationService.findByKey(configForUser.getKey()));
        expectOK(() -> configurationService.findByKey(configForAll.getKey()));
    }

    @Test
    @WithMockUser(username = USER_NO_ACL)
    public void userCanReadConfig() {
        expectForbidden(() -> configurationService.findByKey(configForAdmin.getKey()));
        expectOK(() -> configurationService.findByKey(configForUser.getKey()));
        expectOK(() -> configurationService.findByKey(configForAll.getKey()));
    }

    @Test
    @WithMockUser(username = GUEST)
    public void guestCanReadConfig() {
        expectForbidden(() -> configurationService.findByKey(configForAdmin.getKey()));
        expectForbidden(() -> configurationService.findByKey(configForUser.getKey()));
        expectOK(() -> configurationService.findByKey(configForAll.getKey()));
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void adminCanCreateConfig() {
        expectOK(() -> configurationService.add(configForUser.toJsonObject()
            .withChange("id", null)
            .withChange("key", UUID.randomUUID().toString())));
    }

    @Test
    @WithMockUser(username = USER_NO_ACL)
    public void userCannotCreateConfig() {
        expectForbidden(() -> configurationService.add(configForUser.toJsonObject()
            .withChange("id", null)
            .withChange("key", UUID.randomUUID().toString())));
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void adminCanEditConfig() {
        expectOK(() -> configurationService.update(
            configForUser,
            configForUser.toJsonObject().withChange("value", "newvalue")
        ));
    }

    @Test
    @WithMockUser(username = USER_NO_ACL)
    public void userCannotEditConfig() {
        expectForbidden(() -> configurationService.update(
            configForUser,
            configForUser.toJsonObject().withChange("value", "newvalue")
        ));
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void adminCanDeleteConfig() {
        Configuration configuration = builder.givenAConfiguration("xxx");
        expectOK(() -> configurationService.delete(configuration, null, null, false));
    }

    @Test
    @WithMockUser(username = USER_NO_ACL)
    public void userCannotDeleteConfig() {
        Configuration configuration = builder.givenAConfiguration("xxx");
        expectForbidden(() -> configurationService.delete(configuration, null, null, false));
    }
}
