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

import java.util.List;
import java.util.Map;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
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
import be.cytomine.domain.meta.Property;
import be.cytomine.domain.ontology.UserAnnotation;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.User;
import be.cytomine.utils.CommandResponse;
import be.cytomine.utils.GeometryUtils;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
@Transactional
public class PropertyServiceTests {

    @Autowired
    PropertyService propertyService;

    @Autowired
    BasicInstanceBuilder builder;

    @Test
    public void listProperty() {
        Property property = builder.givenAProperty(builder.givenAProject());
        assertThat(propertyService.list()).contains(property);
    }

    @Test
    public void listPropertyForDomain() {
        Project project = builder.givenAProject();
        Property property = builder.givenAProperty(project);
        assertThat(propertyService.list(project)).contains(property);
    }


    @Test
    public void findById() {
        Property property = builder.givenAProperty(builder.givenAProject());
        assertThat(propertyService.findById(property.getId())).isPresent();
    }

    @Test
    public void findByDomainAndKey() {
        Project project = builder.givenAProject();
        Property property = builder.givenAProperty(project);
        assertThat(propertyService.findByDomainAndKey(project, property.getKey())).isPresent();
    }


    @Test
    public void findByIdThatDoNotExists() {
        assertThat(propertyService.findById(0L)).isEmpty();
    }

    @Test
    public void createProperty() {
        Project project = builder.givenAProject();
        CommandResponse commandResponse =
            propertyService.addProperty(
                project.getClass().getName(),
                project.getId(),
                "key",
                "value",
                builder.givenSuperAdmin(),
                null
            );
        assertThat(commandResponse).isNotNull();
        assertThat(propertyService.findByDomainAndKey(project, "key")).isPresent();
    }

    @Test
    public void addProperty() {
        Project project = builder.givenAProject();

        CommandResponse commandResponse =
            propertyService.add(builder.givenANotPersistedProperty(project, "key", "value").toJsonObject());
        assertThat(commandResponse).isNotNull();
        assertThat(propertyService.findByDomainAndKey(project, "key")).isPresent();
    }

    @Test
    void editValidConfigurationWithSuccess() {
        Project project = builder.givenAProject();
        Property property = builder.givenAProperty(project);

        assertThat(propertyService.findByDomainAndKey(project, "key")).isPresent();

        CommandResponse commandResponse = propertyService.update(
            property,
            property.toJsonObject().withChange("value", "NEW VALUE")
        );

        assertThat(commandResponse).isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(200);
        assertThat(propertyService.findByDomainAndKey(project, "key")).isPresent();
        Property edited = propertyService.findByDomainAndKey(project, "key").get();
        assertThat(edited.getValue()).isEqualTo("NEW VALUE");
    }


    @Test
    public void deleteProperty() {
        Property property = builder.givenAProperty(builder.givenAProject());
        propertyService.delete(property, null, null, false);
        assertThat(propertyService.findById(property.getId())).isEmpty();
    }

    @Test
    public void listKeysForAnnotaion() {
        Project project = builder.givenAProject();
        UserAnnotation
            userAnnotation
            = builder.persistAndReturn(builder.givenANotPersistedUserAnnotation(project));
        Property property = builder.givenAProperty(userAnnotation);
        Property projectProperty = builder.givenAProperty(project, "projectKey", "value");

        List<Map<String, Object>> results = propertyService.listKeysForAnnotation(project, null, false);

        assertThat(results.stream().map(x -> (String) x.get("key"))).containsExactly(property.getKey())
            .doesNotContain(projectProperty.getKey());
    }

    @Test
    public void listKeysForAnnotationByImageWithUser() {
        Project project = builder.givenAProject();
        UserAnnotation
            userAnnotation
            = builder.persistAndReturn(builder.givenANotPersistedUserAnnotation(project));
        Property property = builder.givenAProperty(userAnnotation);
        Property projectProperty = builder.givenAProperty(project, "projectKey", "value");

        List<Map<String, Object>> results = propertyService.listKeysForAnnotation(
            null,
            userAnnotation.getImage(),
            true
        );

        assertThat(results.stream().map(x -> (String) x.get("key"))).containsExactly(property.getKey())
            .doesNotContain(projectProperty.getKey());
        assertThat(results.stream().map(x -> (Long) x.get("user"))).containsExactly(builder.givenSuperAdmin().getId());
    }

    @Test
    public void listKeysForImageInstance() {
        Project project = builder.givenAProject();
        ImageInstance imageInstance = builder.givenAnImageInstance(project);
        Property property = builder.givenAProperty(imageInstance);
        Property projectProperty = builder.givenAProperty(project, "projectKey", "value");

        List<String> results = propertyService.listKeysForImageInstance(imageInstance.getProject());

        assertThat(results).containsExactly(property.getKey()).doesNotContain(projectProperty.getKey());
    }

    @Test
    public void selectCenterAnnotation() throws ParseException {
        Project project = builder.givenAProject();
        User user = builder.givenSuperAdmin();
        ImageInstance imageInstance = builder.givenAnImageInstance(project);
        UserAnnotation annotation = builder.givenAUserAnnotation();
        annotation.setLocation(new WKTReader().read("POLYGON ((0 0, 0 1000, 1000 1000, 1000 0, 0 0))"));
        annotation.setImage(imageInstance);
        Property property = builder.persistAndReturn(builder.givenANotPersistedProperty(
            annotation,
            "TestCytomine",
            "ValueTestCytomine"
        ));

        List<Map<String, Object>> results = propertyService.listAnnotationCenterPosition(
            user,
            imageInstance,
            GeometryUtils.createBoundingBox("0,0,1000,1000"),
            "TestCytomine"
        );

        assertThat(results).hasSize(1);
        assertThat(results.get(0).get("idAnnotation")).isEqualTo(annotation.getId());
    }
}
