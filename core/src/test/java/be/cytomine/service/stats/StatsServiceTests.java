package be.cytomine.service.stats;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.repository.http.StatsHttpContract;
import be.cytomine.common.repository.model.stat.payload.FlatStatUserTerm;
import be.cytomine.common.repository.model.stat.payload.StatPerTermAndImage;
import be.cytomine.common.repository.model.stat.payload.StatTerm;
import be.cytomine.common.repository.model.stat.payload.StatUserTerm;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = "ROLE_SUPER_ADMIN", username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
@Transactional
public class StatsServiceTests {

    private static WireMockServer wireMockServer = new WireMockServer(8888);
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
    @MockitoBean
    StatsHttpContract statsHttpContract;

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
    }

    PersistentProjectConnection givenAPersistentConnectionInProject(User user, Project project, Date created) {
        return projectConnectionService.add(user, project, "xxx", "linux", "chrome", "123", created);
    }

    PersistentImageConsultation givenAPersistentImageConsultation(User user, ImageInstance imageInstance,
                                                                  Date created) {
        return imageConsultationService.add(user, imageInstance.getId(), "xxx", "mode", created);
    }

    AnnotationAction givenAPersistentAnnotationAction(Date creation, AnnotationDomain annotationDomain, User user,
                                                      String action) {
        return annotationActionService.add(annotationDomain, user, action, creation);
    }

    @Test
    void statsDomainCount() {
        UserAnnotation annotation = builder.givenAUserAnnotation();
        assertThat(statsService.total(annotation.getClass())).isGreaterThanOrEqualTo(1);
        assertThat(statsService.total(annotation.getProject().getClass())).isGreaterThanOrEqualTo(1);
    }

    @Test
    void currentUserCount() {
        assertThat(statsService.numberOfCurrentUsers()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void activeProjectsCount() {
        assertThat(statsService.numberOfActiveProjects()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void mostActiveProjectCount() {
        Project project = builder.givenAProject();
        givenAPersistentConnectionInProject(builder.givenSuperAdmin(), project, new Date());
        assertThat(((JsonObject) statsService.mostActiveProjects().get().get("project")).getId()).isEqualTo(
            project.getId());
    }

    @Test
    void statsAnnotationTermByProject() {
        Project project = builder.givenAProject();
        builder.addUserToProject(project, "superadmin");
        AnnotationTerm annotationTerm = builder.givenAnAnnotationTerm(builder.givenAUserAnnotation(project));
        entityManager.refresh(annotationTerm.getUserAnnotation());

        List<JsonObject> jsonObjects = statsService.statAnnotationTermedByProject(annotationTerm.getTerm().getId(),
            annotationTerm.getTerm().getOntology().getId());
        assertThat(jsonObjects).hasSize(1);
        assertThat(jsonObjects.get(0).get("username")).isEqualTo(project.getName());
        assertThat(jsonObjects.get(0).get("value")).isEqualTo(1L);

    }


    @Test
    void statsUserAnnotationEvolution() {
        Project project = builder.givenAProject();
        UserAnnotation annotation1 = builder.givenAUserAnnotation(project);
        annotation1.setCreated(DateUtils.addDays(new Date(), -1));
        builder.persistAndReturn(annotation1);
        UserAnnotation annotation2 = builder.givenAUserAnnotation(project);
        annotation2.setCreated(DateUtils.addDays(new Date(), -10));
        builder.persistAndReturn(annotation2);

        List<JsonObject> jsonObjects =
            statsService.statAnnotationEvolution(project, null, 7, DateUtils.addDays(new Date(), -30),
                DateUtils.addDays(new Date(), 0), true, false);

        assertThat(jsonObjects).hasSize(5);
        assertThat(
            jsonObjects.stream().filter(x -> x.getJSONAttrLong("size") == 1).collect(Collectors.toList())).hasSize(2);

        statsService.statAnnotationEvolution(project, Optional.of(builder.givenATerm(project.getOntology()).getId()), 7,
            DateUtils.addDays(new Date(), -30), DateUtils.addDays(new Date(), 0), true, false);

    }

    @Test
    void statsReviewedAnnotationEvolution() throws ParseException {
        Project project = builder.givenAProject();
        ReviewedAnnotation annotation1 = builder.givenAReviewedAnnotation(project);
        annotation1.setCreated(DateUtils.addDays(new Date(), -1));
        builder.persistAndReturn(annotation1);
        ReviewedAnnotation annotation2 = builder.givenAReviewedAnnotation(project);
        annotation2.setCreated(DateUtils.addDays(new Date(), -10));
        builder.persistAndReturn(annotation2);

        List<JsonObject> jsonObjects =
            statsService.statReviewedAnnotationEvolution(project, null, 7, DateUtils.addDays(new Date(), -30),
                DateUtils.addDays(new Date(), 0), true, false);

        assertThat(jsonObjects).hasSize(5);
        assertThat(
            jsonObjects.stream().filter(x -> x.getJSONAttrLong("size") == 1).collect(Collectors.toList())).hasSize(2);

        statsService.statReviewedAnnotationEvolution(project,
            Optional.of(builder.givenATerm(project.getOntology()).getId()), 7,
            DateUtils.addDays(new Date(), -30), DateUtils.addDays(new Date(), 0), true, false);


    }

    @Test
    void statsUderSlide() {
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

        results =
            statsService.statUserSlide(project, DateUtils.addDays(new Date(), -40), DateUtils.addDays(new Date(), -20));

        assertThat(results).hasSize(2);
        assertThat(results.get(0).get("value")).isEqualTo(0);
        assertThat(results.get(1).get("value")).isEqualTo(0);
    }


    @Test
    void statsTermSlide() {
        Project project = builder.givenAProject();
        long ontologyId = project.getOntology().getId();
        long userId = builder.givenSuperAdmin().getId();

        when(statsHttpContract.findTermsByProject(ontologyId, userId, Optional.empty(), Optional.empty(), 0, 20))
            .thenReturn(new PageImpl<>(List.of()));
        List<StatTerm> results = statsService.statTermSlide(project, Optional.empty(), Optional.empty());
        results.removeIf(x -> x.id() == 0);
        assertThat(results).hasSize(0);

        Term term = builder.givenATerm(project.getOntology());

        when(statsHttpContract.findTermsByProject(ontologyId, userId, Optional.empty(), Optional.empty(), 0, 20))
            .thenReturn(new PageImpl<>(List.of(new StatTerm(term.getId(), term.getName(), term.getColor(), 0))));
        results = statsService.statTermSlide(project, Optional.empty(), Optional.empty());
        results.removeIf(x -> x.id() == 0);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).id()).isEqualTo(term.getId());
        assertThat(results.get(0).count()).isEqualTo(0);

        UserAnnotation annotation1 = builder.givenAUserAnnotation(project);
        annotation1.setCreated(DateUtils.addDays(new Date(), -1));
        builder.givenAnAnnotationTerm(annotation1, term);
        builder.persistAndReturn(annotation1);

        when(statsHttpContract.findTermsByProject(ontologyId, userId, Optional.empty(), Optional.empty(), 0, 20))
            .thenReturn(new PageImpl<>(List.of(new StatTerm(term.getId(), term.getName(), term.getColor(), 1))));
        results = statsService.statTermSlide(project, Optional.empty(), Optional.empty());
        results.removeIf(x -> x.id() == 0);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).id()).isEqualTo(term.getId());
        assertThat(results.get(0).count()).isEqualTo(1L);

        Term term2 = builder.givenATerm(project.getOntology());

        when(statsHttpContract.findTermsByProject(ontologyId, userId, Optional.empty(), Optional.empty(), 0, 20))
            .thenReturn(new PageImpl<>(List.of(
                new StatTerm(term.getId(), term.getName(), term.getColor(), 1),
                new StatTerm(term2.getId(), term2.getName(), term2.getColor(), 0))));
        results = statsService.statTermSlide(project, Optional.empty(), Optional.empty());
        results.removeIf(x -> x.id() == 0);
        assertThat(results).hasSize(2);

        Optional<LocalDateTime> startDate = Optional.of(LocalDateTime.now().minusDays(42));
        Optional<LocalDateTime> endDate = Optional.of(LocalDateTime.now().minusDays(20));
        when(statsHttpContract.findTermsByProject(ontologyId, userId, startDate, endDate, 0, 20))
            .thenReturn(new PageImpl<>(List.of(
                new StatTerm(term.getId(), term.getName(), term.getColor(), 0),
                new StatTerm(term2.getId(), term2.getName(), term2.getColor(), 0))));
        results = statsService.statTermSlide(project, startDate, endDate);
        results.removeIf(x -> x.id() == 0);
        assertThat(results).hasSize(2);
        assertThat(results.get(0).count()).isEqualTo(0);
        assertThat(results.get(1).count()).isEqualTo(0);
    }


    @Test
    void statsTerm() {
        Project project = builder.givenAProject();
        long userId = builder.givenSuperAdmin().getId();

        when(statsHttpContract.findTermsByProject(eq(project.getId()), eq(userId), eq(Optional.empty()),
            eq(Optional.empty()), eq(0), eq(20)))
            .thenReturn(new PageImpl<>(List.of()));
        List<JsonObject> results = statsService.statTerm(project, null, null, false);

        assertThat(results).hasSize(0); //no term

        AnnotationTerm annotationTerm = builder.givenAnAnnotationTerm(builder.givenAUserAnnotation(project));
        entityManager.refresh(project.getOntology());
        long termId = annotationTerm.getTerm().getId();
        Term term = annotationTerm.getTerm();

        when(statsHttpContract.findTermsByProject(eq(project.getId()), eq(userId), eq(Optional.empty()),
            eq(Optional.empty()), eq(0), eq(20)))
            .thenReturn(new PageImpl<>(List.of(new StatTerm(termId, term.getName(), term.getColor(), 1))));
        results = statsService.statTerm(project, null, null, false);
        results.removeIf(x -> x.get("id") == null);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(annotationTerm.getTerm().getId());
        assertThat(results.get(0).get("value")).isEqualTo(1L);

        UserAnnotation annotation1 = builder.givenAUserAnnotation(project);
        annotation1.setCreated(DateUtils.addDays(new Date(), -1));
        builder.givenAnAnnotationTerm(annotation1, annotationTerm.getTerm());
        builder.persistAndReturn(annotation1);

        when(statsHttpContract.findTermsByProject(eq(project.getId()), eq(userId), eq(Optional.empty()),
            eq(Optional.empty()), eq(0), eq(20)))
            .thenReturn(new PageImpl<>(List.of(new StatTerm(termId, term.getName(), term.getColor(), 2))));
        results = statsService.statTerm(project, null, null, false);
        results.removeIf(x -> x.get("id") == null);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(annotationTerm.getTerm().getId());
        assertThat(results.get(0).get("value")).isEqualTo(2L);

        when(statsHttpContract.findTermsByProject(eq(project.getId()), eq(userId), any(), any(), eq(0), eq(20)))
            .thenReturn(new PageImpl<>(List.of(new StatTerm(termId, term.getName(), term.getColor(), 0))));
        results = statsService.statTerm(project, DateUtils.addDays(new Date(), -40), DateUtils.addDays(new Date(), -20),
            false);
        results.removeIf(x -> x.get("id") == null);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).get("value")).isEqualTo(0L);
    }

    @Test
    void statPerTermAndImage() {
        Project project = builder.givenAProject();

        when(statsHttpContract.findPerTermAndImageByProject(eq(project.getId()), eq(Optional.empty()),
            eq(Optional.empty()), eq(0), eq(20)))
            .thenReturn(new PageImpl<>(List.of()));
        List<StatPerTermAndImage> results = statsService.statPerTermAndImage(project, null, null);

        assertThat(results).hasSize(0); //no annotations

        AnnotationTerm annotationTerm = builder.givenAnAnnotationTerm(builder.givenAUserAnnotation(project));
        entityManager.refresh(project.getOntology());
        long termId = annotationTerm.getTerm().getId();
        long imageId = annotationTerm.getUserAnnotation().getImage().getId();

        when(statsHttpContract.findPerTermAndImageByProject(eq(project.getId()), eq(Optional.empty()),
            eq(Optional.empty()), eq(0), eq(20)))
            .thenReturn(new PageImpl<>(List.of(new StatPerTermAndImage(imageId, termId, 1L))));
        results = statsService.statPerTermAndImage(project, null, null);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).termId()).isEqualTo(annotationTerm.getTerm().getId());
        assertThat(results.get(0).imageId()).isEqualTo(annotationTerm.getUserAnnotation().getImage().getId());
        assertThat(results.get(0).countAnnotations()).isEqualTo(1L);

        AnnotationTerm annotationTermWithSameImageAndSameTerm =
            builder.givenAnAnnotationTerm(builder.givenAUserAnnotation(project));
        annotationTermWithSameImageAndSameTerm.getUserAnnotation()
            .setImage(annotationTerm.getUserAnnotation().getImage());
        annotationTermWithSameImageAndSameTerm.setTerm(annotationTerm.getTerm());

        when(statsHttpContract.findPerTermAndImageByProject(eq(project.getId()), eq(Optional.empty()),
            eq(Optional.empty()), eq(0), eq(20)))
            .thenReturn(new PageImpl<>(List.of(new StatPerTermAndImage(imageId, termId, 2L))));
        results = statsService.statPerTermAndImage(project, null, null);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).termId()).isEqualTo(annotationTerm.getTerm().getId());
        assertThat(results.get(0).imageId()).isEqualTo(annotationTerm.getUserAnnotation().getImage().getId());
        assertThat(results.get(0).countAnnotations()).isEqualTo(2L);

        when(statsHttpContract.findPerTermAndImageByProject(eq(project.getId()), any(), any(), eq(0), eq(20)))
            .thenReturn(new PageImpl<>(List.of()));
        results = statsService.statPerTermAndImage(project, DateUtils.addDays(new Date(), -40),
            DateUtils.addDays(new Date(), -20));
        assertThat(results).hasSize(0);
    }


    @Test
    void statsUserAnnotation() {
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

        Term term = annotation1.getTerms().get(0);
        User superAdmin = builder.givenSuperAdmin();
        when(statsHttpContract.findUserTermsByProject(project.getId(), superAdmin.getId(), 0, 20))
            .thenReturn(new PageImpl<>(List.of(
                new FlatStatUserTerm(superAdmin.getId(), superAdmin.getUsername(),
                    new StatTerm(term.getId(), term.getName(), term.getColor(), 2))
            )));

        Set<StatTerm> terms;

        List<StatUserTerm> results = statsService.statUserAnnotations(project);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).userId()).isEqualTo(builder.givenSuperAdmin().getId());
        terms = results.get(0).terms();
        assertThat(terms).hasSize(1);
        assertThat(terms.stream().findFirst().get().count()).isEqualTo(2);
    }


    @Test
    void statsUser() {
        Project project = builder.givenAProject();
        builder.addUserToProject(project, "superadmin");
        UserAnnotation annotation1 = builder.givenAUserAnnotation(project);
        annotation1.setCreated(DateUtils.addDays(new Date(), -1));
        builder.persistAndReturn(annotation1);
        entityManager.refresh(annotation1);
        UserAnnotation annotation2 = builder.givenAUserAnnotation(project);
        annotation2.setCreated(DateUtils.addDays(new Date(), -1));
        builder.persistAndReturn(annotation2);

        List<JsonObject> results = statsService.statUser(project, null, null);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(builder.givenSuperAdmin().getId());
        assertThat(results.get(0).getJSONAttrLong("value")).isEqualTo(2);
    }

    @Test
    void retrieveStorageSpaces() {
        configureFor("localhost", 8888);
        String body = """
            {
              "used": 193396892,
              "available": 445132860,
              "usedP": 0.302878435,
              "hostname": "b52416f53249",
              "mount": "/data/images",
              "ip": null
            }
            """;
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
    void statsConnectionEvolution() {
        Project project = builder.givenAProject();
        givenAPersistentConnectionInProject(builder.givenSuperAdmin(), project, DateUtils.addDays(new Date(), -15));
        givenAPersistentConnectionInProject(builder.givenSuperAdmin(), project, DateUtils.addDays(new Date(), -15));
        givenAPersistentConnectionInProject(builder.givenSuperAdmin(), project, DateUtils.addDays(new Date(), -5));


        List<JsonObject> jsonObjects =
            statsService.statConnectionsEvolution(project, 7, DateUtils.addDays(new Date(), -18), null, false);
        assertThat(jsonObjects).hasSize(3);
        assertThat(jsonObjects.get(0).getJSONAttrLong("size")).isEqualTo(2);
        assertThat(jsonObjects.get(1).getJSONAttrLong("size")).isEqualTo(1);
        assertThat(jsonObjects.get(2).getJSONAttrLong("size")).isEqualTo(0);

        jsonObjects = statsService.statConnectionsEvolution(project, 7, DateUtils.addDays(new Date(), -18), null, true);
        assertThat(jsonObjects).hasSize(3);
        assertThat(jsonObjects.get(0).getJSONAttrLong("size")).isEqualTo(2);
        assertThat(jsonObjects.get(1).getJSONAttrLong("size")).isEqualTo(3);
        assertThat(jsonObjects.get(2).getJSONAttrLong("size")).isEqualTo(3);


        jsonObjects = statsService.statConnectionsEvolution(project, 7, DateUtils.addDays(new Date(), -18),
            DateUtils.addDays(new Date(), -6), true);
        assertThat(jsonObjects).hasSize(2);

    }

    @Test
    void statsImageConsultationEvolution() {
        Project project = builder.givenAProject();
        ImageInstance imageInstance = builder.givenAnImageInstance(project);
        givenAPersistentImageConsultation(builder.givenSuperAdmin(), imageInstance, DateUtils.addDays(new Date(), -15));
        givenAPersistentImageConsultation(builder.givenSuperAdmin(), imageInstance, DateUtils.addDays(new Date(), -15));
        givenAPersistentImageConsultation(builder.givenSuperAdmin(), imageInstance, DateUtils.addDays(new Date(), -5));


        List<JsonObject> jsonObjects =
            statsService.statImageConsultationsEvolution(project, 7, DateUtils.addDays(new Date(), -18), null, false);
        assertThat(jsonObjects).hasSize(3);
        assertThat(jsonObjects.get(0).getJSONAttrLong("size")).isEqualTo(2);
        assertThat(jsonObjects.get(1).getJSONAttrLong("size")).isEqualTo(1);
        assertThat(jsonObjects.get(2).getJSONAttrLong("size")).isEqualTo(0);

        jsonObjects =
            statsService.statImageConsultationsEvolution(project, 7, DateUtils.addDays(new Date(), -18), null, true);
        assertThat(jsonObjects).hasSize(3);
        assertThat(jsonObjects.get(0).getJSONAttrLong("size")).isEqualTo(2);
        assertThat(jsonObjects.get(1).getJSONAttrLong("size")).isEqualTo(3);
        assertThat(jsonObjects.get(2).getJSONAttrLong("size")).isEqualTo(3);
    }

    @Test
    void shouldReturnAnnotationActionEvolutionStatsOverTime() {
        Project project = builder.givenAProject();
        AnnotationDomain annotation = builder.givenAUserAnnotation(project);
        givenAPersistentAnnotationAction(DateUtils.addDays(new Date(), -15), annotation, builder.givenSuperAdmin(),
            "select");
        givenAPersistentAnnotationAction(DateUtils.addDays(new Date(), -15), annotation, builder.givenSuperAdmin(),
            "move");
        givenAPersistentAnnotationAction(DateUtils.addDays(new Date(), -15), annotation, builder.givenSuperAdmin(),
            "select");
        givenAPersistentAnnotationAction(DateUtils.addDays(new Date(), -5), annotation, builder.givenSuperAdmin(),
            "select");

        List<JsonObject> jsonObjects =
            statsService.statAnnotationActionsEvolution(project, 7, DateUtils.addDays(new Date(), -18), null, false,
                "select");
        assertThat(jsonObjects).hasSize(3);
        assertThat(jsonObjects.get(0).getJSONAttrLong("size")).isEqualTo(2);
        assertThat(jsonObjects.get(1).getJSONAttrLong("size")).isEqualTo(1);
        assertThat(jsonObjects.get(2).getJSONAttrLong("size")).isEqualTo(0);

        jsonObjects =
            statsService.statAnnotationActionsEvolution(project, 7, DateUtils.addDays(new Date(), -18), null, true,
                "select");
        assertThat(jsonObjects).hasSize(3);
        assertThat(jsonObjects.get(0).getJSONAttrLong("size")).isEqualTo(2);
        assertThat(jsonObjects.get(1).getJSONAttrLong("size")).isEqualTo(3);
        assertThat(jsonObjects.get(2).getJSONAttrLong("size")).isEqualTo(3);
    }
}
