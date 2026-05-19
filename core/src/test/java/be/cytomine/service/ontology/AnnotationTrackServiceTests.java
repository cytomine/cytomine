package be.cytomine.service.ontology;

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
import be.cytomine.domain.ontology.AnnotationTrack;
import be.cytomine.domain.ontology.UserAnnotation;
import be.cytomine.exceptions.AlreadyExistException;
import be.cytomine.repository.ontology.AnnotationTrackRepository;
import be.cytomine.service.CommandService;
import be.cytomine.service.command.TransactionService;
import be.cytomine.utils.CommandResponse;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = "ROLE_SUPER_ADMIN", username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
@Transactional
public class AnnotationTrackServiceTests {

    @Autowired
    AnnotationTrackService annotationTrackService;

    @Autowired
    AnnotationTrackRepository annotationTrackRepository;

    @Autowired
    BasicInstanceBuilder builder;

    @Autowired
    CommandService commandService;

    @Autowired
    TransactionService transactionService;


    @Test
    void getAnnotationTrackWithSuccess() {
        AnnotationTrack annotationTrack = builder.givenAnAnnotationTrack();
        assertThat(annotationTrack).isEqualTo(annotationTrackService.get(annotationTrack.getId()));
    }

    @Test
    void getUnexistingAnnotationTrackReturnNull() {
        assertThat(annotationTrackService.get(0L)).isNull();
    }

    @Test
    void findAnnotationTrackWithSuccess() {
        AnnotationTrack annotationTrack = builder.givenAnAnnotationTrack();
        assertThat(annotationTrackService.find(annotationTrack.getId()).isPresent());
        assertThat(annotationTrack).isEqualTo(annotationTrackService.find(annotationTrack.getId()).get());
    }

    @Test
    void findUnexistingAnnotationTrackReturnEmpty() {
        assertThat(annotationTrackService.find(0L)).isEmpty();
    }

    @Test
    void findAnnotationTrackWithAnnotationAndTrack() {
        AnnotationTrack annotationTrack = builder.givenAnAnnotationTrack();
        UserAnnotation annotation = builder.givenAUserAnnotation();
        annotationTrack.setAnnotation(annotation);
        assertThat(annotationTrackService.find(annotation, annotationTrack.getTrack()).isPresent());
    }


    @Test
    void listAllAnnotationTrackByTrack() {
        AnnotationTrack annotationTrack = builder.givenAnAnnotationTrack();
        assertThat(annotationTrack).isIn(annotationTrackService.list(annotationTrack.getTrack()));
        assertThat(annotationTrackService.list(builder.givenATrack())).isEmpty();
    }

    @Test
    void listAllAnnotationTrackByAnnotation() {
        AnnotationTrack annotationTrack = builder.givenAnAnnotationTrack();
        UserAnnotation annotation = builder.givenAUserAnnotation();
        annotationTrack.setAnnotation(annotation);
        assertThat(annotationTrack).isIn(annotationTrackService.list(annotation));
        assertThat(annotationTrackService.list(builder.givenAUserAnnotation())).isEmpty();
    }

    @Test
    void addValidAnnotationTrackWithSuccess() {
        AnnotationTrack annotationTrack = builder.givenANotPersistedAnnotationTrack();

        CommandResponse commandResponse = annotationTrackService.add(annotationTrack.toJsonObject());

        assertThat(commandResponse).isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(200);
    }

    @Test
    void addValidAnnotationTrackAlreadyExists() {
        AnnotationTrack annotationTrack = builder.givenAnAnnotationTrack();

        Assertions.assertThrows(
            AlreadyExistException.class, () -> {
                annotationTrackService.add(annotationTrack.toJsonObject()
                    .withChange("id", null));
            }
        );
    }

    @Test
    void addValidAnnotationTrackWithDirectMethod() {
        AnnotationTrack annotationTrack = builder.givenANotPersistedAnnotationTrack();
        CommandResponse commandResponse = annotationTrackService.addAnnotationTrack(
            annotationTrack.getAnnotationClassName(),
            annotationTrack.getAnnotationIdent(),
            annotationTrack.getTrack().getId(),
            annotationTrack.getSlice().getId(),
            null
        );
        assertThat(commandResponse).isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(200);
    }

    @Test
    void deleteAnnotationTrackWithSuccess() {
        AnnotationTrack annotationTrack = builder.givenAnAnnotationTrack();

        CommandResponse commandResponse = annotationTrackService.delete(annotationTrack, null, null, true);

        assertThat(commandResponse).isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(200);
        assertThat(annotationTrackService.find(annotationTrack.getId()).isEmpty());
    }
}
