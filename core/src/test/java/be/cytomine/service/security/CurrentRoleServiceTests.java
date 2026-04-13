package be.cytomine.service.security;

/*
 * Copyright (c) 2009-2022. Authors: see NOTICE file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import be.cytomine.exceptions.ForbiddenException;
import be.cytomine.repository.security.SecRoleRepository;
import be.cytomine.service.CurrentRoleService;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
@Transactional
public class CurrentRoleServiceTests {

    @Autowired
    BasicInstanceBuilder builder;

    @Autowired
    CurrentRoleService currentRoleService;

    @Autowired
    SecRoleRepository secRoleRepository;

    @Test
    @WithMockUser(username = "superadmin")
    public void find_role_for_superadmin() {
        assertThat(currentRoleService.findRealRole(builder.givenSuperAdmin()))
            .contains(secRoleRepository.getSuperAdmin());

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
    @WithMockUser(username = "admin")
    public void find_role_for_admin() {
        assertThat(currentRoleService.findRealRole(builder.givenDefaultAdmin()))
            .contains(secRoleRepository.getAdmin());

        assertThat(currentRoleService.findRealAuthorities(builder.givenDefaultAdmin()))
            .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
        assertThat(currentRoleService.findCurrentAuthorities(builder.givenDefaultAdmin()))
            .containsExactlyInAnyOrder("ROLE_USER");

        assertThat(currentRoleService.isAdminByNow(builder.givenDefaultAdmin())).isFalse();
        assertThat(currentRoleService.isUserByNow(builder.givenDefaultAdmin())).isTrue();
        assertThat(currentRoleService.isGuestByNow(builder.givenDefaultAdmin())).isFalse();
        assertThat(currentRoleService.isAdmin(builder.givenDefaultAdmin())).isTrue();
        assertThat(currentRoleService.isUser(builder.givenDefaultAdmin())).isTrue();
        assertThat(currentRoleService.isGuest(builder.givenDefaultAdmin())).isFalse();
        assertThat(currentRoleService.hasCurrentUserAdminRole(builder.givenDefaultAdmin())).isTrue();
    }

    @Test
    @WithMockUser(username = "user")
    public void find_role_for_user() {
        assertThat(currentRoleService.findRealRole(builder.givenDefaultUser()))
            .contains(secRoleRepository.getUser());

        assertThat(currentRoleService.findRealAuthorities(builder.givenDefaultUser()))
            .containsExactlyInAnyOrder("ROLE_USER");
        assertThat(currentRoleService.findCurrentAuthorities(builder.givenDefaultUser()))
            .containsExactlyInAnyOrder("ROLE_USER");

        assertThat(currentRoleService.isAdminByNow(builder.givenDefaultUser())).isFalse();
        assertThat(currentRoleService.isUserByNow(builder.givenDefaultUser())).isTrue();
        assertThat(currentRoleService.isGuestByNow(builder.givenDefaultUser())).isFalse();
        assertThat(currentRoleService.isAdmin(builder.givenDefaultUser())).isFalse();
        assertThat(currentRoleService.isUser(builder.givenDefaultUser())).isTrue();
        assertThat(currentRoleService.isGuest(builder.givenDefaultUser())).isFalse();
        assertThat(currentRoleService.hasCurrentUserAdminRole(builder.givenDefaultUser())).isFalse();
    }

    @Test
    @WithMockUser(username = "guest")
    public void find_role_for_guest() {
        assertThat(currentRoleService.findRealRole(builder.givenAGuest()))
            .contains(secRoleRepository.getGuest());

        assertThat(currentRoleService.findRealAuthorities(builder.givenAGuest()))
            .containsExactlyInAnyOrder("ROLE_GUEST");
        assertThat(currentRoleService.findCurrentAuthorities(builder.givenAGuest()))
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
    @WithMockUser(username = "admin")
    public void open_close_admin_session_as_admin() {

        assertThat(currentRoleService.findRealRole(builder.givenDefaultAdmin()))
            .contains(secRoleRepository.getAdmin());
        assertThat(currentRoleService.findCurrentAuthorities(builder.givenDefaultAdmin()))
            .containsExactlyInAnyOrder("ROLE_USER");

        assertThat(currentRoleService.isAdminByNow(builder.givenDefaultAdmin())).isFalse();
        assertThat(currentRoleService.isUserByNow(builder.givenDefaultAdmin())).isTrue();

        currentRoleService.activeAdminSession(builder.givenDefaultAdmin());

        assertThat(currentRoleService.isAdminByNow(builder.givenDefaultAdmin())).isTrue();
        assertThat(currentRoleService.isUserByNow(builder.givenDefaultAdmin())).isTrue();

        currentRoleService.closeAdminSession(builder.givenDefaultAdmin());

        assertThat(currentRoleService.isAdminByNow(builder.givenDefaultAdmin())).isFalse();
        assertThat(currentRoleService.isUserByNow(builder.givenDefaultAdmin())).isTrue();
    }

    @Test
    @WithMockUser(username = "user")
    public void open_close_admin_session_as_user() {

        assertThat(currentRoleService.isAdminByNow(builder.givenDefaultUser())).isFalse();
        assertThat(currentRoleService.isUserByNow(builder.givenDefaultUser())).isTrue();

        Assertions.assertThrows(
            ForbiddenException.class, () -> {
                currentRoleService.activeAdminSession(builder.givenDefaultUser());
            }
        );

        assertThat(currentRoleService.isAdminByNow(builder.givenDefaultUser())).isFalse();
        assertThat(currentRoleService.isUserByNow(builder.givenDefaultUser())).isTrue();
    }
}
