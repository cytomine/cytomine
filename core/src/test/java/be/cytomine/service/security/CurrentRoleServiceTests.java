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
import be.cytomine.mapper.UserMapper;
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

    @Autowired
    UserMapper userMapper;

    @Test
    @WithMockUser(username = "superadmin")
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
    @WithMockUser(username = "admin")
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
    @WithMockUser(username = "guest")
    public void findRoleForGuest() {

        assertThat(currentRoleService.findRealAuthorities(userMapper.map(builder.givenAGuest())))
            .containsExactlyInAnyOrder("ROLE_GUEST");
        assertThat(currentRoleService.findCurrentAuthorities(userMapper.map(builder.givenAGuest())))
            .containsExactlyInAnyOrder("ROLE_GUEST");

        assertThat(currentRoleService.isAdminByNow(userMapper.map(builder.givenDefaultGuest()))).isFalse();
        assertThat(currentRoleService.isUserByNow(userMapper.map(builder.givenDefaultGuest()))).isFalse();
        assertThat(currentRoleService.isGuestByNow(userMapper.map(builder.givenDefaultGuest()))).isTrue();
        assertThat(currentRoleService.isAdmin(userMapper.map(builder.givenDefaultGuest()))).isFalse();
        assertThat(currentRoleService.isUser(userMapper.map(builder.givenDefaultGuest()))).isFalse();
        assertThat(currentRoleService.isGuest(userMapper.map(builder.givenDefaultGuest()))).isTrue();
        assertThat(currentRoleService.hasCurrentUserAdminRole(userMapper.map(builder.givenDefaultGuest()))).isFalse();
    }

    @Test
    @WithMockUser(username = "admin")
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
