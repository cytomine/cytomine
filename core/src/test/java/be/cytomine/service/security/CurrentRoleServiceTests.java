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

import static be.cytomine.BasicInstanceBuilder.ACL_USER_NO_ACL;
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

        assertThat(currentRoleService.findRealAuthorities(builder.givenSuperAdmin()))
            .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN", "ROLE_SUPER_ADMIN");
        assertThat(currentRoleService.findCurrentAuthorities(builder.givenSuperAdmin()))
            .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN", "ROLE_SUPER_ADMIN");

        assertThat(currentRoleService.isAdminByNow(builder.givenSuperAdmin())).isTrue();
        assertThat(currentRoleService.isUserByNow(builder.givenSuperAdmin())).isTrue();
        assertThat(currentRoleService.isGuestByNow(builder.givenSuperAdmin())).isFalse();
        assertThat(currentRoleService.isAdmin(builder.givenSuperAdmin())).isTrue();
        assertThat(currentRoleService.isUser(builder.givenSuperAdmin())).isTrue();
        assertThat(currentRoleService.isGuest(builder.givenSuperAdmin())).isFalse();
        assertThat(currentRoleService.hasCurrentUserAdminRole(builder.givenSuperAdmin())).isTrue();
    }

    @Test
    @WithMockUser(username = ADMIN)
    public void findRoleForAdmin() {

        assertThat(currentRoleService.findRealAuthorities(builder.givenAdmin()))
            .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
        assertThat(currentRoleService.findCurrentAuthorities(builder.givenAdmin()))
            .containsExactlyInAnyOrder("ROLE_USER");

        assertThat(currentRoleService.isAdminByNow(builder.givenAdmin())).isFalse();
        assertThat(currentRoleService.isUserByNow(builder.givenAdmin())).isTrue();
        assertThat(currentRoleService.isGuestByNow(builder.givenAdmin())).isFalse();
        assertThat(currentRoleService.isAdmin(builder.givenAdmin())).isTrue();
        assertThat(currentRoleService.isUser(builder.givenAdmin())).isTrue();
        assertThat(currentRoleService.isGuest(builder.givenAdmin())).isFalse();
        assertThat(currentRoleService.hasCurrentUserAdminRole(builder.givenAdmin())).isTrue();
    }

    @Test
    @WithMockUser(username = ACL_USER_NO_ACL)
    public void findRoleForUser() {

        assertThat(currentRoleService.findRealAuthorities(builder.givenAclUserNoAcl()))
            .containsExactlyInAnyOrder("ROLE_USER");
        assertThat(currentRoleService.findCurrentAuthorities(builder.givenAclUserNoAcl()))
            .containsExactlyInAnyOrder("ROLE_USER");

        assertThat(currentRoleService.isAdminByNow(builder.givenAclUserNoAcl())).isFalse();
        assertThat(currentRoleService.isUserByNow(builder.givenAclUserNoAcl())).isTrue();
        assertThat(currentRoleService.isGuestByNow(builder.givenAclUserNoAcl())).isFalse();
        assertThat(currentRoleService.isAdmin(builder.givenAclUserNoAcl())).isFalse();
        assertThat(currentRoleService.isUser(builder.givenAclUserNoAcl())).isTrue();
        assertThat(currentRoleService.isGuest(builder.givenAclUserNoAcl())).isFalse();
        assertThat(currentRoleService.hasCurrentUserAdminRole(builder.givenAclUserNoAcl())).isFalse();
    }

    @Test
    @WithMockUser(username = GUEST)
    public void findRoleForGuest() {

        assertThat(currentRoleService.findRealAuthorities(builder.givenGuestAcl()))
            .containsExactlyInAnyOrder("ROLE_GUEST");
        assertThat(currentRoleService.findCurrentAuthorities(builder.givenGuestAcl()))
            .containsExactlyInAnyOrder("ROLE_GUEST");

        assertThat(currentRoleService.isAdminByNow(builder.givenGuestAcl())).isFalse();
        assertThat(currentRoleService.isUserByNow(builder.givenGuestAcl())).isFalse();
        assertThat(currentRoleService.isGuestByNow(builder.givenGuestAcl())).isTrue();
        assertThat(currentRoleService.isAdmin(builder.givenGuestAcl())).isFalse();
        assertThat(currentRoleService.isUser(builder.givenGuestAcl())).isFalse();
        assertThat(currentRoleService.isGuest(builder.givenGuestAcl())).isTrue();
        assertThat(currentRoleService.hasCurrentUserAdminRole(builder.givenGuestAcl())).isFalse();
    }

    @Test
    @WithMockUser(username = ADMIN)
    public void openCloseAdminSessionAsAdmin() {
        assertThat(currentRoleService.findCurrentAuthorities(builder.givenAdmin()))
            .containsExactlyInAnyOrder("ROLE_USER");

        assertThat(currentRoleService.isAdminByNow(builder.givenAdmin())).isFalse();
        assertThat(currentRoleService.isUserByNow(builder.givenAdmin())).isTrue();

        currentRoleService.activeAdminSession(builder.givenAdmin());

        assertThat(currentRoleService.isAdminByNow(builder.givenAdmin())).isTrue();
        assertThat(currentRoleService.isUserByNow(builder.givenAdmin())).isTrue();

        currentRoleService.closeAdminSession(builder.givenAdmin());

        assertThat(currentRoleService.isAdminByNow(builder.givenAdmin())).isFalse();
        assertThat(currentRoleService.isUserByNow(builder.givenAdmin())).isTrue();
    }

    @Test
    @WithMockUser(username = ACL_USER_NO_ACL)
    public void openCloseAdminSessionAsUser() {

        assertThat(currentRoleService.isAdminByNow(builder.givenAclUserNoAcl())).isFalse();
        assertThat(currentRoleService.isUserByNow(builder.givenAclUserNoAcl())).isTrue();

        Assertions.assertThrows(
            ForbiddenException.class, () -> {
                currentRoleService.activeAdminSession(builder.givenAclUserNoAcl());
            }
        );

        assertThat(currentRoleService.isAdminByNow(builder.givenAclUserNoAcl())).isFalse();
        assertThat(currentRoleService.isUserByNow(builder.givenAclUserNoAcl())).isTrue();
    }
}
