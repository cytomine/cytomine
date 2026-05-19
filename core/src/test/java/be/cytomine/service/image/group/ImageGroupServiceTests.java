package be.cytomine.service.image.group;

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
import be.cytomine.domain.image.group.ImageGroup;
import be.cytomine.domain.project.Project;
import be.cytomine.exceptions.ObjectNotFoundException;
import be.cytomine.utils.CommandResponse;
import be.cytomine.utils.JsonObject;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
@Transactional
public class ImageGroupServiceTests {

    @Autowired
    BasicInstanceBuilder builder;

    @Autowired
    ImageGroupService imageGroupService;

    @Test
    void getNonExistingImagegroupReturnNull() {
        AssertionsForClassTypes.assertThat(imageGroupService.get(0L)).isNull();
    }

    @Test
    void findImagegroupWithSuccess() {
        ImageGroup imageGroup = builder.givenAnImageGroup();
        AssertionsForClassTypes.assertThat(imageGroupService.find(imageGroup.getId()).isPresent());
        assertThat(imageGroup).isEqualTo(imageGroupService.find(imageGroup.getId()).get());
    }

    @Test
    void findNonExistingImagegroupReturnEmpty() {
        AssertionsForClassTypes.assertThat(imageGroupService.find(0L)).isEmpty();
    }

    @Test
    void listImagegroupByProject() {
        Project project = builder.givenAProject();

        ImageGroup imageGroup1 = builder.givenAnImageGroup(project);
        ImageGroup imageGroup2 = builder.givenAnImageGroup(project);
        ImageGroup imageGroup3 = builder.givenAnImageGroup(project);
        ImageGroup imageGroup4 = builder.givenAnImageGroup();

        assertThat(imageGroupService.list(project)).containsExactly(imageGroup1, imageGroup2, imageGroup3);
        assertThat(imageGroupService.list(project)).doesNotContain(imageGroup4);
    }

    @Test
    void addValidImagegroupWithSuccess() {
        ImageGroup imageGroup = builder.givenANotPersistedImagegroup();

        CommandResponse commandResponse = imageGroupService.add(imageGroup.toJsonObject());

        AssertionsForClassTypes.assertThat(commandResponse).isNotNull();
        AssertionsForClassTypes.assertThat(commandResponse.getStatus()).isEqualTo(200);
        AssertionsForClassTypes.assertThat(imageGroupService.find(commandResponse.getObject().getId())).isPresent();
    }

    @Test
    void addImagegroupWithNullProjectFails() {
        ImageGroup imageGroup = builder.givenAnImageGroup();
        Assertions.assertThrows(
            ObjectNotFoundException.class, () -> {
                imageGroupService.add(imageGroup.toJsonObject().withChange("project", null));
            }
        );
    }

    @Test
    void editImagegroupWithSuccess() {
        Project project1 = builder.givenAProject();
        Project project2 = builder.givenAProject();
        ImageGroup imageGroup = builder.givenAnImageGroup(project1);

        JsonObject jsonObject = imageGroup.toJsonObject();
        jsonObject.put("project", project2.getId());

        CommandResponse commandResponse = imageGroupService.edit(jsonObject, true);
        assertThat(commandResponse).isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(200);
        AssertionsForClassTypes.assertThat(imageGroupService.find(commandResponse.getObject().getId())).isPresent();
        ImageGroup updated = imageGroupService.find(commandResponse.getObject().getId()).get();

        assertThat(updated.getProject()).isEqualTo(project2);
    }

    @Test
    void deleteImagegroupWithSuccess() {
        ImageGroup imageGroup = builder.givenAnImageGroup();

        CommandResponse commandResponse = imageGroupService.delete(imageGroup, null, null, true);

        assertThat(commandResponse).isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(200);
        AssertionsForClassTypes.assertThat(imageGroupService.find(imageGroup.getId()).isEmpty());
    }
}
