package be.cytomine.controller.social;

import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.repository.model.command.payload.response.UserResponse;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.config.WiremockRepository;
import be.cytomine.domain.ontology.AnnotationDomain;
import be.cytomine.domain.security.User;
import be.cytomine.domain.social.AnnotationAction;
import be.cytomine.repositorynosql.social.AnnotationActionRepository;
import be.cytomine.service.CurrentUserService;
import be.cytomine.service.social.AnnotationActionService;
import be.cytomine.utils.JsonObject;

import static be.cytomine.authorization.AbstractAuthorizationTest.SUPERADMIN;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(username = SUPERADMIN)
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class, WiremockRepository.class})
public class AnnotationActionResourceTests {

    @Autowired
    private BasicInstanceBuilder builder;

    @Autowired
    private MockMvc restUserPositionControllerMockMvc;

    @Autowired
    private AnnotationActionRepository annotationActionRepository;

    @Autowired
    private AnnotationActionService annotationActionService;

    @Autowired
    private CurrentUserService currentUserService;

    @BeforeEach
    public void cleanDB() {
        annotationActionRepository.deleteAll();
    }

    AnnotationAction givenAPersistentAnnotationAction(
        Date creation,
        AnnotationDomain annotationDomain,
        long userId,
        String action
    ) {
        return annotationActionService.add(
            annotationDomain,
            userId,
            action,
            creation
        );
    }

    @Test
    public void addActionForAnnotation() throws Exception {
        AssertionsForClassTypes.assertThat(annotationActionRepository.count()).isEqualTo(0);
        long userId = currentUserService.getCurrentUser().id();
        AnnotationDomain annotationDomain = builder.givenAUserAnnotation();

        JsonObject jsonObject = new JsonObject();
        jsonObject.put("annotationIdent", annotationDomain.getId());
        jsonObject.put("action", "select");

        restUserPositionControllerMockMvc.perform(post("/api/annotation_action.json")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toJsonString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.class").value("be.cytomine.domain.social.AnnotationAction"))
            .andExpect(jsonPath("$.class").value("be.cytomine.domain.social.AnnotationAction"))
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.created").exists())
            .andExpect(jsonPath("$.user").value(userId))
            .andExpect(jsonPath("$.image").value(annotationDomain.getImage().getId()))
            .andExpect(jsonPath("$.project").value(annotationDomain.getProject().getId()))
            .andExpect(jsonPath("$.action").value("select"))
            .andExpect(jsonPath("$.annotationIdent").value(annotationDomain.getId()))
            .andExpect(jsonPath("$.annotationClassName").value(annotationDomain.getClass().getName()))
            .andExpect(jsonPath("$.annotationCreator").value(annotationDomain.getUserId()));

        AssertionsForClassTypes.assertThat(annotationActionRepository.count()).isEqualTo(1);
    }

    @Test
    @Transactional
    public void listLastUserOnImage() throws Exception {
        UserResponse user = builder.givenUserAclRead();

        AnnotationDomain annotationDomain = builder.givenAUserAnnotation();
        givenAPersistentAnnotationAction(new Date(), annotationDomain, user.id(), "select");

        restUserPositionControllerMockMvc.perform(get(
                "/api/imageinstance/{image}/annotation_action.json",
                annotationDomain.getImage().getId()
            )
                .param("user", String.valueOf(user.id())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(equalTo(1))));

        restUserPositionControllerMockMvc.perform(get(
                "/api/imageinstance/{image}/annotation_action.json",
                builder.givenAnImageInstance().getId()
            )
                .param("user", String.valueOf(user.id())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(equalTo(0))));
    }

    @Test
    @Transactional
    public void listLastUserOnSlice() throws Exception {
        UserResponse user = builder.givenUserAclRead();

        AnnotationDomain annotationDomain = builder.givenAUserAnnotation();
        givenAPersistentAnnotationAction(new Date(), annotationDomain, user.id(), "select");

        restUserPositionControllerMockMvc.perform(get(
                "/api/sliceinstance/{image}/annotation_action.json",
                annotationDomain.getSlice().getId()
            )
                .param("user", String.valueOf(user.id())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(equalTo(1))));

        restUserPositionControllerMockMvc.perform(get(
                "/api/sliceinstance/{image}/annotation_action.json",
                builder.givenASliceInstance().getId()
            )
                .param("user", String.valueOf(user.id())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(equalTo(0))));
    }

    @Test
    @Transactional
    public void countAnnotationByProject() throws Exception {
        UserResponse user = builder.givenSuperAdmin();
        AnnotationDomain annotationDomain = builder.givenAUserAnnotation();
        givenAPersistentAnnotationAction(DateUtils.addDays(new Date(), -2), annotationDomain, user.id(), "select");

        restUserPositionControllerMockMvc.perform(get(
                "/api/project/{project}/annotation_action/count.json",
                annotationDomain.getProject().getId()
            )
                .param("startDate", "" + DateUtils.addDays(new Date(), -10).getTime())
                .param("endDate", "" + DateUtils.addDays(new Date(), 10).getTime()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value("1"));

        restUserPositionControllerMockMvc.perform(get(
                "/api/project/{project}/annotation_action/count.json",
                annotationDomain.getProject().getId()
            )
                .param("endDate", "" + DateUtils.addDays(new Date(), -5).getTime()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value("0"));

        restUserPositionControllerMockMvc.perform(get(
                "/api/project/{project}/annotation_action/count.json",
                annotationDomain.getProject().getId()
            )
                .param("startDate", "" + DateUtils.addDays(new Date(), -1).getTime()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value("0"));

        restUserPositionControllerMockMvc.perform(get(
                "/api/project/{project}/annotation_action/count.json",
                annotationDomain.getProject().getId()
            )
                .param("startDate", "" + DateUtils.addDays(new Date(), -10).getTime()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value("1"));

    }
}
