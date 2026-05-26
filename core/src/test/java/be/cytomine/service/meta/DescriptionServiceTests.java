package be.cytomine.service.meta;

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
import be.cytomine.domain.meta.Description;
import be.cytomine.domain.project.Project;
import be.cytomine.exceptions.AlreadyExistException;
import be.cytomine.exceptions.ObjectNotFoundException;
import be.cytomine.utils.CommandResponse;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
@Transactional
public class DescriptionServiceTests {

    @Autowired
    DescriptionService descriptionService;

    @Autowired
    BasicInstanceBuilder builder;

    @Test
    public void listDescription() {
        Description description = builder.givenADescription(builder.givenAProject());
        assertThat(descriptionService.list()).contains(description);
    }

    @Test
    public void findDescriptionForDomain() {
        Project project = builder.givenAProject();
        Description description = builder.givenADescription(project);
        assertThat(descriptionService.findByDomain(project)).contains(description);
    }


    @Test
    public void findDescriptionForDomainIdent() {
        Project project = builder.givenAProject();
        Description description = builder.givenADescription(project);
        assertThat(descriptionService.findByDomain(
            project.getClass().getName(),
            project.getId()
        )).contains(description);
    }

    @Test
    public void findDescriptionForDomainThatDoNotExists() {
        Assertions.assertThrows(
            ObjectNotFoundException.class, () -> {
                descriptionService.findByDomain(Project.class.getName(), 0L);
            }
        );
    }


    @Test
    public void createDescription() {
        Project project = builder.givenAProject();
        Description description = builder.givenANotPersistedDescription(project);
        CommandResponse commandResponse = descriptionService.add(description.toJsonObject());
        assertThat(commandResponse).isNotNull();
        assertThat(descriptionService.findByDomain(project)).isNotNull();
    }

    @Test
    public void createDescriptionAlreadyExistsFail() {
        Project project = builder.givenAProject();
        Assertions.assertThrows(
            AlreadyExistException.class, () -> {
                Description description = builder.givenADescription(project);
                CommandResponse commandResponse = descriptionService.add(description.toJsonObject()
                    .withChange("id", null));
            }
        );
    }


    @Test
    public void editDescription() {
        Project project = builder.givenAProject();
        Description description = builder.givenADescription(project);
        description.setData("v2");
        CommandResponse commandResponse = descriptionService.update(description, description.toJsonObject());
        assertThat(commandResponse).isNotNull();
        assertThat(descriptionService.findByDomain(project).get().getData()).isEqualTo("v2");
    }

    @Test
    public void deleteDescription() {
        Project project = builder.givenAProject();
        Description description = builder.givenADescription(project);
        descriptionService.delete(description, null, null, false);
        assertThat(descriptionService.findByDomain(project)).isEmpty();
    }

}
