package be.cytomine.service.security;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.config.WiremockRepository;
import be.cytomine.exceptions.ForbiddenException;
import be.cytomine.mapper.UserMapper;
import be.cytomine.repository.security.SecRoleRepository;
import be.cytomine.service.CurrentRoleService;

import static be.cytomine.authorization.AbstractAuthorizationTest.ADMIN;
import static be.cytomine.authorization.AbstractAuthorizationTest.GUEST;
import static be.cytomine.authorization.AbstractAuthorizationTest.SUPERADMIN;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class, WiremockRepository.class})
@Transactional
public class CurrentRoleServiceTests {

    @Autowired
    BasicInstanceBuilder builder;

    @Autowired
    CurrentRoleService currentRoleService;

    @Autowired
    SecRoleRepository secRoleRepository;

    @Autowired
    UserMapper userMapper;

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void findRoleForSuperadmin() {

        assertThat(currentRoleService.findRealAuthorities(userMapper.map(builder.givenSuperAdmin())))
            .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN", "ROLE_SUPER_ADMIN");
        assertThat(currentRoleService.findCurrentAuthorities(userMapper.map(builder.givenSuperAdmin())))
            .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN", "ROLE_SUPER_ADMIN");

        assertThat(currentRoleService.isAdminByNow(userMapper.map(builder.givenSuperAdmin()))).isTrue();
        assertThat(currentRoleService.isUserByNow(userMapper.map(builder.givenSuperAdmin()))).isTrue();
        assertThat(currentRoleService.isGuestByNow(userMapper.map(builder.givenSuperAdmin()))).isFalse();
        assertThat(currentRoleService.isAdmin(userMapper.map(builder.givenSuperAdmin()))).isTrue();
        assertThat(currentRoleService.isUser(userMapper.map(builder.givenSuperAdmin()))).isTrue();
        assertThat(currentRoleService.isGuest(userMapper.map(builder.givenSuperAdmin()))).isFalse();
        assertThat(currentRoleService.hasCurrentUserAdminRole(userMapper.map(builder.givenSuperAdmin()))).isTrue();
    }

    @Test
    @WithMockUser(username = ADMIN)
    public void findRoleForAdmin() {

        assertThat(currentRoleService.findRealAuthorities(userMapper.map(builder.givenDefaultAdmin())))
            .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
        assertThat(currentRoleService.findCurrentAuthorities(userMapper.map(builder.givenDefaultAdmin())))
            .containsExactlyInAnyOrder("ROLE_USER");

        assertThat(currentRoleService.isAdminByNow(userMapper.map(builder.givenDefaultAdmin()))).isFalse();
        assertThat(currentRoleService.isUserByNow(userMapper.map(builder.givenDefaultAdmin()))).isTrue();
        assertThat(currentRoleService.isGuestByNow(userMapper.map(builder.givenDefaultAdmin()))).isFalse();
        assertThat(currentRoleService.isAdmin(userMapper.map(builder.givenDefaultAdmin()))).isTrue();
        assertThat(currentRoleService.isUser(userMapper.map(builder.givenDefaultAdmin()))).isTrue();
        assertThat(currentRoleService.isGuest(userMapper.map(builder.givenDefaultAdmin()))).isFalse();
        assertThat(currentRoleService.hasCurrentUserAdminRole(userMapper.map(builder.givenDefaultAdmin()))).isTrue();
    }

    @Test
    @WithMockUser(username = "user")
    public void findRoleForUser() {

        assertThat(currentRoleService.findRealAuthorities(userMapper.map(builder.givenDefaultUser())))
            .containsExactlyInAnyOrder("ROLE_USER");
        assertThat(currentRoleService.findCurrentAuthorities(userMapper.map(builder.givenDefaultUser())))
            .containsExactlyInAnyOrder("ROLE_USER");

        assertThat(currentRoleService.isAdminByNow(userMapper.map(builder.givenDefaultUser()))).isFalse();
        assertThat(currentRoleService.isUserByNow(userMapper.map(builder.givenDefaultUser()))).isTrue();
        assertThat(currentRoleService.isGuestByNow(userMapper.map(builder.givenDefaultUser()))).isFalse();
        assertThat(currentRoleService.isAdmin(userMapper.map(builder.givenDefaultUser()))).isFalse();
        assertThat(currentRoleService.isUser(userMapper.map(builder.givenDefaultUser()))).isTrue();
        assertThat(currentRoleService.isGuest(userMapper.map(builder.givenDefaultUser()))).isFalse();
        assertThat(currentRoleService.hasCurrentUserAdminRole(userMapper.map(builder.givenDefaultUser()))).isFalse();
    }

    @Test
    @WithMockUser(username = GUEST)
    public void findRoleForGuest() {

        assertThat(currentRoleService.findRealAuthorities(userMapper.map(builder.givenAGuest())))
            .containsExactlyInAnyOrder("ROLE_GUEST");
        assertThat(currentRoleService.findCurrentAuthorities(userMapper.map(builder.givenAGuest())))
            .containsExactlyInAnyOrder("ROLE_GUEST");

        assertThat(currentRoleService.isAdminByNow(builder.givenDefaultGuest())).isFalse();
        assertThat(currentRoleService.isUserByNow(builder.givenDefaultGuest())).isFalse();
        assertThat(currentRoleService.isGuestByNow(builder.givenDefaultGuest())).isTrue();
        assertThat(currentRoleService.isAdmin(builder.givenDefaultGuest())).isFalse();
        assertThat(currentRoleService.isUser(builder.givenDefaultGuest())).isFalse();
        assertThat(currentRoleService.isGuest(builder.givenDefaultGuest())).isTrue();
        assertThat(currentRoleService.hasCurrentUserAdminRole(builder.givenDefaultGuest())).isFalse();
    }

    @Test
    @WithMockUser(username = ADMIN)
    public void openCloseAdminSessionAsAdmin() {
        assertThat(currentRoleService.findCurrentAuthorities(userMapper.map(builder.givenDefaultAdmin())))
            .containsExactlyInAnyOrder("ROLE_USER");

        assertThat(currentRoleService.isAdminByNow(userMapper.map(builder.givenDefaultAdmin()))).isFalse();
        assertThat(currentRoleService.isUserByNow(userMapper.map(builder.givenDefaultAdmin()))).isTrue();

        currentRoleService.activeAdminSession(userMapper.map(builder.givenDefaultAdmin()));

        assertThat(currentRoleService.isAdminByNow(userMapper.map(builder.givenDefaultAdmin()))).isTrue();
        assertThat(currentRoleService.isUserByNow(userMapper.map(builder.givenDefaultAdmin()))).isTrue();

        currentRoleService.closeAdminSession(userMapper.map(builder.givenDefaultAdmin()));

        assertThat(currentRoleService.isAdminByNow(userMapper.map(builder.givenDefaultAdmin()))).isFalse();
        assertThat(currentRoleService.isUserByNow(userMapper.map(builder.givenDefaultAdmin()))).isTrue();
    }

    @Test
    @WithMockUser(username = "user")
    public void openCloseAdminSessionAsUser() {

        assertThat(currentRoleService.isAdminByNow(userMapper.map(builder.givenDefaultUser()))).isFalse();
        assertThat(currentRoleService.isUserByNow(userMapper.map(builder.givenDefaultUser()))).isTrue();

        Assertions.assertThrows(
            ForbiddenException.class, () -> {
                currentRoleService.activeAdminSession(userMapper.map(builder.givenDefaultUser()));
            }
        );

        assertThat(currentRoleService.isAdminByNow(userMapper.map(builder.givenDefaultUser()))).isFalse();
        assertThat(currentRoleService.isUserByNow(userMapper.map(builder.givenDefaultUser()))).isTrue();
    }
}
