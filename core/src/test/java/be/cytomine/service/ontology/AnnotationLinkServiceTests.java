package be.cytomine.service.ontology;

import jakarta.transaction.Transactional;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.api.AssertionsForInterfaceTypes;
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
import be.cytomine.domain.ontology.AnnotationGroup;
import be.cytomine.domain.ontology.AnnotationLink;
import be.cytomine.domain.project.Project;
import be.cytomine.utils.CommandResponse;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
@Transactional
public class AnnotationLinkServiceTests {

    @Autowired
    BasicInstanceBuilder builder;

    @Autowired
    AnnotationLinkService annotationLinkService;

    @Test
    void findAnnotationLinkWithSuccess() {
        AnnotationLink annotationLink = builder.givenAnAnnotationLink();
        AssertionsForClassTypes.assertThat(annotationLinkService.find(annotationLink.getId()).isPresent());
        assertThat(annotationLink).isEqualTo(annotationLinkService.find(annotationLink.getId()).get());
    }

    @Test
    void findNonExistingAnnotationLinkReturnEmpty() {
        AssertionsForClassTypes.assertThat(annotationLinkService.find(0L)).isEmpty();
    }

    @Test
    void getNonExistingAnnotationLinkReturnNull() {
        AssertionsForClassTypes.assertThat(annotationLinkService.get(0L)).isNull();
    }

    @Test
    void listAnnotationLinkByAnnotationGroup() {
        Project project = builder.givenAProject();
        AnnotationGroup annotationGroup = builder.givenAnAnnotationGroup(
            project,
            builder.givenAnImageGroup(project)
        );
        ImageInstance image = builder.givenAnImageInstance(project);

        AnnotationLink annotationLink1 = builder.givenAnAnnotationLink(
            builder.givenAUserAnnotation(project),
            annotationGroup,
            image
        );
        AnnotationLink annotationLink2 = builder.givenAnAnnotationLink(
            builder.givenAUserAnnotation(project),
            annotationGroup,
            image
        );
        AnnotationLink annotationLink3 = builder.givenAnAnnotationLink(
            builder.givenAUserAnnotation(project),
            annotationGroup,
            image
        );
        AnnotationLink annotationLink4 = builder.givenAnAnnotationLink();

        AssertionsForInterfaceTypes.assertThat(annotationLinkService.list(annotationGroup))
            .containsExactly(annotationLink1, annotationLink2, annotationLink3);
        AssertionsForInterfaceTypes.assertThat(annotationLinkService.list(annotationGroup))
            .doesNotContain(annotationLink4);
    }

    @Test
    void addValidAnnotationLinkWithSuccess() {
        AnnotationLink annotationLink = builder.givenANotPersistedAnnotationLink();

        CommandResponse commandResponse = annotationLinkService.add(annotationLink.toJsonObject());

        AssertionsForClassTypes.assertThat(commandResponse).isNotNull();
        AssertionsForClassTypes.assertThat(commandResponse.getStatus()).isEqualTo(200);
        AssertionsForClassTypes.assertThat(annotationLinkService.find(commandResponse.getObject().getId())).isPresent();
    }

    @Test
    void deleteAnnotationLinkWithSuccess() {
        AnnotationLink annotationLink = builder.givenAnAnnotationLink();

        CommandResponse commandResponse = annotationLinkService.delete(annotationLink, null, null, true);

        assertThat(commandResponse).isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(200);
        AssertionsForClassTypes.assertThat(annotationLinkService.find(annotationLink.getId()).isEmpty());
    }
}
