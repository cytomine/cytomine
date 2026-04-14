package be.cytomine.service.utils;

import jakarta.transaction.Transactional;
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
import be.cytomine.domain.ontology.Term;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.User;
import be.cytomine.repository.AnnotationListing;
import be.cytomine.utils.JsonObject;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = "ROLE_SUPER_ADMIN", username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
@Transactional
public class ParamServiceTests {

    @Autowired
    ParamsService paramsService;

    @Autowired
    BasicInstanceBuilder builder;

    @Test
    public void paramsUser() {
        Project project = builder.givenAProject();
        User userInProject = builder.givenAUser();
        builder.addUserToProject(project, userInProject.getUsername());
        User userNotInProject = builder.givenAUser();

        assertThat(paramsService.getParamsUserList(null, project))
            .contains(userInProject.getId()).doesNotContain(userNotInProject.getId());
        assertThat(paramsService.getParamsUserList("null", project))
            .contains(userInProject.getId()).doesNotContain(userNotInProject.getId());
        assertThat(paramsService.getParamsUserList(userInProject.getId() + "_" + userNotInProject.getId(), project))
            .contains(userInProject.getId()).doesNotContain(userNotInProject.getId());
        assertThat(paramsService.getParamsUserList(userNotInProject.getId() + "", project))
            .doesNotContain(userInProject.getId(), userNotInProject.getId());
    }

    @Test
    public void paramsImageInstance() {
        Project project = builder.givenAProject();
        ImageInstance imageInstanceInProject = builder.givenAnImageInstance(project);
        ImageInstance imageInstanceNotInProject = builder.givenAnImageInstance();

        assertThat(paramsService.getParamsImageInstanceList(null, project))
            .contains(imageInstanceInProject.getId()).doesNotContain(imageInstanceNotInProject.getId());
        assertThat(paramsService.getParamsImageInstanceList("null", project))
            .contains(imageInstanceInProject.getId()).doesNotContain(imageInstanceNotInProject.getId());
        assertThat(paramsService.getParamsImageInstanceList(
            imageInstanceInProject.getId()
                + "_"
                + imageInstanceNotInProject.getId(), project
        ))
            .contains(imageInstanceInProject.getId()).doesNotContain(imageInstanceNotInProject.getId());
        assertThat(paramsService.getParamsImageInstanceList(imageInstanceNotInProject.getId() + "", project))
            .doesNotContain(imageInstanceInProject.getId(), imageInstanceNotInProject.getId());
    }

    @Test
    public void paramsTerm() {

        Term termInProject = builder.givenATerm(builder.givenAnOntology());
        Term termNotInProject = builder.givenATerm();
        Project project = builder.givenAProjectWithOntology(termInProject.getOntology());

        assertThat(paramsService.getParamsTermList(null, project))
            .contains(termInProject.getId()).doesNotContain(termNotInProject.getId());
        assertThat(paramsService.getParamsTermList("null", project))
            .contains(termInProject.getId()).doesNotContain(termNotInProject.getId());
        assertThat(paramsService.getParamsTermList(termInProject.getId() + "_" + termNotInProject.getId(), project))
            .contains(termInProject.getId()).doesNotContain(termNotInProject.getId());
        assertThat(paramsService.getParamsTermList(termNotInProject.getId() + "", project))
            .doesNotContain(termInProject.getId(), termNotInProject.getId());
    }

    @Test
    public void propertyGroupToShow() {
        assertThat(paramsService.getPropertyGroupToShow(new JsonObject()))
            .containsExactlyElementsOf(AnnotationListing.availableColumnsDefault);

        assertThat(paramsService.getPropertyGroupToShow(JsonObject.of("showGIS", true)))
            .contains("gis");

        assertThat(paramsService.getPropertyGroupToShow(JsonObject.of("hideTerm", true)))
            .doesNotContain("term");
    }
}
