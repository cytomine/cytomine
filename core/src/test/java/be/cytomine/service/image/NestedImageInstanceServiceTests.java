package be.cytomine.service.image;

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

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.assertj.core.api.AssertionsForClassTypes;
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
import be.cytomine.domain.image.NestedImageInstance;
import be.cytomine.domain.project.Project;
import be.cytomine.exceptions.AlreadyExistException;
import be.cytomine.exceptions.ObjectNotFoundException;
import be.cytomine.exceptions.WrongArgumentException;
import be.cytomine.repository.image.UploadedFileRepository;
import be.cytomine.service.CommandService;
import be.cytomine.service.command.TransactionService;
import be.cytomine.utils.CommandResponse;
import be.cytomine.utils.JsonObject;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
@Transactional
public class NestedImageInstanceServiceTests {

    @Autowired
    NestedImageInstanceService nestedImageInstanceService;

    @Autowired
    UploadedFileRepository uploadedFileRepository;

    @Autowired
    BasicInstanceBuilder builder;

    @Autowired
    CommandService commandService;

    @Autowired
    TransactionService transactionService;

    @Autowired
    EntityManager entityManager;


    @Test
    void listAllNestedImageImageByImageInstance() {
        NestedImageInstance nestedImageInstance1 = builder.givenANestedImageInstance();
        NestedImageInstance nestedImageInstance2 = builder.givenANestedImageInstance();

        List<NestedImageInstance> list = nestedImageInstanceService.list(nestedImageInstance1.getParent());

        assertThat(list).contains(nestedImageInstance1);
        assertThat(list).doesNotContain(nestedImageInstance2);
    }

    @Test
    void getNestedImageIntanceWithSuccess() {
        NestedImageInstance nestedImageInstance = builder.givenANestedImageInstance();
        assertThat(nestedImageInstance).isEqualTo(nestedImageInstanceService.get(nestedImageInstance.getId()));
    }

    @Test
    void getUnexistingNestedImageInstanceReturnNull() {
        AssertionsForClassTypes.assertThat(nestedImageInstanceService.get(0L)).isNull();
    }

    @Test
    void findNestedImageInstanceWithSuccess() {
        NestedImageInstance nestedImageInstance = builder.givenANestedImageInstance();
        AssertionsForClassTypes.assertThat(nestedImageInstanceService.find(nestedImageInstance.getId()).isPresent());
        assertThat(nestedImageInstance).isEqualTo(nestedImageInstanceService.find(nestedImageInstance.getId()).get());
    }

    @Test
    void findUnexistingNestedImageInstanceReturnEmpty() {
        AssertionsForClassTypes.assertThat(nestedImageInstanceService.find(0L)).isEmpty();
    }

    @Test
    void addValidNestedImageInstanceWithSuccess() {
        NestedImageInstance nestedImageInstance = builder.givenANotPersistedNestedImageInstance();

        CommandResponse commandResponse = nestedImageInstanceService.add(nestedImageInstance.toJsonObject());

        assertThat(commandResponse).isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(200);
        AssertionsForClassTypes.assertThat(nestedImageInstanceService.find(commandResponse.getObject().getId()))
            .isPresent();
        NestedImageInstance created = nestedImageInstanceService.find(commandResponse.getObject().getId()).get();
    }


    @Test
    void addAlreadyExistingNestedImageInstanceFails() {
        NestedImageInstance nestedImageInstance = builder.givenANestedImageInstance();
        Assertions.assertThrows(
            AlreadyExistException.class, () -> {
                nestedImageInstanceService.add(nestedImageInstance.toJsonObject().withChange("id", null));
            }
        );
    }

    @Test
    void addValidNestedImageInstanceWithUnexstingAbstractImageFails() {
        NestedImageInstance nestedImageInstance = builder.givenANotPersistedNestedImageInstance();
        Assertions.assertThrows(
            WrongArgumentException.class, () -> {
                nestedImageInstanceService.add(nestedImageInstance.toJsonObject().withChange("baseImage", null));
            }
        );
    }

    @Test
    void addValidNestedImageInstanceWithUnexstingParentFails() {
        NestedImageInstance nestedImageInstance = builder.givenANotPersistedNestedImageInstance();
        Assertions.assertThrows(
            WrongArgumentException.class, () -> {
                nestedImageInstanceService.add(nestedImageInstance.toJsonObject().withChange("parent", null));
            }
        );
    }

    @Test
    void addValidNestedImageInstanceWithUnexstingProjectFails() {
        NestedImageInstance nestedImageInstance = builder.givenANotPersistedNestedImageInstance();
        Assertions.assertThrows(
            ObjectNotFoundException.class, () -> {
                nestedImageInstanceService.add(nestedImageInstance.toJsonObject().withChange("project", null));
            }
        );
    }

    @Test
    void editNestedImageInstanceWithSuccess() {
        Project project1 = builder.givenAProject();
        Project project2 = builder.givenAProject();

        NestedImageInstance nestedImageInstance = builder.givenANotPersistedNestedImageInstance();
        nestedImageInstance.setProject(project1);
        nestedImageInstance = builder.persistAndReturn(nestedImageInstance);

        JsonObject jsonObject = nestedImageInstance.toJsonObject();
        jsonObject.put("project", project2.getId());

        CommandResponse commandResponse = nestedImageInstanceService.edit(jsonObject, true);
        assertThat(commandResponse).isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(200);
        AssertionsForClassTypes.assertThat(nestedImageInstanceService.find(commandResponse.getObject().getId()))
            .isPresent();
        NestedImageInstance updated = nestedImageInstanceService.find(commandResponse.getObject().getId()).get();

        assertThat(updated.getProject().getId()).isEqualTo(project2.getId());
    }

    @Test
    void deleteNestedImageInstanceWithSuccess() {
        NestedImageInstance nestedImageInstance = builder.givenANestedImageInstance();

        CommandResponse commandResponse = nestedImageInstanceService.delete(nestedImageInstance, null, null, true);

        assertThat(commandResponse).isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(200);
        AssertionsForClassTypes.assertThat(nestedImageInstanceService.find(nestedImageInstance.getId()).isEmpty());
    }
}
