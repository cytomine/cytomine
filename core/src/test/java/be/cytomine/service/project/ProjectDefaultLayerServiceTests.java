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
import be.cytomine.domain.project.ProjectDefaultLayer;
import be.cytomine.exceptions.AlreadyExistException;
import be.cytomine.exceptions.ObjectNotFoundException;
import be.cytomine.utils.CommandResponse;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = "ROLE_SUPER_ADMIN", username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
@Transactional
public class ProjectDefaultLayerServiceTests {

    @Autowired
    ProjectDefaultLayerService projectDefaultLayerService;

    @Autowired
    BasicInstanceBuilder builder;

    @Test
    void getProjectDefaultLayerWithSuccess() {
        ProjectDefaultLayer projectDefaultLayer = builder.givenAProjectDefaultLayer();
        assertThat(projectDefaultLayer).isEqualTo(projectDefaultLayerService.get(projectDefaultLayer.getId()));
    }

    @Test
    void getUnexistingProjectDefaultLayerReturnNull() {
        assertThat(projectDefaultLayerService.get(0L)).isNull();
    }

    @Test
    void findProjectDefaultLayerWithSuccess() {
        ProjectDefaultLayer projectDefaultLayer = builder.givenAProjectDefaultLayer();
        assertThat(projectDefaultLayerService.find(projectDefaultLayer.getId()).isPresent());
        assertThat(projectDefaultLayer).isEqualTo(projectDefaultLayerService.find(projectDefaultLayer.getId()).get());
    }

    @Test
    void findUnexistingProjectDefaultLayerReturnEmpty() {
        assertThat(projectDefaultLayerService.find(0L)).isEmpty();
    }

    @Test
    void listAllProjectDefaultLayerByProjectWithSuccess() {
        ProjectDefaultLayer projectDefaultLayer = builder.givenAProjectDefaultLayer();
        ProjectDefaultLayer projectDefaultLayerFromAnotherProject = builder.givenAProjectDefaultLayer();
        assertThat(projectDefaultLayer)
            .isIn(projectDefaultLayerService.listByProject(projectDefaultLayer.getProject()));
        assertThat(projectDefaultLayerFromAnotherProject).isNotIn(projectDefaultLayerService.listByProject(
            projectDefaultLayer.getProject()));
    }

    @Test
    void addValidProjectDefaultLayerWithSuccess() {
        ProjectDefaultLayer projectDefaultLayer = builder.givenANotPersistedProjectDefaultLayer();

        CommandResponse commandResponse = projectDefaultLayerService.add(projectDefaultLayer.toJsonObject());

        assertThat(commandResponse).isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(200);
        assertThat(projectDefaultLayerService.find(commandResponse.getObject().getId())).isPresent();
        ProjectDefaultLayer created = projectDefaultLayerService.find(commandResponse.getObject().getId()).get();
        assertThat(created.getProject()).isEqualTo(projectDefaultLayer.getProject());
    }

    @Test
    void addProjectDefaultLayerWithBadProject() {
        ProjectDefaultLayer projectDefaultLayer = builder.givenANotPersistedProjectDefaultLayer();
        Assertions.assertThrows(
            ObjectNotFoundException.class, () -> {
                projectDefaultLayerService.add(projectDefaultLayer.toJsonObject().withChange("project", 0L));
            }
        );
    }

    @Test
    void addProjectDefaultLayerWithBadUser() {
        ProjectDefaultLayer projectDefaultLayer = builder.givenANotPersistedProjectDefaultLayer();
        Assertions.assertThrows(
            ObjectNotFoundException.class, () -> {
                projectDefaultLayerService.add(projectDefaultLayer.toJsonObject().withChange("user", 0L));
            }
        );
    }

    @Test
    void addAlreadyExistingProjectDefaultLayerFails() {
        ProjectDefaultLayer projectDefaultLayer = builder.givenAProjectDefaultLayer();
        Assertions.assertThrows(
            AlreadyExistException.class, () -> {
                projectDefaultLayerService.add(projectDefaultLayer.toJsonObject().withChange("id", null));
            }
        );
    }

    @Test
    void deleteProjectDefaultLayerWithSuccess() {
        ProjectDefaultLayer projectDefaultLayer = builder.givenAProjectDefaultLayer();

        CommandResponse commandResponse = projectDefaultLayerService.delete(projectDefaultLayer, null, null, true);

        assertThat(commandResponse).isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(200);
        assertThat(projectDefaultLayerService.find(projectDefaultLayer.getId()).isEmpty());
    }
}
