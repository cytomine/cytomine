package be.cytomine.service.stats;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.io.ParseException;
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
import be.cytomine.domain.ontology.AnnotationDomain;
import be.cytomine.domain.ontology.AnnotationTerm;
import be.cytomine.domain.ontology.ReviewedAnnotation;
import be.cytomine.domain.ontology.Term;
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

import static be.cytomine.service.middleware.ImageServerService.IMS_API_BASE_PATH;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = "ROLE_SUPER_ADMIN", username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
@Transactional
public class StatsServiceTests {

    @Autowired
    BasicInstanceBuilder builder;

    @Autowired
    EntityManager entityManager;

    @Autowired
    StatsService statsService;

    @Autowired
    ImageConsultationService imageConsultationService;

    @Autowired
    ProjectConnectionService projectConnectionService;

    @Autowired
    PersistentConnectionRepository persistentConnectionRepository;

    @Autowired
    LastConnectionRepository lastConnectionRepository;

    @Autowired
    PersistentImageConsultationRepository persistentImageConsultationRepository;

    @Autowired
    PersistentProjectConnectionRepository persistentProjectConnectionRepository;

    @Autowired
    ProjectConnectionRepository projectConnectionRepository;

    @Autowired
    PersistentUserPositionRepository persistentUserPositionRepository;

    @Autowired
    LastUserPositionRepository lastUserPositionRepository;

    @Autowired
    AnnotationActionService annotationActionService;

    @Autowired
    ObjectMapper objectMapper;

    private static WireMockServer wireMockServer = new WireMockServer(8888);

    @BeforeAll
    public static void beforeAll() {
        wireMockServer.start();
    }

    @AfterAll
    public static void afterAll() {
        try {
            wireMockServer.stop();
        } catch (Exception e) {
        }
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
    }

    PersistentProjectConnection given_a_persistent_connection_in_project(User user, Project project, Date created) {
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

    PersistentImageConsultation given_a_persistent_image_consultation(
        User user,
        ImageInstance imageInstance,
        Date created
    ) {
        return imageConsultationService.add(user, imageInstance.getId(), "xxx", "mode", created);
    }

    AnnotationAction given_a_persistent_annotation_action(
        Date creation,
        AnnotationDomain annotationDomain,
        User user,
        String action
    ) {
        AnnotationAction annotationAction =
            annotationActionService.add(
                annotationDomain,
                user,
                action,
                creation
            );
        return annotationAction;
    }


    @Test
    void stats_domain_count() {
        UserAnnotation annotation = builder.givenAUserAnnotation();
        assertThat(statsService.total(annotation.getClass())).isGreaterThanOrEqualTo(1);
        assertThat(statsService.total(annotation.getProject().getClass())).isGreaterThanOrEqualTo(1);
    }

    @Test
    void current_user_count() {
        assertThat(statsService.numberOfCurrentUsers()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void active_projects_count() {
        assertThat(statsService.numberOfActiveProjects()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void most_active_project_count() {
        Project project = builder.givenAProject();
        given_a_persistent_connection_in_project(builder.givenSuperAdmin(), project, new Date());
        assertThat(((JsonObject) statsService.mostActiveProjects()
            .get()
            .get("project")).getId()).isEqualTo(project.getId());
    }

    @Test
    void stats_annotation_term_by_project() {
        Project project = builder.givenAProject();
        builder.addUserToProject(project, "superadmin");
        AnnotationTerm annotationTerm = builder.givenAnAnnotationTerm(builder.givenAUserAnnotation(project));
        entityManager.refresh(annotationTerm.getUserAnnotation());

        List<JsonObject> jsonObjects = statsService.statAnnotationTermedByProject(annotationTerm.getTerm());
        assertThat(jsonObjects).hasSize(1);
        assertThat(jsonObjects.get(0).get("key")).isEqualTo(project.getName());
        assertThat(jsonObjects.get(0).get("value")).isEqualTo(1L);

    }


    @Test
    void stats_user_annotation_evolution() {
        Project project = builder.givenAProject();
        UserAnnotation annotation1 = builder.givenAUserAnnotation(project);
        annotation1.setCreated(DateUtils.addDays(new Date(), -1));
        builder.persistAndReturn(annotation1);
        UserAnnotation annotation2 = builder.givenAUserAnnotation(project);
        annotation2.setCreated(DateUtils.addDays(new Date(), -10));
        builder.persistAndReturn(annotation2);

        List<JsonObject> jsonObjects = statsService.statAnnotationEvolution(
            project,
            null,
            7,
            DateUtils.addDays(new Date(), -30),
            DateUtils.addDays(new Date(), 0),
            true,
            false
        );

        assertThat(jsonObjects).hasSize(5);
        assertThat(jsonObjects.stream()
            .filter(x -> x.getJSONAttrLong("size") == 1)
            .collect(Collectors.toList())).hasSize(2);

        statsService.statAnnotationEvolution(
            project,
            builder.givenATerm(project.getOntology()),
            7,
            DateUtils.addDays(new Date(), -30),
            DateUtils.addDays(new Date(), 0),
            true,
            false
        );

    }

    @Test
    void stats_reviewed_annotation_evolution() throws ParseException {
        Project project = builder.givenAProject();
        ReviewedAnnotation annotation1 = builder.givenAReviewedAnnotation(project);
        annotation1.setCreated(DateUtils.addDays(new Date(), -1));
        builder.persistAndReturn(annotation1);
        ReviewedAnnotation annotation2 = builder.givenAReviewedAnnotation(project);
        annotation2.setCreated(DateUtils.addDays(new Date(), -10));
        builder.persistAndReturn(annotation2);

        List<JsonObject> jsonObjects = statsService.statReviewedAnnotationEvolution(
            project,
            null,
            7,
            DateUtils.addDays(new Date(), -30),
            DateUtils.addDays(new Date(), 0),
            true,
            false
        );

        assertThat(jsonObjects).hasSize(5);
        assertThat(jsonObjects.stream()
            .filter(x -> x.getJSONAttrLong("size") == 1)
            .collect(Collectors.toList())).hasSize(2);

        statsService.statReviewedAnnotationEvolution(
            project,
            builder.givenATerm(project.getOntology()),
            7,
            DateUtils.addDays(new Date(), -30),
            DateUtils.addDays(new Date(), 0),
            true,
            false
        );


    }

    @Test
    void stats_uder_slide() {
        Project project = builder.givenAProject();
        builder.addUserToProject(project, "superadmin");

        List<JsonObject> results = statsService.statUserSlide(project, null, null);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(builder.givenSuperAdmin().getId());
        assertThat(results.get(0).get("value")).isEqualTo(0);

        UserAnnotation annotation1 = builder.givenAUserAnnotation(project);
        annotation1.setCreated(DateUtils.addDays(new Date(), -1));
        builder.persistAndReturn(annotation1);
        UserAnnotation annotation2 = builder.givenAUserAnnotation(project);
        annotation2.setCreated(DateUtils.addDays(new Date(), -10));
        builder.persistAndReturn(annotation2);

        results = statsService.statUserSlide(project, null, null);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(builder.givenSuperAdmin().getId());
        assertThat(results.get(0).get("value")).isEqualTo(2L);

        builder.addUserToProject(project, builder.givenAUser().getUsername());

        results = statsService.statUserSlide(project, null, null);

        assertThat(results).hasSize(2);

        results = statsService.statUserSlide(
            project,
            DateUtils.addDays(new Date(), -40),
            DateUtils.addDays(new Date(), -20)
        );

        assertThat(results).hasSize(2);
        assertThat(results.get(0).get("value")).isEqualTo(0);
        assertThat(results.get(1).get("value")).isEqualTo(0);
    }


    @Test
    void stats_term_slide() {
        Project project = builder.givenAProject();

        List<JsonObject> results = statsService.statTermSlide(project, null, null);
        results.removeIf(x -> x.get("id") == null);
        assertThat(results).hasSize(0);

        Term term = builder.givenATerm(project.getOntology());

        results = statsService.statTermSlide(project, null, null);
        results.removeIf(x -> x.get("id") == null);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(term.getId());
        assertThat(results.get(0).get("value")).isEqualTo(0);

        UserAnnotation annotation1 = builder.givenAUserAnnotation(project);
        annotation1.setCreated(DateUtils.addDays(new Date(), -1));
        builder.givenAnAnnotationTerm(annotation1, term);
        builder.persistAndReturn(annotation1);

        results = statsService.statTermSlide(project, null, null);
        results.removeIf(x -> x.get("id") == null);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(term.getId());
        assertThat(results.get(0).get("value")).isEqualTo(1L);

        builder.givenATerm(project.getOntology());

        results = statsService.statTermSlide(project, null, null);
        results.removeIf(x -> x.get("id") == null);
        assertThat(results).hasSize(2);

        results = statsService.statTermSlide(
            project,
            DateUtils.addDays(new Date(), -40),
            DateUtils.addDays(new Date(), -20)
        );
        results.removeIf(x -> x.get("id") == null);
        assertThat(results).hasSize(2);
        assertThat(results.get(0).get("value")).isEqualTo(0);
        assertThat(results.get(1).get("value")).isEqualTo(0);
    }


    @Test
    void stats_term() {
        Project project = builder.givenAProject();

        List<JsonObject> results = statsService.statTerm(project, null, null, false);

        assertThat(results).hasSize(1); //no term

        AnnotationTerm annotationTerm = builder.givenAnAnnotationTerm(builder.givenAUserAnnotation(project));
        entityManager.refresh(project.getOntology());
        results = statsService.statTerm(project, null, null, false);
        results.removeIf(x -> x.get("id") == null);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(annotationTerm.getTerm().getId());
        assertThat(results.get(0).get("value")).isEqualTo(1L);

        UserAnnotation annotation1 = builder.givenAUserAnnotation(project);
        annotation1.setCreated(DateUtils.addDays(new Date(), -1));
        builder.givenAnAnnotationTerm(annotation1, annotationTerm.getTerm());
        builder.persistAndReturn(annotation1);

        results = statsService.statTerm(project, null, null, false);
        results.removeIf(x -> x.get("id") == null);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(annotationTerm.getTerm().getId());
        assertThat(results.get(0).get("value")).isEqualTo(2L);


        results = statsService.statTerm(
            project,
            DateUtils.addDays(new Date(), -40),
            DateUtils.addDays(new Date(), -20),
            false
        );
        results.removeIf(x -> x.get("id") == null);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).get("value")).isEqualTo(0);
    }

    @Test
    void stat_per_term_and_image() {
        Project project = builder.givenAProject();

        List<JsonObject> results = statsService.statPerTermAndImage(project, null, null);

        assertThat(results).hasSize(0); //no annotations

        AnnotationTerm annotationTerm = builder.givenAnAnnotationTerm(builder.givenAUserAnnotation(project));
        entityManager.refresh(project.getOntology());

        results = statsService.statPerTermAndImage(project, null, null);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).get("term")).isEqualTo(annotationTerm.getTerm().getId());
        assertThat(results.get(0).get("image")).isEqualTo(annotationTerm.getUserAnnotation().getImage().getId());
        assertThat(results.get(0).get("countAnnotations")).isEqualTo(1L);

        AnnotationTerm
            annotationTermWithSameImageAndSameTerm
            = builder.givenAnAnnotationTerm(builder.givenAUserAnnotation(project));
        annotationTermWithSameImageAndSameTerm.getUserAnnotation()
            .setImage(annotationTerm.getUserAnnotation().getImage());
        annotationTermWithSameImageAndSameTerm.setTerm(annotationTerm.getTerm());

        results = statsService.statPerTermAndImage(project, null, null);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).get("term")).isEqualTo(annotationTerm.getTerm().getId());
        assertThat(results.get(0).get("image")).isEqualTo(annotationTerm.getUserAnnotation().getImage().getId());
        assertThat(results.get(0).get("countAnnotations")).isEqualTo(2L);

        AnnotationTerm annotationTermWithSameImage = builder.givenAnAnnotationTerm(builder.givenAUserAnnotation(
            project));
        annotationTermWithSameImage.getUserAnnotation().setImage(annotationTerm.getUserAnnotation().getImage());

        results = statsService.statPerTermAndImage(project, null, null);
        assertThat(results).hasSize(2);
        assertThat(results.get(0).get("term")).isEqualTo(annotationTerm.getTerm().getId());
        assertThat(results.get(0).get("image")).isEqualTo(annotationTerm.getUserAnnotation().getImage().getId());
        assertThat(results.get(0).get("countAnnotations")).isEqualTo(2L);
        assertThat(results.get(1).get("term")).isEqualTo(annotationTermWithSameImage.getTerm().getId());
        assertThat(results.get(1).get("image")).isEqualTo(annotationTerm.getUserAnnotation().getImage().getId());
        assertThat(results.get(1).get("countAnnotations")).isEqualTo(1L);


        results = statsService.statPerTermAndImage(
            project,
            DateUtils.addDays(new Date(), -40),
            DateUtils.addDays(new Date(), -20)
        );
        assertThat(results).hasSize(0);
    }


    @Test
    void stats_user_annotation() {
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

        List<JsonObject> results = statsService.statUserAnnotations(project);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(builder.givenSuperAdmin().getId());
        terms = (List<JsonObject>) results.get(0).get("terms");
        assertThat(terms).hasSize(1);
        assertThat(terms.get(0).getJSONAttrLong("value")).isEqualTo(2);
    }


    @Test
    void stats_user() {
        Project project = builder.givenAProject();
        builder.addUserToProject(project, "superadmin");
        UserAnnotation annotation1 = builder.givenAUserAnnotation(project);
        annotation1.setCreated(DateUtils.addDays(new Date(), -1));
        builder.persistAndReturn(annotation1);
        entityManager.refresh(annotation1);
        UserAnnotation annotation2 = builder.givenAUserAnnotation(project);
        annotation2.setCreated(DateUtils.addDays(new Date(), -1));
        builder.persistAndReturn(annotation2);

        List<JsonObject> terms;

        List<JsonObject> results = statsService.statUser(project, null, null);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(builder.givenSuperAdmin().getId());
        assertThat(results.get(0).getJSONAttrLong("value")).isEqualTo(2);
    }


    @Test
    void retrieve_storage_spaces() throws JsonProcessingException {
        configureFor("localhost", 8888);
        String body = objectMapper.writeValueAsString(Map.of(
            "used", 193396892,
            "available", 445132860,
            "usedP", 0.302878435,
            "hostname", "b52416f53249",
            "mount", "/data/images",
            "ip", null
        ));
        stubFor(get(urlEqualTo(IMS_API_BASE_PATH + "/storage/size.json")).willReturn(aResponse().withBody(body)));

        JsonObject response = statsService.statUsedStorage();
        assertThat(response).isNotNull();
        // expected to be Greather than or eq because localhost:8888 may not be the only one
        assertThat(response.getJSONAttrLong("total")).isGreaterThanOrEqualTo(193396892 + 445132860);
        assertThat(response.getJSONAttrLong("available")).isGreaterThanOrEqualTo(445132860);
        assertThat(response.getJSONAttrLong("used")).isGreaterThanOrEqualTo(193396892);
        assertThat(response.getJSONAttrDouble("usedP")).isGreaterThan(0);

    }

    @Test
    void stats_connection_evolution() {
        Project project = builder.givenAProject();
        given_a_persistent_connection_in_project(
            builder.givenSuperAdmin(),
            project,
            DateUtils.addDays(new Date(), -15)
        );
        given_a_persistent_connection_in_project(
            builder.givenSuperAdmin(),
            project,
            DateUtils.addDays(new Date(), -15)
        );
        given_a_persistent_connection_in_project(
            builder.givenSuperAdmin(),
            project,
            DateUtils.addDays(new Date(), -5)
        );


        List<JsonObject> jsonObjects = statsService.statConnectionsEvolution(
            project,
            7,
            DateUtils.addDays(new Date(), -18),
            null,
            false
        );
        assertThat(jsonObjects).hasSize(3);
        assertThat(jsonObjects.get(0).getJSONAttrLong("size")).isEqualTo(2);
        assertThat(jsonObjects.get(1).getJSONAttrLong("size")).isEqualTo(1);
        assertThat(jsonObjects.get(2).getJSONAttrLong("size")).isEqualTo(0);

        jsonObjects = statsService.statConnectionsEvolution(project, 7, DateUtils.addDays(new Date(), -18), null, true);
        assertThat(jsonObjects).hasSize(3);
        assertThat(jsonObjects.get(0).getJSONAttrLong("size")).isEqualTo(2);
        assertThat(jsonObjects.get(1).getJSONAttrLong("size")).isEqualTo(3);
        assertThat(jsonObjects.get(2).getJSONAttrLong("size")).isEqualTo(3);


        jsonObjects = statsService.statConnectionsEvolution(
            project,
            7,
            DateUtils.addDays(new Date(), -18),
            DateUtils.addDays(new Date(), -6),
            true
        );
        assertThat(jsonObjects).hasSize(2);

    }

    @Test
    void stats_image_consultation_evolution() {
        Project project = builder.givenAProject();
        ImageInstance imageInstance = builder.givenAnImageInstance(project);
        given_a_persistent_image_consultation(
            builder.givenSuperAdmin(),
            imageInstance,
            DateUtils.addDays(new Date(), -15)
        );
        given_a_persistent_image_consultation(
            builder.givenSuperAdmin(),
            imageInstance,
            DateUtils.addDays(new Date(), -15)
        );
        given_a_persistent_image_consultation(
            builder.givenSuperAdmin(),
            imageInstance,
            DateUtils.addDays(new Date(), -5)
        );


        List<JsonObject> jsonObjects = statsService.statImageConsultationsEvolution(
            project,
            7,
            DateUtils.addDays(new Date(), -18),
            null,
            false
        );
        assertThat(jsonObjects).hasSize(3);
        assertThat(jsonObjects.get(0).getJSONAttrLong("size")).isEqualTo(2);
        assertThat(jsonObjects.get(1).getJSONAttrLong("size")).isEqualTo(1);
        assertThat(jsonObjects.get(2).getJSONAttrLong("size")).isEqualTo(0);

        jsonObjects = statsService.statImageConsultationsEvolution(
            project,
            7,
            DateUtils.addDays(new Date(), -18),
            null,
            true
        );
        assertThat(jsonObjects).hasSize(3);
        assertThat(jsonObjects.get(0).getJSONAttrLong("size")).isEqualTo(2);
        assertThat(jsonObjects.get(1).getJSONAttrLong("size")).isEqualTo(3);
        assertThat(jsonObjects.get(2).getJSONAttrLong("size")).isEqualTo(3);
    }


    @Test
    void stats_annotation_Action_evolution() {
        Project project = builder.givenAProject();
        AnnotationDomain annotation = builder.givenAUserAnnotation(project);
        given_a_persistent_annotation_action(
            DateUtils.addDays(new Date(), -15),
            annotation,
            builder.givenSuperAdmin(),
            "select"
        );
        given_a_persistent_annotation_action(
            DateUtils.addDays(new Date(), -15),
            annotation,
            builder.givenSuperAdmin(),
            "move"
        );
        given_a_persistent_annotation_action(
            DateUtils.addDays(new Date(), -15),
            annotation,
            builder.givenSuperAdmin(),
            "select"
        );
        given_a_persistent_annotation_action(
            DateUtils.addDays(new Date(), -5),
            annotation,
            builder.givenSuperAdmin(),
            "select"
        );

        List<JsonObject> jsonObjects = statsService.statAnnotationActionsEvolution(
            project,
            7,
            DateUtils.addDays(new Date(), -18),
            null,
            false,
            "select"
        );
        assertThat(jsonObjects).hasSize(3);
        assertThat(jsonObjects.get(0).getJSONAttrLong("size")).isEqualTo(2);
        assertThat(jsonObjects.get(1).getJSONAttrLong("size")).isEqualTo(1);
        assertThat(jsonObjects.get(2).getJSONAttrLong("size")).isEqualTo(0);

        jsonObjects = statsService.statAnnotationActionsEvolution(
            project,
            7,
            DateUtils.addDays(new Date(), -18),
            null,
            true,
            "select"
        );
        assertThat(jsonObjects).hasSize(3);
        assertThat(jsonObjects.get(0).getJSONAttrLong("size")).isEqualTo(2);
        assertThat(jsonObjects.get(1).getJSONAttrLong("size")).isEqualTo(3);
        assertThat(jsonObjects.get(2).getJSONAttrLong("size")).isEqualTo(3);
    }


}
