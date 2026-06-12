package be.cytomine.service.ontology;

import jakarta.transaction.Transactional;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.api.AssertionsForInterfaceTypes;
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
import be.cytomine.domain.image.group.ImageGroup;
import be.cytomine.domain.ontology.AnnotationGroup;
import be.cytomine.domain.project.Project;
import be.cytomine.exceptions.ObjectNotFoundException;
import be.cytomine.utils.CommandResponse;
import be.cytomine.utils.JsonObject;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
@Transactional
public class AnnotationGroupServiceTests {

    @Autowired
    BasicInstanceBuilder builder;

    @Autowired
    AnnotationGroupService annotationGroupService;

    @Test
    void findAnnotationGroupWithSuccess() {
        AnnotationGroup annotationGroup = builder.givenAnAnnotationGroup();
        AssertionsForClassTypes.assertThat(annotationGroupService.find(annotationGroup.getId()).isPresent());
        assertThat(annotationGroup).isEqualTo(annotationGroupService.find(annotationGroup.getId()).get());
    }

    @Test
    void findNonExistingAnnotationGroupReturnEmpty() {
        AssertionsForClassTypes.assertThat(annotationGroupService.find(0L)).isEmpty();
    }

    @Test
    void getNonExistingAnnotationGroupReturnNull() {
        AssertionsForClassTypes.assertThat(annotationGroupService.get(0L)).isNull();
    }

    @Test
    void listAnnotationGroupByProject() {
        Project project = builder.givenAProject();
        ImageGroup imageGroup = builder.givenAnImageGroup(project);

        AnnotationGroup annotationGroup1 = builder.givenAnAnnotationGroup(project, imageGroup);
        AnnotationGroup annotationGroup2 = builder.givenAnAnnotationGroup(project, imageGroup);
        AnnotationGroup annotationGroup3 = builder.givenAnAnnotationGroup(project, imageGroup);
        AnnotationGroup annotationGroup4 = builder.givenAnAnnotationGroup();

        AssertionsForInterfaceTypes.assertThat(annotationGroupService.list(project))
            .containsExactly(annotationGroup1, annotationGroup2, annotationGroup3);
        AssertionsForInterfaceTypes.assertThat(annotationGroupService.list(project)).doesNotContain(annotationGroup4);
    }

    @Test
    void listAnnotationGroupByImageGroup() {
        Project project = builder.givenAProject();
        ImageGroup imageGroup = builder.givenAnImageGroup(project);

        AnnotationGroup annotationGroup1 = builder.givenAnAnnotationGroup(project, imageGroup);
        AnnotationGroup annotationGroup2 = builder.givenAnAnnotationGroup(project, imageGroup);
        AnnotationGroup annotationGroup3 = builder.givenAnAnnotationGroup(project, imageGroup);
        AnnotationGroup annotationGroup4 = builder.givenAnAnnotationGroup();

        AssertionsForInterfaceTypes.assertThat(annotationGroupService.list(imageGroup))
            .containsExactly(annotationGroup1, annotationGroup2, annotationGroup3);
        AssertionsForInterfaceTypes.assertThat(annotationGroupService.list(imageGroup))
            .doesNotContain(annotationGroup4);
    }

    @Test
    void addValidAnnotationGroupWithSuccess() {
        AnnotationGroup annotationGroup = builder.givenANotPersistedAnnotationGroup();

        CommandResponse commandResponse = annotationGroupService.add(annotationGroup.toJsonObject());

        AssertionsForClassTypes.assertThat(commandResponse).isNotNull();
        AssertionsForClassTypes.assertThat(commandResponse.getStatus()).isEqualTo(200);
        AssertionsForClassTypes.assertThat(annotationGroupService.find(commandResponse.getObject().getId()))
            .isPresent();
    }

    @Test
    void addAnnotationGroupWithNullProjectFails() {
        AnnotationGroup annotationGroup = builder.givenAnAnnotationGroup();
        Assertions.assertThrows(
            ObjectNotFoundException.class,
            () -> annotationGroupService.add(annotationGroup.toJsonObject().withChange("project", null))
        );
    }

    @Test
    void editAnnotationGroupWithSuccess() {
        Project project1 = builder.givenAProject();
        Project project2 = builder.givenAProject();
        ImageGroup imageGroup = builder.givenAnImageGroup(project1);
        AnnotationGroup annotationGroup = builder.givenAnAnnotationGroup(project1, imageGroup);

        JsonObject jsonObject = annotationGroup.toJsonObject();
        jsonObject.put("project", project2.getId());

        CommandResponse commandResponse = annotationGroupService.edit(jsonObject, true);
        assertThat(commandResponse).isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(200);
        AssertionsForClassTypes.assertThat(annotationGroupService.find(commandResponse.getObject().getId()))
            .isPresent();

        AnnotationGroup updated = annotationGroupService.find(commandResponse.getObject().getId()).get();
        assertThat(updated.getProject()).isEqualTo(project2);
    }

    @Test
    void deleteAnnotationGroupWithSuccess() {
        AnnotationGroup annotationGroup = builder.givenAnAnnotationGroup();

        CommandResponse commandResponse = annotationGroupService.delete(annotationGroup, null, null, true);

        assertThat(commandResponse).isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(200);
        AssertionsForClassTypes.assertThat(annotationGroupService.find(annotationGroup.getId()).isEmpty());
    }

    @Test
    public void mergeAnnotationGroupWithSuccess() {
        Project project = builder.givenAProject();
        ImageGroup imageGroup = builder.givenAnImageGroup(project);
        AnnotationGroup annotationGroup = builder.givenAnAnnotationGroup(project, imageGroup);
        AnnotationGroup annotationGroupToMerge = builder.givenAnAnnotationGroup(project, imageGroup);

        CommandResponse commandResponse = annotationGroupService.merge(
            annotationGroup.getId(),
            annotationGroupToMerge.getId()
        );
        assertThat(commandResponse).isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(200);
        AssertionsForClassTypes.assertThat(annotationGroupService.find(annotationGroupToMerge.getId()).isEmpty());
        AssertionsForClassTypes.assertThat(annotationGroupService.find(annotationGroup.getId()).isPresent());
    }
}

