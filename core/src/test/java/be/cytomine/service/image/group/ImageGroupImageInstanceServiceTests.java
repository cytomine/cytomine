package be.cytomine.service.image.group;

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
import be.cytomine.domain.image.ImageInstance;
import be.cytomine.domain.image.group.ImageGroup;
import be.cytomine.domain.image.group.ImageGroupImageInstance;
import be.cytomine.domain.project.Project;
import be.cytomine.exceptions.WrongArgumentException;
import be.cytomine.utils.CommandResponse;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
@Transactional
public class ImageGroupImageInstanceServiceTests {

    @Autowired
    private BasicInstanceBuilder builder;

    @Autowired
    private ImageGroupImageInstanceService imageGroupImageInstanceService;

    @Test
    void getNonExistingImagegroupImageinstanceReturnNull() {
        Project project = builder.givenAProject();
        ImageGroup group = builder.givenAnImageGroup(project);
        ImageInstance image = builder.givenAnImageInstance(project);
        AssertionsForClassTypes.assertThat(imageGroupImageInstanceService.get(group, image)).isNull();
    }

    @Test
    void findImagegroupImageinstanceWithSuccess() {
        ImageGroupImageInstance igii = builder.givenAnImageGroupImageInstance();
        AssertionsForClassTypes.assertThat(imageGroupImageInstanceService.find(igii.getGroup(), igii.getImage())
            .isPresent());
        assertThat(igii).isEqualTo(imageGroupImageInstanceService.find(igii.getGroup(), igii.getImage()).get());
    }

    @Test
    void listImagegroupImageinstanceByImageinstance() {
        Project project = builder.givenAProject();
        ImageGroup group = builder.givenAnImageGroup(project);
        ImageInstance image = builder.givenAnImageInstance(project);

        ImageGroupImageInstance igii1 = builder.givenAnImageGroupImageInstance(group, image);
        ImageGroupImageInstance igii2 = builder.givenAnImageGroupImageInstance(group, image);
        ImageGroupImageInstance igii3 = builder.givenAnImageGroupImageInstance(group, image);
        ImageGroupImageInstance igii4 = builder.givenAnImageGroupImageInstance();

        AssertionsForInterfaceTypes.assertThat(imageGroupImageInstanceService.list(image))
            .containsExactly(igii1, igii2, igii3);
        AssertionsForInterfaceTypes.assertThat(imageGroupImageInstanceService.list(image)).doesNotContain(igii4);
    }

    @Test
    void listImagegroupImageinstanceByImagegroup() {
        Project project = builder.givenAProject();
        ImageGroup group = builder.givenAnImageGroup(project);
        ImageInstance image = builder.givenAnImageInstance(project);

        ImageGroupImageInstance igii1 = builder.givenAnImageGroupImageInstance(group, image);
        ImageGroupImageInstance igii2 = builder.givenAnImageGroupImageInstance(group, image);
        ImageGroupImageInstance igii3 = builder.givenAnImageGroupImageInstance(group, image);
        ImageGroupImageInstance igii4 = builder.givenAnImageGroupImageInstance();

        AssertionsForInterfaceTypes.assertThat(imageGroupImageInstanceService.list(group))
            .containsExactly(igii1, igii2, igii3);
        AssertionsForInterfaceTypes.assertThat(imageGroupImageInstanceService.list(group)).doesNotContain(igii4);
    }

    @Test
    void addValidImagegroupImageinstanceWithSuccess() {
        ImageGroupImageInstance igii = builder.givenANotPersistedImageGroupImageInstance();

        CommandResponse commandResponse = imageGroupImageInstanceService.add(igii.toJsonObject());

        AssertionsForClassTypes.assertThat(commandResponse).isNotNull();
        AssertionsForClassTypes.assertThat(commandResponse.getStatus()).isEqualTo(200);
        ImageGroupImageInstance response = (ImageGroupImageInstance) commandResponse.getObject();
        AssertionsForClassTypes.assertThat(imageGroupImageInstanceService.find(
            response.getGroup(),
            response.getImage()
        )).isPresent();
    }

    @Test
    void addImagegroupImageinstanceWithWrongGroupFails() {
        ImageGroupImageInstance igii = builder.givenAnImageGroupImageInstance();
        Assertions.assertThrows(
            WrongArgumentException.class,
            () -> imageGroupImageInstanceService.add(igii.toJsonObject()
                .withChange("group", builder.givenAnImageGroup().getId()))
        );
    }

    @Test
    void addImagegroupImageinstanceWithWrongImageFails() {
        ImageGroupImageInstance igii = builder.givenAnImageGroupImageInstance();
        Assertions.assertThrows(
            WrongArgumentException.class,
            () -> imageGroupImageInstanceService.add(igii.toJsonObject()
                .withChange("image", builder.givenAnImageInstance().getId()))
        );
    }

    @Test
    void deleteImagegroupImageinstanceWithSuccess() {
        ImageGroupImageInstance igii = builder.givenAnImageGroupImageInstance();

        CommandResponse commandResponse = imageGroupImageInstanceService.delete(igii, null, null, true);

        assertThat(commandResponse).isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(200);
        AssertionsForClassTypes.assertThat(imageGroupImageInstanceService.find(igii.getGroup(), igii.getImage())
            .isEmpty());
    }
}
