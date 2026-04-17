package be.cytomine.controller.stats;

import java.util.Date;
import java.util.List;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import jakarta.persistence.EntityManager;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.domain.image.ImageInstance;
import be.cytomine.domain.ontology.AnnotationDomain;
import be.cytomine.domain.ontology.AnnotationTerm;
import be.cytomine.domain.ontology.ReviewedAnnotation;
import be.cytomine.domain.ontology.UserAnnotation;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.User;
import be.cytomine.domain.social.AnnotationAction;
import be.cytomine.domain.social.PersistentImageConsultation;
import be.cytomine.domain.social.PersistentProjectConnection;
import be.cytomine.repositorynosql.social.LastConnectionRepository;
import be.cytomine.repositorynosql.social.LastUserPositionRepository;
import be.cytomine.repositorynosql.social.PersistentConnectionRepository;
import be.cytomine.repositorynosql.social.PersistentImageConsultationRepository;
import be.cytomine.repositorynosql.social.PersistentProjectConnectionRepository;
import be.cytomine.repositorynosql.social.PersistentUserPositionRepository;
import be.cytomine.repositorynosql.social.ProjectConnectionRepository;
import be.cytomine.service.social.AnnotationActionService;
import be.cytomine.service.social.ImageConsultationService;
import be.cytomine.service.social.ProjectConnectionService;
import be.cytomine.utils.JsonObject;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = "ROLE_SUPER_ADMIN", username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
@Transactional
public class StatsResourceTests {

    @Autowired
    private BasicInstanceBuilder builder;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ImageConsultationService imageConsultationService;

    @Autowired
    private ProjectConnectionService projectConnectionService;

    @Autowired
    private PersistentConnectionRepository persistentConnectionRepository;

    @Autowired
    private LastConnectionRepository lastConnectionRepository;

    @Autowired
    private PersistentImageConsultationRepository persistentImageConsultationRepository;

    @Autowired
    private PersistentProjectConnectionRepository persistentProjectConnectionRepository;

    @Autowired
    private ProjectConnectionRepository projectConnectionRepository;

    @Autowired
    private PersistentUserPositionRepository persistentUserPositionRepository;

    @Autowired
    private LastUserPositionRepository lastUserPositionRepository;

    @Autowired
    private AnnotationActionService annotationActionService;

    @Autowired
    private MockMvc restStatsControllerMockMvc;

    private static WireMockServer wireMockServer = new WireMockServer(8888);

    @BeforeAll
    public static void beforeAll() {
        wireMockServer.start();
    }

    @AfterAll
    public static void afterAll() {
        wireMockServer.stop();
    }

    @BeforeEach
    public void init() {
        persistentConnectionRepository.deleteAll();
        lastConnectionRepository.deleteAll();
        persistentImageConsultationRepository.deleteAll();
        persistentProjectConnectionRepository.deleteAll();
        projectConnectionRepository.deleteAll();
        lastUserPositionRepository.deleteAll();
        persistentUserPositionRepository.deleteAll();
        wireMockServer.stubFor(WireMock.get(WireMock.urlPathMatching("/term_relations/ontology/.*"))
            .willReturn(WireMock.aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("[]")));
    }

    PersistentProjectConnection givenAPersistentConnectionInProject(User user, Project project, Date created) {
        PersistentProjectConnection connection = projectConnectionService.add(
            user,
            project,
            "xxx",
            "linux",
            "chrome",
            "123",
            created
        );
        return connection;
    }

    PersistentImageConsultation givenAPersistentImageConsultation(
        User user,
        ImageInstance imageInstance,
        Date created
    ) {
        return imageConsultationService.add(user, imageInstance.getId(), "xxx", "mode", created);
    }

    AnnotationAction givenAPersistentAnnotationAction(
        Date creation,
        AnnotationDomain annotationDomain,
        User user,
        String action
    ) {
        return annotationActionService.add(
            annotationDomain,
            user,
            action,
            creation
        );
    }

    @Test
    void statsTerm() throws Exception {
        Project project = builder.givenAProject();

        restStatsControllerMockMvc.perform(get("/api/project/{project}/stats/term.json", project.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(equalTo(1))));

        AnnotationTerm annotationTerm = builder.givenAnAnnotationTerm(builder.givenAUserAnnotation(project));
        entityManager.refresh(project.getOntology());

        restStatsControllerMockMvc.perform(get("/api/project/{project}/stats/term.json", project.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(equalTo(2))))
            .andExpect(jsonPath("$.collection[?(@.key=='" + annotationTerm.getTerm().getName() + "')].id").value(
                annotationTerm.getTerm().getId().intValue()))
            .andExpect(jsonPath("$.collection[?(@.key=='" + annotationTerm.getTerm().getName() + "')].value").value(1));

        restStatsControllerMockMvc.perform(get("/api/project/{project}/stats/term.json", project.getId())
                .param("startDate", String.valueOf(DateUtils.addDays(new Date(), -10).getTime()))
                .param("endDate", String.valueOf(new Date().getTime())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(equalTo(2))));
    }

    @Test
    void statsUser() throws Exception {
        Project project = builder.givenAProject();
        builder.addUserToProject(project, "superadmin");
        UserAnnotation annotation1 = builder.givenAUserAnnotation(project);
        annotation1.setCreated(DateUtils.addDays(new Date(), -1));
        builder.persistAndReturn(annotation1);
        entityManager.refresh(annotation1);
        UserAnnotation annotation2 = builder.givenAUserAnnotation(project);
        annotation2.setCreated(DateUtils.addDays(new Date(), -1));
        builder.persistAndReturn(annotation2);

        restStatsControllerMockMvc.perform(get("/api/project/{project}/stats/user.json", project.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(equalTo(1))))
            .andExpect(jsonPath("$.collection[0].id").value(builder.givenSuperAdmin().getId().intValue()))
            .andExpect(jsonPath("$.collection[0].value").value(2));

        restStatsControllerMockMvc.perform(get("/api/project/{project}/stats/user.json", project.getId())
                .param("startDate", String.valueOf(DateUtils.addDays(new Date(), -20).getTime()))
                .param("endDate", String.valueOf(DateUtils.addDays(new Date(), -10).getTime())))
            .andExpect(status().isOk());
    }

    @Test
    void statsTermSlide() throws Exception {
        Project project = builder.givenAProject();

        restStatsControllerMockMvc.perform(get("/api/project/{project}/stats/termslide.json", project.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(equalTo(1))));

        builder.givenATerm(project.getOntology());

        restStatsControllerMockMvc.perform(get("/api/project/{project}/stats/termslide.json", project.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(equalTo(2))));

        restStatsControllerMockMvc.perform(get("/api/project/{project}/stats/termslide.json", project.getId())
                .param("startDate", String.valueOf(DateUtils.addDays(new Date(), -20).getTime()))
                .param("endDate", String.valueOf(DateUtils.addDays(new Date(), -10).getTime())))
            .andExpect(status().isOk());
    }

    @Test
    void shouldReturnTermImageStatsPerProject() throws Exception {
        Project project = builder.givenAProject();

        restStatsControllerMockMvc.perform(get("/api/project/{project}/stats/termimage.json", project.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(equalTo(0))));

        AnnotationTerm annotationTerm = builder.givenAnAnnotationTerm(builder.givenAUserAnnotation(project));
        entityManager.refresh(project.getOntology());

        restStatsControllerMockMvc.perform(get("/api/project/{project}/stats/termimage.json", project.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(equalTo(1))))
            .andExpect(jsonPath("$.collection[0].countAnnotations").value(1));

        restStatsControllerMockMvc.perform(get("/api/project/{project}/stats/termimage.json", project.getId())
                .param("startDate", String.valueOf(DateUtils.addDays(new Date(), -20).getTime()))
                .param("endDate", String.valueOf(DateUtils.addDays(new Date(), -10).getTime())))
            .andExpect(status().isOk());
    }

    @Test
    void statsUderSlide() throws Exception {
        Project project = builder.givenAProject();
        builder.addUserToProject(project, "superadmin");

        restStatsControllerMockMvc.perform(get("/api/project/{project}/stats/userslide.json", project.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(equalTo(1))))
            .andExpect(jsonPath("$.collection[0].id").value(builder.givenSuperAdmin().getId()))
            .andExpect(jsonPath("$.collection[0].value").value(0));
        ;

        UserAnnotation annotation1 = builder.givenAUserAnnotation(project);
        annotation1.setCreated(DateUtils.addDays(new Date(), -1));
        builder.persistAndReturn(annotation1);
        UserAnnotation annotation2 = builder.givenAUserAnnotation(project);
        annotation2.setCreated(DateUtils.addDays(new Date(), -10));
        builder.persistAndReturn(annotation2);

        restStatsControllerMockMvc.perform(get("/api/project/{project}/stats/userslide.json", project.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(equalTo(1))))
            .andExpect(jsonPath("$.collection[0].id").value(builder.givenSuperAdmin().getId()))
            .andExpect(jsonPath("$.collection[0].value").value(2));
        ;

        restStatsControllerMockMvc.perform(get("/api/project/{project}/stats/userslide.json", project.getId())
                .param("startDate", String.valueOf(DateUtils.addDays(new Date(), -20).getTime()))
                .param("endDate", String.valueOf(DateUtils.addDays(new Date(), -10).getTime())))
            .andExpect(status().isOk());
    }

    @Test
    void statsUserAnnotation() throws Exception {
        Project project = builder.givenAProject();
        builder.addUserToProject(project, "superadmin");
        UserAnnotation annotation1 = builder.givenAUserAnnotation(project);
        annotation1.setCreated(DateUtils.addDays(new Date(), -1));
        builder.givenAnAnnotationTerm(annotation1, builder.givenATerm(project.getOntology()));
        builder.persistAndReturn(annotation1);
        entityManager.refresh(annotation1);
        UserAnnotation annotation2 = builder.givenAUserAnnotation(project);
        annotation2.setCreated(DateUtils.addDays(new Date(), -1));
        builder.givenAnAnnotationTerm(annotation2, annotation1.getTerms().get(0));
        builder.persistAndReturn(annotation2);
        entityManager.refresh(annotation2);

        List<JsonObject> terms;

        restStatsControllerMockMvc.perform(get("/api/project/{project}/stats/userannotations.json", project.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(equalTo(1))))
            .andExpect(jsonPath("$.collection[0].id").value(builder.givenSuperAdmin().getId()))
            .andExpect(jsonPath("$.collection[0].terms[0].value").value(2));

    }


    @Test
    void statsUserAnnotationEvolution() throws Exception {
        Project project = builder.givenAProject();
        UserAnnotation annotation1 = builder.givenAUserAnnotation(project);
        annotation1.setCreated(DateUtils.addDays(new Date(), -1));
        builder.persistAndReturn(annotation1);
        UserAnnotation annotation2 = builder.givenAUserAnnotation(project);
        annotation2.setCreated(DateUtils.addDays(new Date(), -10));
        builder.persistAndReturn(annotation2);

        restStatsControllerMockMvc.perform(get("/api/project/{project}/stats/annotationevolution.json", project.getId())
                .param("daysRange", "7")
                .param("startDate", String.valueOf(DateUtils.addDays(new Date(), -18).getTime())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(equalTo(3))));

        restStatsControllerMockMvc.perform(get("/api/project/{project}/stats/annotationevolution.json", project.getId())
                .param("startDate", String.valueOf(DateUtils.addDays(new Date(), -20).getTime()))
                .param("endDate", String.valueOf(DateUtils.addDays(new Date(), -10).getTime())))
            .andExpect(status().isOk());
    }

    @Test
    void statsReviewedAnnotationEvolution() throws Exception {
        Project project = builder.givenAProject();
        ReviewedAnnotation annotation1 = builder.givenAReviewedAnnotation(project);
        annotation1.setCreated(DateUtils.addDays(new Date(), -1));
        builder.persistAndReturn(annotation1);
        ReviewedAnnotation annotation2 = builder.givenAReviewedAnnotation(project);
        annotation2.setCreated(DateUtils.addDays(new Date(), -10));
        builder.persistAndReturn(annotation2);

        restStatsControllerMockMvc.perform(get(
                "/api/project/{project}/stats/reviewedannotationevolution.json",
                project.getId()
            ))
            .andExpect(status().isOk());

        restStatsControllerMockMvc.perform(get(
                "/api/project/{project}/stats/reviewedannotationevolution.json",
                project.getId()
            )
                .param("startDate", String.valueOf(DateUtils.addDays(new Date(), -20).getTime()))
                .param("endDate", String.valueOf(DateUtils.addDays(new Date(), -10).getTime())))
            .andExpect(status().isOk());
    }

    @Test
    void statsAnnotationTermByProject() throws Exception {
        Project project = builder.givenAProject();
        builder.addUserToProject(project, "superadmin");
        AnnotationTerm annotationTerm = builder.givenAnAnnotationTerm(builder.givenAUserAnnotation(project));
        entityManager.refresh(annotationTerm.getUserAnnotation());

        restStatsControllerMockMvc.perform(get("/api/term/{id}/project/stat.json", annotationTerm.getTerm().getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(equalTo(1))))
            .andExpect(jsonPath("$.collection[0].key").value(project.getName()))
            .andExpect(jsonPath("$.collection[0].value").value(1L));
    }

    @Test
    void numberOfConnections() throws Exception {
        restStatsControllerMockMvc.perform(get("/api/total/project/connections.json"))
            .andExpect(status().isOk());
    }

    @Test
    void statsDomainCount() throws Exception {
        UserAnnotation annotation = builder.givenAUserAnnotation();

        restStatsControllerMockMvc.perform(get("/api/total/{domain}.json", annotation.getClass().getName()))
            .andExpect(status().isOk());
    }

    @Test
    void currentStats() throws Exception {
        restStatsControllerMockMvc.perform(get("/api/stats/currentStats.json"))
            .andExpect(status().isOk());
    }

    @Test
    void allGlobalStats() throws Exception {
        restStatsControllerMockMvc.perform(get("/api/stats/all.json"))
            .andExpect(status().isOk());
    }

    @Test
    void statsConnectionEvolution() throws Exception {
        Project project = builder.givenAProject();
        givenAPersistentConnectionInProject(
            builder.givenSuperAdmin(),
            project,
            DateUtils.addDays(new Date(), -15)
        );
        givenAPersistentConnectionInProject(
            builder.givenSuperAdmin(),
            project,
            DateUtils.addDays(new Date(), -15)
        );
        givenAPersistentConnectionInProject(
            builder.givenSuperAdmin(),
            project,
            DateUtils.addDays(new Date(), -5)
        );

        restStatsControllerMockMvc.perform(get(
                "/api/project/{project}/stats/connectionsevolution.json",
                project.getId()
            )
                .param("daysRange", "7")
                .param("startDate", String.valueOf(DateUtils.addDays(new Date(), -18).getTime())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(equalTo(3))));
    }

    @Test
    void statsImageConsultationEvolution() throws Exception {
        Project project = builder.givenAProject();
        ImageInstance imageInstance = builder.givenAnImageInstance(project);
        givenAPersistentImageConsultation(
            builder.givenSuperAdmin(),
            imageInstance,
            DateUtils.addDays(new Date(), -15)
        );
        givenAPersistentImageConsultation(
            builder.givenSuperAdmin(),
            imageInstance,
            DateUtils.addDays(new Date(), -15)
        );
        givenAPersistentImageConsultation(
            builder.givenSuperAdmin(),
            imageInstance,
            DateUtils.addDays(new Date(), -5)
        );

        restStatsControllerMockMvc.perform(get(
                "/api/project/{project}/stats/imageconsultationsevolution.json",
                project.getId()
            )
                .param("daysRange", "7")
                .param("startDate", String.valueOf(DateUtils.addDays(new Date(), -18).getTime())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(equalTo(3))));
    }

    @Test
    void shouldReturnAnnotationActionEvolutionStatsForProject() throws Exception {
        Project project = builder.givenAProject();
        AnnotationDomain annotation = builder.givenAUserAnnotation(project);
        givenAPersistentAnnotationAction(
            DateUtils.addDays(new Date(), -15),
            annotation,
            builder.givenSuperAdmin(),
            "select"
        );
        givenAPersistentAnnotationAction(
            DateUtils.addDays(new Date(), -15),
            annotation,
            builder.givenSuperAdmin(),
            "move"
        );
        givenAPersistentAnnotationAction(
            DateUtils.addDays(new Date(), -15),
            annotation,
            builder.givenSuperAdmin(),
            "select"
        );
        givenAPersistentAnnotationAction(
            DateUtils.addDays(new Date(), -5),
            annotation,
            builder.givenSuperAdmin(),
            "select"
        );

        restStatsControllerMockMvc.perform(get(
                "/api/project/{project}/stats/annotationactionsevolution.json",
                project.getId()
            )
                .param("daysRange", "7")
                .param("startDate", String.valueOf(DateUtils.addDays(new Date(), -18).getTime())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(equalTo(3))));
    }
}
