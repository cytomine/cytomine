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
import be.cytomine.domain.image.ImageInstance;
import be.cytomine.domain.image.server.Storage;
import be.cytomine.domain.ontology.Ontology;
import be.cytomine.domain.project.EditingMode;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.User;
import be.cytomine.exceptions.ForbiddenException;
import be.cytomine.repository.project.ProjectRepository;
import be.cytomine.repositorynosql.social.PersistentProjectConnectionRepository;
import be.cytomine.service.CommandService;
import be.cytomine.service.PermissionService;
import be.cytomine.service.command.TransactionService;
import be.cytomine.service.ontology.UserAnnotationService;
import be.cytomine.service.project.ProjectService;
import be.cytomine.service.social.ProjectConnectionService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION;
import static org.springframework.security.acls.domain.BasePermission.READ;


@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
@Transactional
public class SecurityAclServiceTests {

    @Autowired
    ProjectService projectService;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    BasicInstanceBuilder builder;

    @Autowired
    CommandService commandService;

    @Autowired
    TransactionService transactionService;

    @Autowired
    PersistentProjectConnectionRepository persistentProjectConnectionRepository;

    @Autowired
    ProjectConnectionService projectConnectionService;

    @Autowired
    UserAnnotationService userAnnotationService;

    @Autowired
    SecurityACLService securityACLService;

    @Autowired
    PermissionService permissionService;

    @WithMockUser(username = "user")
    @Test
    void checkIsUserAllowed() {
        Project project = builder.givenAProject();
        User user = builder.givenDefaultUser();

        Assertions.assertThrows(
            ForbiddenException.class, () -> {
                securityACLService.check(project.getId(), project.getClass().getName(), READ);
            }
        );

        Assertions.assertThrows(
            ForbiddenException.class, () -> {
                securityACLService.check(project.getId(), project.getClass(), READ);
            }
        );

        Assertions.assertThrows(
            ForbiddenException.class, () -> {
                securityACLService.check(project, READ, user);
            }
        );

        Assertions.assertThrows(
            ForbiddenException.class, () -> {
                securityACLService.check(project, READ);
            }
        );

        builder.addUserToProject(project, user.getUsername());

        securityACLService.check(project.getId(), project.getClass().getName(), READ);
        securityACLService.check(project.getId(), project.getClass(), READ);
        securityACLService.check(project, READ, user);
        securityACLService.check(project, READ);
    }

    @WithMockUser(username = "user")
    @Test
    void checkIfUserIsContainerAdmin() {
        Project project = builder.givenAProject();
        User user = builder.givenDefaultUser();

        Assertions.assertThrows(
            ForbiddenException.class, () -> {
                securityACLService.checkIsAdminContainer(project);
            }
        );
        Assertions.assertThrows(
            ForbiddenException.class, () -> {
                securityACLService.checkIsAdminContainer(project, user);
            }
        );

        builder.addUserToProject(project, user.getUsername(), ADMINISTRATION);

        securityACLService.checkIsAdminContainer(project);
        securityACLService.checkIsAdminContainer(project, user);
    }

    @WithMockUser(username = "user")
    @Test
    void hasUserPermission() {
        Project project = builder.givenAProject();
        User user = builder.givenDefaultUser();

        assertThat(securityACLService.hasPermission(project, READ, false)).isFalse();
        assertThat(securityACLService.hasPermission(project, READ)).isFalse();
        assertThat(securityACLService.hasPermission(project, READ, true)).isTrue();

        builder.addUserToProject(project, user.getUsername());

        assertThat(securityACLService.hasPermission(project, READ, false)).isTrue();
        assertThat(securityACLService.hasPermission(project, READ)).isTrue();
        assertThat(securityACLService.hasPermission(project, READ, true)).isTrue();
    }

    @WithMockUser(username = "user")
    @Test
    void hasRightToReadAbstractImage() {
        Project project = builder.givenAProject();
        ImageInstance imageInstance = builder.givenAnImageInstance(project);
        User user = builder.givenDefaultUser();

        assertThat(securityACLService.hasRightToReadAbstractImageWithProject(imageInstance.getBaseImage())).isFalse();

        builder.addUserToProject(project, user.getUsername());

        assertThat(securityACLService.hasRightToReadAbstractImageWithProject(imageInstance.getBaseImage())).isTrue();

    }

    @WithMockUser(username = "user")
    @Test
    void listAuthorizedStorages() {
        Storage storage = builder.givenAStorage();
        User user = builder.givenDefaultUser();

        assertThat(securityACLService.getStorageList(user, false)).doesNotContain(storage);

        permissionService.addPermission(storage, user.getUsername(), READ);

        assertThat(securityACLService.getStorageList(user, false)).contains(storage);
        assertThat(securityACLService.getStorageList(user, false, storage.getName())).contains(storage);

    }


    @WithMockUser(username = "user")
    @Test
    void listAuthorizedProjects() {
        Project project = builder.givenAProject();
        User user = builder.givenDefaultUser();

        assertThat(securityACLService.getProjectList(user, project.getOntology().getId())).doesNotContain(project);

        permissionService.addPermission(project, user.getUsername(), READ);

        assertThat(securityACLService.getProjectList(user, project.getOntology().getId())).contains(project);

    }

    @WithMockUser(username = "user")
    @Test
    void listUserFromProjects() {
        Project project = builder.givenAProject();
        User user = builder.givenDefaultUser();

        assertThat(securityACLService.getProjectUsers(project)).doesNotContain(user.getUsername());

        permissionService.addPermission(project, user.getUsername(), READ);

        assertThat(securityACLService.getProjectUsers(project)).contains(user.getUsername());

    }


    @WithMockUser(username = "user")
    @Test
    void listAuthorizedOntologies() {
        Ontology ontology = builder.givenAnOntology();
        User user = builder.givenDefaultUser();

        assertThat(securityACLService.getOntologyList(user)).doesNotContain(ontology);

        permissionService.addPermission(ontology, user.getUsername(), READ);

        assertThat(securityACLService.getOntologyList(user)).contains(ontology);

    }

    @WithMockUser(username = "user")
    @Test
    void checkSameUser() {
        User user = builder.givenDefaultUser();
        Assertions.assertThrows(
            ForbiddenException.class, () -> {
                securityACLService.checkIsSameUser(builder.givenSuperAdmin(), user);
            }
        );
        securityACLService.checkIsSameUser(user, user);
        securityACLService.checkIsSameUser(user, builder.givenSuperAdmin());
    }

    @WithMockUser(username = "user")
    @Test
    void checkIsAdmin() {
        User user = builder.givenDefaultUser();
        Assertions.assertThrows(
            ForbiddenException.class, () -> {
                securityACLService.checkAdmin(user);
            }
        );
        securityACLService.checkAdmin(builder.givenSuperAdmin());
    }

    @WithMockUser(username = "user")
    @Test
    void checkIsUser() {
        User user = builder.givenDefaultUser();
        User guest = builder.givenAGuest();

        Assertions.assertThrows(
            ForbiddenException.class, () -> {
                securityACLService.checkAdmin(guest);
            }
        );
        securityACLService.checkUser(user);
        securityACLService.checkUser(builder.givenSuperAdmin());
    }

    @WithMockUser(username = "user")
    @Test
    void checkIsGuest() {
        User user = builder.givenDefaultUser();
        User guest = builder.givenAGuest();

        securityACLService.checkGuest(guest);
        securityACLService.checkGuest(user);
        securityACLService.checkGuest(builder.givenSuperAdmin());
    }

    @WithMockUser(username = "user")
    @Test
    void checkNotReadonly() {
        Project project = builder.givenAProject();
        User user = builder.givenDefaultUser();
        permissionService.addPermission(project, user.getUsername(), READ);

        securityACLService.checkIsNotReadOnly(project);

        project.setMode(EditingMode.READ_ONLY);

        Assertions.assertThrows(
            ForbiddenException.class, () -> {
                securityACLService.checkIsNotReadOnly(project);
            }
        );

        permissionService.addPermission(project, user.getUsername(), ADMINISTRATION);

        securityACLService.checkIsNotReadOnly(project);
    }

    @WithMockUser(username = "superadmin")
    @Test
    void checkIsUserInProject() {
        Project project = builder.givenAProject();
        User user = builder.givenAUser();
        assertThat(securityACLService.isUserInProject(user, project))
            .isFalse();
        builder.addUserToProject(project, user.getUsername());
        assertThat(securityACLService.isUserInProject(user, project))
            .isTrue();
    }

}
