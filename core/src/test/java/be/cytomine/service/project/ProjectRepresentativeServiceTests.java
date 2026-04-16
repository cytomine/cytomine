package be.cytomine.service.project;

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
import be.cytomine.domain.project.ProjectRepresentativeUser;
import be.cytomine.exceptions.AlreadyExistException;
import be.cytomine.exceptions.ObjectNotFoundException;
import be.cytomine.exceptions.WrongArgumentException;
import be.cytomine.utils.CommandResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION;


@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = "ROLE_SUPER_ADMIN", username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
@Transactional
public class ProjectRepresentativeServiceTests {

    @Autowired
    ProjectMemberService projectMemberService;

    @Autowired
    ProjectRepresentativeUserService projectRepresentativeUserService;

    @Autowired
    BasicInstanceBuilder builder;

    @Test
    void getProjectRepresentativeUserWithSuccess() {
        ProjectRepresentativeUser projectRepresentativeUser = builder.givenAProjectRepresentativeUser();
        assertThat(projectRepresentativeUser)
            .isEqualTo(projectRepresentativeUserService.get(projectRepresentativeUser.getId()));
    }

    @Test
    void getUnexistingProjectRepresentativeUserReturnNull() {
        assertThat(projectRepresentativeUserService.get(0L)).isNull();
    }

    @Test
    void findProjectRepresentativeUserWithSuccess() {
        ProjectRepresentativeUser projectRepresentativeUser = builder.givenAProjectRepresentativeUser();
        assertThat(projectRepresentativeUserService.find(projectRepresentativeUser.getId()).isPresent());
        assertThat(projectRepresentativeUser)
            .isEqualTo(projectRepresentativeUserService.find(projectRepresentativeUser.getId()).get());
    }

    @Test
    void findUnexistingProjectRepresentativeUserReturnEmpty() {
        assertThat(projectRepresentativeUserService.find(0L)).isEmpty();
    }

    @Test
    void findProjectRepresentativeUserWithProjectAndUserWithSuccess() {
        ProjectRepresentativeUser projectRepresentativeUser = builder.givenAProjectRepresentativeUser();
        assertThat(projectRepresentativeUserService.find(
            projectRepresentativeUser.getProject(),
            projectRepresentativeUser.getUser()
        ).isPresent());
        assertThat(projectRepresentativeUser).isEqualTo(projectRepresentativeUserService.find(
            projectRepresentativeUser.getProject(),
            projectRepresentativeUser.getUser()
        ).get());
    }

    @Test
    void findUnexistingProjectRepresentativeUserWithProjectAndUserReturnEmpty() {
        assertThat(projectRepresentativeUserService.find(
            builder.givenAProject(),
            builder.givenSuperAdmin()
        )).isEmpty();
    }


    @Test
    void listAllProjectRepresentativeUserByProjectWithSuccess() {
        ProjectRepresentativeUser projectRepresentativeUser = builder.givenAProjectRepresentativeUser();
        ProjectRepresentativeUser
            projectRepresentativeUserFromAnotherProject
            = builder.givenAProjectRepresentativeUser();
        assertThat(projectRepresentativeUser).isIn(projectRepresentativeUserService.listByProject(
            projectRepresentativeUser.getProject()));
        assertThat(projectRepresentativeUserFromAnotherProject).isNotIn(projectRepresentativeUserService.listByProject(
            projectRepresentativeUser.getProject()));
    }

    @Test
    void addValidProjectRepresentativeUserWithSuccess() {
        ProjectRepresentativeUser
            projectRepresentativeUser
            = builder.givenANotPersistedProjectRepresentativeUser();

        CommandResponse
            commandResponse
            = projectRepresentativeUserService.add(projectRepresentativeUser.toJsonObject());

        assertThat(commandResponse).isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(200);
        assertThat(projectRepresentativeUserService.find(commandResponse.getObject().getId())).isPresent();
        ProjectRepresentativeUser created = projectRepresentativeUserService.find(commandResponse.getObject().getId())
            .get();
        assertThat(created.getProject()).isEqualTo(projectRepresentativeUser.getProject());
    }

    @Test
    void addProjectRepresentativeUserWithBadProject() {
        ProjectRepresentativeUser
            projectRepresentativeUser
            = builder.givenANotPersistedProjectRepresentativeUser();
        Assertions.assertThrows(
            ObjectNotFoundException.class, () -> {
                projectRepresentativeUserService.add(projectRepresentativeUser.toJsonObject()
                    .withChange("project", 0L));
            }
        );
    }

    @Test
    void addProjectRepresentativeUserWithBadUser() {
        ProjectRepresentativeUser
            projectRepresentativeUser
            = builder.givenANotPersistedProjectRepresentativeUser();
        Assertions.assertThrows(
            ObjectNotFoundException.class, () -> {
                projectRepresentativeUserService.add(projectRepresentativeUser.toJsonObject().withChange("user", 0L));
            }
        );
    }

    @Test
    void addAlreadyExistingProjectRepresentativeUserFails() {
        ProjectRepresentativeUser projectRepresentativeUser = builder.givenAProjectRepresentativeUser();
        Assertions.assertThrows(
            AlreadyExistException.class, () -> {
                projectRepresentativeUserService.add(projectRepresentativeUser.toJsonObject().withChange("id", null));
            }
        );
    }

    @Test
    void deleteProjectRepresentativeUserWithSuccess() {
        ProjectRepresentativeUser projectRepresentativeUser1 = builder.givenAProjectRepresentativeUser();
        ProjectRepresentativeUser projectRepresentativeUser2 = builder.givenAProjectRepresentativeUser(
            projectRepresentativeUser1.getProject(),
            builder.givenAUser()
        );
        CommandResponse commandResponse = projectRepresentativeUserService.delete(
            projectRepresentativeUser1,
            null,
            null,
            true
        );

        assertThat(commandResponse).isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(200);
        assertThat(projectRepresentativeUserService.find(projectRepresentativeUser1.getId()).isEmpty());
    }

    @Test
    void deleteProjectRepresentativeUserRefusedIfOnlyOneRepresentative() {
        ProjectRepresentativeUser projectRepresentativeUser = builder.givenAProjectRepresentativeUser();

        Assertions.assertThrows(
            WrongArgumentException.class, () -> {
                projectRepresentativeUserService.delete(projectRepresentativeUser, null, null, true);
            }
        );
    }

    @Test
    void deletingLastRepresentativeUserFromProjectWillGrantCurrentUserAsRepresentative() {
        ProjectRepresentativeUser projectRepresentativeUser = builder.givenAProjectRepresentativeUser(
            builder.givenAProject(), builder.givenAUser()
        );
        builder.addUserToProject(
            projectRepresentativeUser.getProject(),
            projectRepresentativeUser.getUser().getUsername(),
            ADMINISTRATION
        );

        assertThat(projectRepresentativeUserService.listByProject(projectRepresentativeUser.getProject())).hasSize(1);


        projectMemberService.deleteUserFromProject(
            projectRepresentativeUser.getUser(),
            projectRepresentativeUser.getProject(),
            true
        );

        assertThat(projectRepresentativeUserService.listByProject(projectRepresentativeUser.getProject())).hasSize(1);
        assertThat(projectRepresentativeUserService.find(
            projectRepresentativeUser.getProject(),
            projectRepresentativeUser.getUser()
        )).isEmpty();
        assertThat(projectRepresentativeUserService.find(
            projectRepresentativeUser.getProject(),
            builder.givenSuperAdmin()
        )).isPresent();
    }
}
