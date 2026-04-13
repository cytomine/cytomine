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
import be.cytomine.domain.image.AbstractImage;
import be.cytomine.domain.image.AbstractSlice;
import be.cytomine.domain.image.SliceInstance;
import be.cytomine.domain.image.UploadedFile;
import be.cytomine.exceptions.AlreadyExistException;
import be.cytomine.exceptions.CytomineException;
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
public class AbstractSliceServiceTests {

    @Autowired
    AbstractSliceService abstractSliceService;

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

    @Autowired
    ImageInstanceService imageInstanceService;

    @Test
    void list_all_image_by_abstract_image() {
        AbstractImage image1 = builder.givenAnAbstractImage();
        AbstractImage image2 = builder.givenAnAbstractImage();

        AbstractSlice abstractSlice1 = builder.givenAnAbstractSlice();
        abstractSlice1.setImage(image1);
        builder.persistAndReturn(abstractSlice1);
        AbstractSlice abstractSlice2 = builder.givenAnAbstractSlice();
        abstractSlice2.setImage(image2);
        builder.persistAndReturn(abstractSlice2);

        assertThat(abstractSliceService.list(image1)).contains(abstractSlice1);
        assertThat(abstractSliceService.list(image1)).doesNotContain(abstractSlice2);
    }

    @Test
    void list_all_image_by_uploaded_file() {
        UploadedFile file1 = builder.givenAUploadedFile();
        UploadedFile file2 = builder.givenAUploadedFile();

        AbstractSlice abstractSlice1 = builder.givenAnAbstractSlice();
        abstractSlice1.setUploadedFile(file1);
        builder.persistAndReturn(abstractSlice1);
        AbstractSlice abstractSlice2 = builder.givenAnAbstractSlice();
        abstractSlice2.setUploadedFile(file2);
        builder.persistAndReturn(abstractSlice2);

        assertThat(abstractSliceService.list(file1)).contains(abstractSlice1);
        assertThat(abstractSliceService.list(file1)).doesNotContain(abstractSlice2);
    }


    @Test
    void find_abstract_slice_by_image_and_coordinates() {

        AbstractSlice abstractSlice1 = builder.givenAnAbstractSlice();
        abstractSlice1.setChannel(1);
        abstractSlice1.setZStack(2);
        abstractSlice1.setTime(3);
        builder.persistAndReturn(abstractSlice1);
        AbstractSlice abstractSlice2 = builder.givenAnAbstractSlice();
        abstractSlice2.setImage(abstractSlice1.getImage());
        abstractSlice2.setChannel(1);
        abstractSlice2.setZStack(2);
        abstractSlice2.setTime(4);
        builder.persistAndReturn(abstractSlice2);


        assertThat(abstractSliceService.find(abstractSlice1.getImage(), 1, 2, 3)).isPresent();
        assertThat(abstractSliceService.find(abstractSlice1.getImage(), 1, 2, 4)).isPresent();
        assertThat(abstractSliceService.find(abstractSlice1.getImage(), 2, 2, 3)).isEmpty();
    }

    @Test
    void find_abstract_slice_image_uploaded() {
        AbstractSlice abstractSlice1 = builder.givenAnAbstractSlice();
        assertThat(abstractSliceService.findImageUploaded(abstractSlice1.getId()))
            .isEqualTo(abstractSlice1.getUploadedFile().getUser());
    }


    @Test
    void get_unexisting_abstractSlice_return_null() {
        assertThat(abstractSliceService.get(0L)).isNull();
    }

    @Test
    void find_abstractSlice_with_success() {
        AbstractSlice abstractSlice = builder.givenAnAbstractSlice();
        assertThat(abstractSliceService.find(abstractSlice.getId()).isPresent());
        assertThat(abstractSlice).isEqualTo(abstractSliceService.find(abstractSlice.getId()).get());
    }

    @Test
    void find_unexisting_abstractSlice_return_empty() {
        assertThat(abstractSliceService.find(0L)).isEmpty();
    }

    @Test
    void add_valid_abstract_slice_with_success() {
        AbstractSlice abstractSlice = builder.givenANotPersistedAbstractSlice();

        CommandResponse commandResponse = abstractSliceService.add(abstractSlice.toJsonObject());

        assertThat(commandResponse).isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(200);
        AssertionsForClassTypes.assertThat(abstractSliceService.find(commandResponse.getObject().getId())).isPresent();
        AbstractSlice created = abstractSliceService.find(commandResponse.getObject().getId()).get();
    }

    @Test
    void add_already_existing_abstract_slice() {
        AbstractSlice abstractSlice = builder.givenAnAbstractSlice();
        Assertions.assertThrows(
            AlreadyExistException.class, () -> {
                abstractSliceService.add(abstractSlice.toJsonObject().withChange("id", null));
            }
        );
    }

    @Test
    void add_valid_abstract_slice_with_null_abstract_image_fails() {
        AbstractSlice abstractSlice = builder.givenANotPersistedAbstractSlice();
        Assertions.assertThrows(
            WrongArgumentException.class, () -> {
                abstractSliceService.add(abstractSlice.toJsonObject().withChange("image", null));
            }
        );
    }

    @Test
    void edit_abstract_slice_with_success() {
        AbstractSlice abstractSlice = builder.givenANotPersistedAbstractSlice();
        abstractSlice.setChannel(1);
        abstractSlice.setZStack(10);
        abstractSlice.setTime(100);
        abstractSlice = builder.persistAndReturn(abstractSlice);

        JsonObject jsonObject = abstractSlice.toJsonObject();
        jsonObject.put("channel", 2);
        jsonObject.put("zStack", 20);
        jsonObject.put("time", 200);

        CommandResponse commandResponse = abstractSliceService.edit(jsonObject, true);
        assertThat(commandResponse).isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(200);
        AssertionsForClassTypes.assertThat(abstractSliceService.find(commandResponse.getObject().getId())).isPresent();
        AbstractSlice updated = abstractSliceService.find(commandResponse.getObject().getId()).get();

        assertThat(updated.getChannel()).isEqualTo(2);
        assertThat(updated.getZStack()).isEqualTo(20);
        assertThat(updated.getTime()).isEqualTo(200);
    }

    @Test
    void delete_abstract_slice_with_success() {
        AbstractSlice abstractSlice = builder.givenAnAbstractSlice();

        CommandResponse commandResponse = abstractSliceService.delete(abstractSlice, null, null, true);

        assertThat(commandResponse).isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(200);
        AssertionsForClassTypes.assertThat(abstractSliceService.find(abstractSlice.getId()).isEmpty());
    }

    @Test
    void delete_abstract_slice_with_dependencies_with_success() {
        SliceInstance sliceInstance = builder.givenASliceInstance();
        Assertions.assertThrows(
            CytomineException.class, () -> {
                abstractSliceService.delete(sliceInstance.getBaseSlice(), null, null, false);
            }
        );
    }


}
