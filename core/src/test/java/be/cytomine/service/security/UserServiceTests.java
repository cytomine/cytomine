package be.cytomine.service.security;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.context.request.RequestContextHolder;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.domain.image.ImageInstance;
import be.cytomine.domain.image.SliceInstance;
import be.cytomine.domain.image.server.Storage;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.User;
import be.cytomine.domain.social.LastConnection;
import be.cytomine.domain.social.PersistentConnection;
import be.cytomine.domain.social.PersistentImageConsultation;
import be.cytomine.domain.social.PersistentProjectConnection;
import be.cytomine.domain.social.PersistentUserPosition;
import be.cytomine.dto.auth.AuthInformation;
import be.cytomine.dto.image.AreaDTO;
import be.cytomine.repositorynosql.social.LastConnectionRepository;
import be.cytomine.repositorynosql.social.LastUserPositionRepository;
import be.cytomine.repositorynosql.social.PersistentConnectionRepository;
import be.cytomine.repositorynosql.social.PersistentImageConsultationRepository;
import be.cytomine.repositorynosql.social.PersistentProjectConnectionRepository;
import be.cytomine.repositorynosql.social.PersistentUserPositionRepository;
import be.cytomine.repositorynosql.social.ProjectConnectionRepository;
import be.cytomine.service.PermissionService;
import be.cytomine.service.database.SequenceService;
import be.cytomine.service.project.ProjectMemberService;
import be.cytomine.service.search.UserSearchExtension;
import be.cytomine.service.social.ImageConsultationService;
import be.cytomine.service.social.ProjectConnectionService;
import be.cytomine.service.social.UserPositionService;
import be.cytomine.service.social.UserPositionServiceTests;
import be.cytomine.utils.JsonObject;
import be.cytomine.utils.filters.SearchOperation;
import be.cytomine.utils.filters.SearchParameterEntry;

import static be.cytomine.service.search.RetrievalService.CBIR_API_BASE_PATH;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION;
import static org.springframework.security.acls.domain.BasePermission.READ;
import static org.springframework.security.acls.domain.BasePermission.WRITE;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
@Transactional
public class UserServiceTests {

    @Autowired
    UserService userService;

    @Autowired
    private ProjectMemberService projectMemberService;

    @Autowired
    private BasicInstanceBuilder builder;

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
    private SequenceService sequenceService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private UserPositionService userPositionService;

    private static WireMockServer wireMockServer = new WireMockServer(8888);

    private static void setupStub() {
        /* Simulate call to CBIR */
        wireMockServer.stubFor(delete(urlPathMatching(CBIR_API_BASE_PATH + "/images/.*"))
            .withQueryParam("storage", WireMock.matching(".*"))
            .withQueryParam("index", WireMock.equalTo("annotation"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", "application/json")
                .withBody(UUID.randomUUID().toString())
            )
        );
    }

    @BeforeAll
    public static void beforeAll() {
        wireMockServer.start();
        WireMock.configureFor("localhost", 8888);

        setupStub();
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
        return projectConnectionService.add(
            user,
            project,
            "xxx",
            "linux",
            "chrome",
            "123",
            created
        );
    }

    PersistentImageConsultation givenAPersistentImageConsultation(
        User user,
        ImageInstance imageInstance,
        Date created
    ) {
        return imageConsultationService.add(user, imageInstance.getId(), "xxx", "mode", created);
    }

    PersistentConnection givenALastConnection(User user, Long idProject, Date date) {
        LastConnection connection = new LastConnection();
        connection.setId(sequenceService.generateID());
        connection.setUser(user.getId());
        connection.setDate(date);
        connection.setCreated(date);
        connection.setProject(idProject);
        lastConnectionRepository.insert(connection); //don't use save (stateless collection)

        PersistentConnection connectionPersist = new PersistentConnection();
        connectionPersist.setId(sequenceService.generateID());
        connectionPersist.setUser(user.getId());
        connectionPersist.setCreated(date);
        connectionPersist.setProject(idProject);
        connectionPersist.setSession(RequestContextHolder.currentRequestAttributes().getSessionId());
        persistentConnectionRepository.insert(connectionPersist); //don't use save (stateless collection)
        return connectionPersist;
    }

    @Test
    void findUnexistingUserReturnEmpty() {
        assertThat(userService.find(0L)).isEmpty();
    }

    @Test
    void findUserWithSuccess() {
        User user = builder.givenAUser();
        assertThat(userService.findUser(user.getId())).isPresent().contains(user);
    }

    @Test
    void findUserByUsername() {
        User user = builder.givenAUser();
        assertThat(userService.findByUsername(user.getUsername())).isPresent().contains(user);
        assertThat(userService.findByUsername(user.getUsername().toUpperCase(Locale.ROOT))).isPresent().contains(user);
        assertThat(userService.findByUsername(user.getUsername().toLowerCase(Locale.ROOT))).isPresent().contains(user);
    }

    @Test
    void findUserByPublicKey() {
        User user = builder.givenAUser();
        assertThat(userService.findByPublicKey(user.getPublicKey())).isPresent().contains(user);
    }

    @Test
    void getAuthRolesForUser() {
        User user = builder.givenAUser();
        AuthInformation authInformation = userService.getAuthenticationRoles(user);
        assertThat(authInformation.getAdmin()).isFalse();
        assertThat(authInformation.getUser()).isTrue();
        assertThat(authInformation.getGuest()).isFalse();

        assertThat(authInformation.getAdminByNow()).isFalse();
        assertThat(authInformation.getUserByNow()).isTrue();
        assertThat(authInformation.getGuestByNow()).isFalse();
    }


    @Test
    void getAuthRolesForGuest() {
        User user = builder.givenAGuest();
        AuthInformation authInformation = userService.getAuthenticationRoles(user);
        assertThat(authInformation.getAdmin()).isFalse();
        assertThat(authInformation.getUser()).isFalse();
        assertThat(authInformation.getGuest()).isTrue();

        assertThat(authInformation.getAdminByNow()).isFalse();
        assertThat(authInformation.getUserByNow()).isFalse();
        assertThat(authInformation.getGuestByNow()).isTrue();
    }

    @Test
    void getAuthRolesForSuperamdin() {
        User user = builder.givenSuperAdmin();
        AuthInformation authInformation = userService.getAuthenticationRoles(user);
        assertThat(authInformation.getAdmin()).isTrue();
        assertThat(authInformation.getUser()).isFalse();
        assertThat(authInformation.getGuest()).isFalse();

        assertThat(authInformation.getAdminByNow()).isTrue();
        assertThat(authInformation.getUserByNow()).isFalse();
        assertThat(authInformation.getGuestByNow()).isFalse();
    }

    @Test
    void getAuthRolesForAdmin() {
        User user = builder.givenAnAdmin();
        AuthInformation authInformation = userService.getAuthenticationRoles(user);
        assertThat(authInformation.getAdmin()).isTrue();
        assertThat(authInformation.getUser()).isFalse();
        assertThat(authInformation.getGuest()).isFalse();

        assertThat(authInformation.getAdminByNow()).isFalse();
        assertThat(authInformation.getUserByNow()).isFalse();
        assertThat(authInformation.getGuestByNow()).isFalse();
    }

    @Test
    void listUsersWithNoFiltersNoExtension() {
        Page<Map<String, Object>> list = userService.list(new ArrayList<>(), "created", "desc", 0L, 0L);

        assertThat(list.getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(list.getContent().stream()
            .map(x -> x.get("id"))).contains(builder.givenSuperAdmin().getId());
    }

    @Test
    void listUsersWithWithMultisearchFilters() {
        Page<Map<String, Object>> list = userService.list(
            new ArrayList<>(List.of(new SearchParameterEntry("fullName", SearchOperation.like, "superad"))),
            "created",
            "desc",
            0L,
            0L
        );

        assertThat(list.getContent().stream()
            .map(x -> x.get("id"))).contains(builder.givenSuperAdmin().getId());

        list = userService.list(
            new ArrayList<>(List.of(new SearchParameterEntry(
                "fullName",
                SearchOperation.like,
                builder.givenSuperAdmin().getName()
            ))), "created", "desc", 0L, 0L
        );

        assertThat(list.getContent().stream()
            .map(x -> x.get("id"))).contains(builder.givenSuperAdmin().getId());

        list = userService.list(
            new ArrayList<>(List.of(new SearchParameterEntry("fullName", SearchOperation.like, "johndoe@example.com"))),
            "created",
            "desc",
            0L,
            0L
        );

        assertThat(list.getContent().stream()
            .map(x -> x.get("id"))).doesNotContain(builder.givenSuperAdmin().getId());
    }

    @Test
    void listUsersWithSortUsername() {
        User user1 = builder.givenAUser("list_users_with_sort_username1");
        User user2 = builder.givenAUser("list_users_with_sort_username2");

        Page<Map<String, Object>> list = userService.list(
            new ArrayList<>(List.of(new SearchParameterEntry(
                "fullName",
                SearchOperation.like,
                "list_users_with_sort_username"
            ))), "username", "asc", 0L, 0L
        );
        assertThat(list.getContent()).hasSize(2);
        assertThat(list.getContent().get(0).get("username")).isEqualTo(user1.getUsername());
        assertThat(list.getContent().get(1).get("username")).isEqualTo(user2.getUsername());

        list = userService.list(
            new ArrayList<>(List.of(new SearchParameterEntry(
                "fullName",
                SearchOperation.like,
                "list_users_with_sort_username"
            ))), "username", "desc", 0L, 0L
        );
        assertThat(list.getContent()).hasSize(2);
        assertThat(list.getContent().get(0).get("username")).isEqualTo(user2.getUsername());
        assertThat(list.getContent().get(1).get("username")).isEqualTo(user1.getUsername());
    }

    @Test
    void listUsersWithPage() {
        User user1 = builder.givenAUser("list_users_with_page1");
        User user2 = builder.givenAUser("list_users_with_page2");
        User user3 = builder.givenAUser("list_users_with_page3");
        User user4 = builder.givenAUser("list_users_with_page4");
        User user5 = builder.givenAUser("list_users_with_page5");

        Page<Map<String, Object>> list = userService.list(
            new ArrayList<>(List.of(new SearchParameterEntry(
                "fullName",
                SearchOperation.like,
                "list_users_with_page"
            ))), "username", "asc", 0L, 0L
        );
        assertThat(list.getContent()).hasSize(5);
        assertThat(list.getTotalElements()).isEqualTo(5);
        assertThat(list.getContent().get(0).get("username")).isEqualTo(user1.getUsername());
        assertThat(list.getContent().get(1).get("username")).isEqualTo(user2.getUsername());
        assertThat(list.getContent().get(2).get("username")).isEqualTo(user3.getUsername());
        assertThat(list.getContent().get(3).get("username")).isEqualTo(user4.getUsername());
        assertThat(list.getContent().get(4).get("username")).isEqualTo(user5.getUsername());

        list = userService.list(
            new ArrayList<>(List.of(new SearchParameterEntry(
                "fullName",
                SearchOperation.like,
                "list_users_with_page"
            ))), "username", "asc", 3L, 0L
        );
        assertThat(list.getContent()).hasSize(3);
        assertThat(list.getTotalElements()).isEqualTo(5);
        assertThat(list.getContent().get(0).get("username")).isEqualTo(user1.getUsername());
        assertThat(list.getContent().get(1).get("username")).isEqualTo(user2.getUsername());
        assertThat(list.getContent().get(2).get("username")).isEqualTo(user3.getUsername());

        list = userService.list(
            new ArrayList<>(List.of(new SearchParameterEntry(
                "fullName",
                SearchOperation.like,
                "list_users_with_page"
            ))), "username", "asc", 4L, 2L
        );
        assertThat(list.getContent()).hasSize(3);
        assertThat(list.getTotalElements()).isEqualTo(5);
        assertThat(list.getContent().get(0).get("username")).isEqualTo(user3.getUsername());
        assertThat(list.getContent().get(1).get("username")).isEqualTo(user4.getUsername());
        assertThat(list.getContent().get(2).get("username")).isEqualTo(user5.getUsername());

        list = userService.list(
            new ArrayList<>(List.of(new SearchParameterEntry(
                "fullName",
                SearchOperation.like,
                "list_users_with_page"
            ))), "username", "asc", 4L, 4L
        );
        assertThat(list.getContent()).hasSize(1);
        assertThat(list.getTotalElements()).isEqualTo(5);
        assertThat(list.getContent().get(0).get("username")).isEqualTo(user5.getUsername());

        list = userService.list(
            new ArrayList<>(List.of(new SearchParameterEntry(
                "fullName",
                SearchOperation.like,
                "list_users_with_page"
            ))), "username", "asc", 5L, 6L
        );
        assertThat(list.getContent()).hasSize(0);
        assertThat(list.getTotalElements()).isEqualTo(5);
    }

    @Test
    void listUserByProjectWithSuccess() {
        User user = builder.givenSuperAdmin();

        Project projectWhereUserIsManager = builder.givenAProject();
        Project projectWhereUserIsContributor = builder.givenAProject();
        Project projectWhereUserIsMissing = builder.givenAProject();
        Project projectWithTwoUsers = builder.givenAProject();

        builder.addUserToProject(projectWhereUserIsManager, "superadmin", ADMINISTRATION);
        builder.addUserToProject(projectWhereUserIsContributor, "superadmin", WRITE);
        builder.addUserToProject(projectWithTwoUsers, "superadmin", WRITE);

        User anotherUser = builder.givenAUser();
        builder.addUserToProject(projectWhereUserIsMissing, anotherUser.getUsername(), WRITE);
        builder.addUserToProject(projectWithTwoUsers, anotherUser.getUsername(), WRITE);

        Page<JsonObject> page = userService.listUsersByProject(
            projectWhereUserIsManager,
            new ArrayList<>(),
            "id",
            "desc",
            0L,
            0L
        );
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()
            .stream()
            .map(x -> x.get("id"))
            .collect(Collectors.toList())).contains(user.getId());
        assertThat(page.getContent().get(0).get("role")).isEqualTo("manager");
        assertThat(page.getContent().stream().map(x -> x.get("id")).collect(Collectors.toList())).doesNotContain(
            anotherUser.getId());

        page = userService.listUsersByProject(projectWhereUserIsContributor, new ArrayList<>(), "id", "desc", 0L, 0L);
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()
            .stream()
            .map(x -> x.get("id"))
            .collect(Collectors.toList())).contains(user.getId());
        assertThat(page.getContent().get(0).get("role")).isEqualTo("contributor");
        assertThat(page.getContent().stream().map(x -> x.get("id")).collect(Collectors.toList())).doesNotContain(
            anotherUser.getId());

        page = userService.listUsersByProject(projectWhereUserIsMissing, new ArrayList<>(), "id", "desc", 0L, 0L);
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()
            .stream()
            .map(x -> x.get("id"))
            .collect(Collectors.toList())).contains(anotherUser.getId());
        assertThat(page.getContent().get(0).get("role")).isEqualTo("contributor");
        assertThat(page.getContent()
            .stream()
            .map(x -> x.get("id"))
            .collect(Collectors.toList())).doesNotContain(user.getId());

        page = userService.listUsersByProject(projectWithTwoUsers, new ArrayList<>(), "id", "desc", 0L, 0L);
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()
            .stream()
            .map(x -> x.get("id"))
            .collect(Collectors.toList())).contains(anotherUser.getId());
        assertThat(page.getContent()
            .stream()
            .map(x -> x.get("id"))
            .collect(Collectors.toList())).contains(user.getId());
    }

    @Test
    void listUserExtendedWithEmptyExtension() {
        User user = builder.givenSuperAdmin();

        Project projectWhereUserIsManager = builder.givenAProject();
        Project projectWhereUserIsContributor = builder.givenAProject();
        Project projectWhereUserIsMissing = builder.givenAProject();
        Project projectWithTwoUsers = builder.givenAProject();

        builder.addUserToProject(projectWhereUserIsManager, "superadmin", ADMINISTRATION);
        builder.addUserToProject(projectWhereUserIsContributor, "superadmin", WRITE);
        builder.addUserToProject(projectWithTwoUsers, "superadmin", WRITE);

        User anotherUser = builder.givenAUser();
        builder.addUserToProject(projectWhereUserIsMissing, anotherUser.getUsername(), WRITE);
        builder.addUserToProject(projectWithTwoUsers, anotherUser.getUsername(), WRITE);

        List<SearchParameterEntry> searchParameterEntries = new ArrayList<>();

        Page<JsonObject> page = userService.listUsersExtendedByProject(
            projectWhereUserIsManager,
            new UserSearchExtension(),
            new ArrayList<>(),
            "id",
            "desc",
            0L,
            0L
        );
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()
            .stream()
            .map(x -> x.get("id"))
            .collect(Collectors.toList())).contains(user.getId());
        assertThat(page.getContent().get(0).get("role")).isEqualTo("manager");
        assertThat(page.getContent().stream().map(x -> x.get("id")).collect(Collectors.toList())).doesNotContain(
            anotherUser.getId());

        page = userService.listUsersExtendedByProject(
            projectWhereUserIsContributor,
            new UserSearchExtension(),
            new ArrayList<>(),
            "id",
            "desc",
            0L,
            0L
        );
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()
            .stream()
            .map(x -> x.get("id"))
            .collect(Collectors.toList())).contains(user.getId());
        assertThat(page.getContent().get(0).get("role")).isEqualTo("contributor");
        assertThat(page.getContent().stream().map(x -> x.get("id")).collect(Collectors.toList())).doesNotContain(
            anotherUser.getId());

        page = userService.listUsersExtendedByProject(
            projectWhereUserIsMissing,
            new UserSearchExtension(),
            new ArrayList<>(),
            "id",
            "desc",
            0L,
            0L
        );
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()
            .stream()
            .map(x -> x.get("id"))
            .collect(Collectors.toList())).contains(anotherUser.getId());
        assertThat(page.getContent().get(0).get("role")).isEqualTo("contributor");
        assertThat(page.getContent()
            .stream()
            .map(x -> x.get("id"))
            .collect(Collectors.toList())).doesNotContain(user.getId());

        page = userService.listUsersExtendedByProject(
            projectWithTwoUsers,
            new UserSearchExtension(),
            new ArrayList<>(),
            "id",
            "desc",
            0L,
            0L
        );
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()
            .stream()
            .map(x -> x.get("id"))
            .collect(Collectors.toList())).contains(anotherUser.getId());
        assertThat(page.getContent()
            .stream()
            .map(x -> x.get("id"))
            .collect(Collectors.toList())).contains(user.getId());
    }


    @Test
    void listUserExtendedWithLastImageName() {
        User userWhoHasOpenImage = builder.givenAUser();
        User userWhoHasOpenImageAfter = builder.givenAUser();
        User userNeverOpenImage = builder.givenAUser();

        Project project = builder.givenAProject();

        builder.addUserToProject(project, userWhoHasOpenImage.getUsername(), READ);
        builder.addUserToProject(project, userWhoHasOpenImageAfter.getUsername(), READ);
        builder.addUserToProject(project, userNeverOpenImage.getUsername(), WRITE);

        ImageInstance imageInstance = builder.givenAnImageInstance(project);
        imageInstance.setInstanceFilename(UUID.randomUUID().toString());

        givenAPersistentImageConsultation(userNeverOpenImage, imageInstance, DateUtils.addDays(new Date(), -2));
        givenAPersistentImageConsultation(
            userWhoHasOpenImageAfter,
            imageInstance,
            DateUtils.addDays(new Date(), -1)
        );

        UserSearchExtension userSearchExtension = new UserSearchExtension();
        userSearchExtension.setWithLastImage(true);
        Page<JsonObject> page = userService.listUsersExtendedByProject(
            project,
            userSearchExtension,
            new ArrayList<>(),
            "lastImageName",
            "desc",
            0L,
            0L
        );
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent().stream().map(x -> x.getJSONAttrLong("id"))).contains(
            userWhoHasOpenImage.getId(),
            userWhoHasOpenImageAfter.getId()
        );
        assertThat(page.getContent().stream().map(x -> x.getJSONAttrLong("lastImage"))).contains(imageInstance.getId());
    }


    @Test
    void listUserExtendedWithLastConnection() {
        User userWhoHasOpenProject = builder.givenAUser();
        User userWhoHasOpenProjectAfter = builder.givenAUser();
        User userNeverOpenProject = builder.givenAUser();

        Project project = builder.givenAProject();

        builder.addUserToProject(project, userWhoHasOpenProject.getUsername(), READ);
        builder.addUserToProject(project, userWhoHasOpenProjectAfter.getUsername(), READ);
        builder.addUserToProject(project, userNeverOpenProject.getUsername(), WRITE);

        PersistentProjectConnection userWhoHasOpenProjectConnection = givenAPersistentConnectionInProject(
            userWhoHasOpenProject,
            project,
            DateUtils.addDays(new Date(), -2)
        );
        PersistentProjectConnection userWhoHasOpenProjectAfterConnection = givenAPersistentConnectionInProject(
            userWhoHasOpenProjectAfter,
            project,
            DateUtils.addDays(new Date(), -1)
        );

        UserSearchExtension userSearchExtension = new UserSearchExtension();
        userSearchExtension.setWithLastConnection(true);
        Page<JsonObject> page = userService.listUsersExtendedByProject(
            project, userSearchExtension, new ArrayList<>(), "lastConnection", "desc", 0L, 0L);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent().get(0).getJSONAttrLong("id")).isEqualTo(userWhoHasOpenProjectAfter.getId());
        assertThat(page.getContent().get(0).getJSONAttrDate("lastConnection")).isEqualTo(
            userWhoHasOpenProjectAfterConnection.getCreated());
        assertThat(page.getContent().get(1).getJSONAttrLong("id")).isEqualTo(userWhoHasOpenProject.getId());
        assertThat(page.getContent()
            .get(1)
            .getJSONAttrDate("lastConnection")).isEqualTo(userWhoHasOpenProjectConnection.getCreated());
        assertThat(page.getContent().get(2).getJSONAttrLong("id")).isEqualTo(userNeverOpenProject.getId());
        assertThat(page.getContent().get(2).getJSONAttrStr("lastImage")).isNull();

        page = userService.listUsersExtendedByProject(
            project,
            userSearchExtension,
            new ArrayList<>(),
            "lastConnection",
            "asc",
            0L,
            0L
        );
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent().get(0).getJSONAttrLong("id")).isEqualTo(userNeverOpenProject.getId());
        assertThat(page.getContent().get(0).getJSONAttrDate("lastConnection")).isNull();
        assertThat(page.getContent().get(1).getJSONAttrLong("id")).isEqualTo(userWhoHasOpenProject.getId());
        assertThat(page.getContent()
            .get(1)
            .getJSONAttrDate("lastConnection")).isEqualTo(userWhoHasOpenProjectConnection.getCreated());
        assertThat(page.getContent().get(2).getJSONAttrLong("id")).isEqualTo(userWhoHasOpenProjectAfter.getId());
        assertThat(page.getContent().get(2).getJSONAttrDate("lastConnection")).isEqualTo(
            userWhoHasOpenProjectAfterConnection.getCreated());
    }

    @Test
    void listUserExtendedWithConnectionFrequency() {
        User userWhoHasOpenOnce = builder.givenAUser();
        User userWhoHasOpenProject11x = builder.givenAUser();
        User userNeverOpenProject = builder.givenAUser();

        Project project = builder.givenAProject();

        builder.addUserToProject(project, userWhoHasOpenOnce.getUsername(), READ);
        builder.addUserToProject(project, userWhoHasOpenProject11x.getUsername(), READ);
        builder.addUserToProject(project, userNeverOpenProject.getUsername(), WRITE);

        PersistentProjectConnection userWhoHasOpenProjectConnection = givenAPersistentConnectionInProject(
            userWhoHasOpenOnce,
            project,
            DateUtils.addDays(new Date(), -2)
        );
        for (int i = 0; i < 11; i++) {
            givenAPersistentConnectionInProject(
                userWhoHasOpenProject11x,
                project,
                DateUtils.addDays(new Date(), -1)
            );
        }

        UserSearchExtension userSearchExtension = new UserSearchExtension();
        userSearchExtension.setWithNumberConnections(true);
        Page<JsonObject> page = userService.listUsersExtendedByProject(
            project,
            userSearchExtension,
            new ArrayList<>(),
            "frequency",
            "desc",
            0L,
            0L
        );
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent().get(0).getJSONAttrLong("id")).isEqualTo(userWhoHasOpenProject11x.getId());
        assertThat(page.getContent().get(0).getJSONAttrInteger("numberConnections")).isEqualTo(11);
        assertThat(page.getContent().get(1).getJSONAttrLong("id")).isEqualTo(userWhoHasOpenOnce.getId());
        assertThat(page.getContent().get(1).getJSONAttrInteger("numberConnections")).isEqualTo(1);
        assertThat(page.getContent().get(2).getJSONAttrLong("id")).isEqualTo(userNeverOpenProject.getId());
        assertThat(page.getContent().get(2).getJSONAttrInteger("numberConnections")).isEqualTo(0);

        page = userService.listUsersExtendedByProject(
            project,
            userSearchExtension,
            new ArrayList<>(),
            "frequency",
            "asc",
            0L,
            0L
        );
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent().get(0).getJSONAttrLong("id")).isEqualTo(userNeverOpenProject.getId());
        assertThat(page.getContent().get(0).getJSONAttrInteger("numberConnections")).isEqualTo(0);
        assertThat(page.getContent().get(1).getJSONAttrLong("id")).isEqualTo(userWhoHasOpenOnce.getId());
        assertThat(page.getContent().get(1).getJSONAttrInteger("numberConnections")).isEqualTo(1);
        assertThat(page.getContent().get(2).getJSONAttrLong("id")).isEqualTo(userWhoHasOpenProject11x.getId());
        assertThat(page.getContent().get(2).getJSONAttrInteger("numberConnections")).isEqualTo(11);
    }

    @Test
    void listProjectAdmins() {
        User user = builder.givenSuperAdmin();

        Project projectWhereUserIsManager = builder.givenAProject();
        Project projectWhereUserIsContributor = builder.givenAProject();
        Project projectWhereUserIsMissing = builder.givenAProject();
        Project projectWithTwoUsers = builder.givenAProject();

        builder.addUserToProject(projectWhereUserIsManager, "superadmin", ADMINISTRATION);
        builder.addUserToProject(projectWhereUserIsContributor, "superadmin", WRITE);
        builder.addUserToProject(projectWithTwoUsers, "superadmin", WRITE);

        User anotherUser = builder.givenAUser();
        builder.addUserToProject(projectWhereUserIsMissing, anotherUser.getUsername(), WRITE);
        builder.addUserToProject(projectWithTwoUsers, anotherUser.getUsername(), WRITE);

        assertThat(userService.listAdmins(projectWhereUserIsManager)).contains(user).doesNotContain(anotherUser);
        assertThat(userService.listAdmins(projectWhereUserIsContributor)).doesNotContain(user);
    }

    @Test
    void listProjectUsers() {
        User user = builder.givenSuperAdmin();

        Project projectWhereUserIsManager = builder.givenAProject();
        Project projectWhereUserIsContributor = builder.givenAProject();
        Project projectWhereUserIsMissing = builder.givenAProject();
        Project projectWithTwoUsers = builder.givenAProject();

        builder.addUserToProject(projectWhereUserIsManager, "superadmin", ADMINISTRATION);
        builder.addUserToProject(projectWhereUserIsContributor, "superadmin", WRITE);
        builder.addUserToProject(projectWithTwoUsers, "superadmin", WRITE);

        User anotherUser = builder.givenAUser();
        builder.addUserToProject(projectWhereUserIsMissing, anotherUser.getUsername(), WRITE);
        builder.addUserToProject(projectWithTwoUsers, anotherUser.getUsername(), WRITE);

        assertThat(userService.listUsers(projectWhereUserIsManager)).contains(user).doesNotContain(anotherUser);
        assertThat(userService.listUsers(projectWhereUserIsContributor)).contains(user).doesNotContain(anotherUser);
        assertThat(userService.listUsers(projectWithTwoUsers)).contains(user, anotherUser);
    }

    @Test
    void findProjectCreator() {
        User user = builder.givenSuperAdmin();

        Project projectWhereUserIsManager = builder.givenAProject();
        builder.addUserToProject(projectWhereUserIsManager, "superadmin", ADMINISTRATION);

        assertThat(userService.findCreator(projectWhereUserIsManager)).contains(user);
    }

    @Test
    void listOntologyUsers() {
        User user = builder.givenSuperAdmin();

        Project projectWhereUserIsManager = builder.givenAProject();
        Project projectWhereUserIsContributor = builder.givenAProject();

        builder.addUserToProject(projectWhereUserIsManager, "superadmin", ADMINISTRATION);
        builder.addUserToProject(projectWhereUserIsContributor, "superadmin", WRITE);

        assertThat(userService.listUsers(projectWhereUserIsManager.getOntology()))
            .contains(user);
        assertThat(userService.listUsers(projectWhereUserIsContributor.getOntology()))
            .contains(user);
    }

    @Test
    void listStorageUsers() {
        Storage storage = builder.givenAStorage(builder.givenSuperAdmin());

        assertThat(userService.listUsers(storage)).contains(builder.givenSuperAdmin());
    }

    @Test
    void listAllProjectUsers() {
        User user = builder.givenSuperAdmin();

        Project project = builder.givenAProject();

        builder.addUserToProject(project, "superadmin", WRITE);

        assertThat(userService.listAll(project)).contains(user);
    }

    @Test
    void listLayers() {
        User user = builder.givenAUser();
        User anotherUserInProject = builder.givenAUser();
        User anotherUserNotInProject = builder.givenAUser();

        Project project = builder.givenAProject();

        builder.addUserToProject(project, user.getUsername(), WRITE);
        builder.addUserToProject(project, anotherUserInProject.getUsername(), WRITE);

        assertThat(userService.listLayers(project, builder.givenAnImageInstance(project))
            .stream()
            .map(x -> x.getJSONAttrLong("id")))
            .contains(user.getId(), anotherUserInProject.getId())
            .doesNotContain(anotherUserNotInProject.getId());
    }

    @WithMockUser("user")
    @Test
    void listLayersWithProjectWithPrivateAdminLayer() {
        User user = builder.givenDefaultUser();
        User adminInProject = builder.givenAUser();

        Project project = builder.givenAProject();
        project.setHideAdminsLayers(true);

        builder.addUserToProject(project, user.getUsername(), WRITE);
        builder.addUserToProject(project, adminInProject.getUsername(), ADMINISTRATION);

        assertThat(userService.listLayers(project, builder.givenAnImageInstance(project))
            .stream()
            .map(x -> x.getJSONAttrLong("id")))
            .hasSize(1)
            .contains(user.getId())
            .doesNotContain(adminInProject.getId());
    }

    @WithMockUser("user")
    @Test
    void listLayersWithProjectWithPrivateUserLayer() {
        User user = builder.givenDefaultUser();
        User userInProject = builder.givenAUser();

        Project project = builder.givenAProject();
        project.setHideUsersLayers(true);

        builder.addUserToProject(project, user.getUsername(), WRITE);
        builder.addUserToProject(project, userInProject.getUsername(), WRITE);

        assertThat(userService.listLayers(project, builder.givenAnImageInstance(project))
            .stream()
            .map(x -> x.getJSONAttrLong("id")))
            .hasSize(1)
            .contains(user.getId())
            .doesNotContain(userInProject.getId());
    }

    @WithMockUser("user")
    @Test
    void listLayersWithProjectWithPrivateUserLayerWithProjectAdminRole() {
        User user = builder.givenDefaultUser();
        User userInProject = builder.givenAUser();

        Project project = builder.givenAProject();
        project.setHideUsersLayers(true);

        builder.addUserToProject(project, user.getUsername(), ADMINISTRATION);
        builder.addUserToProject(project, userInProject.getUsername(), WRITE);

        assertThat(userService.listLayers(project, builder.givenAnImageInstance(project))
            .stream()
            .map(x -> x.getJSONAttrLong("id")))
            .hasSize(2)
            .contains(user.getId(), userInProject.getId());
    }

    @Test
    void listOnlineUser() {
        User userOnline = builder.givenDefaultUser();
        User userOffline = builder.givenAUser();

        assertThat(userService.getAllOnlineUsers()).isEmpty();
        givenALastConnection(userOnline, null, new Date());

        assertThat(userService.getAllOnlineUsers()).contains(userOnline)
            .doesNotContain(userOffline);
    }

    @Test
    void listOnlineUserForProject() {
        User userOnline = builder.givenDefaultUser();
        User userOnlineButOnDifferentProject = builder.givenAUser();
        User userOffline = builder.givenAUser();

        Project project = builder.givenAProject();
        Project anotherProject = builder.givenAProject();

        givenALastConnection(userOffline, project.getId(), DateUtils.addDays(new Date(), -15));
        givenALastConnection(userOnline, project.getId(), DateUtils.addSeconds(new Date(), -15));
        givenALastConnection(
            userOnlineButOnDifferentProject,
            anotherProject.getId(),
            DateUtils.addSeconds(new Date(), -10)
        );


        assertThat(userService.getAllOnlineUserIds(project)).contains(userOnline.getId())
            .doesNotContain(userOnlineButOnDifferentProject.getId(), userOffline.getId());
        assertThat(userService.getAllOnlineUsers(project)).contains(userOnline)
            .doesNotContain(userOnlineButOnDifferentProject, userOffline);
    }


    @Test
    void listFriendUsers() {
        User user = builder.givenDefaultUser();
        User userFriend = builder.givenAUser();
        User userNotFriend = builder.givenAUser();

        Project project = builder.givenAProject();

        builder.addUserToProject(project, user.getUsername(), READ);
        builder.addUserToProject(project, userFriend.getUsername(), READ);

        assertThat(userService.getAllFriendsUsers(user)).contains(userFriend)
            .doesNotContain(userNotFriend);
    }

    @Test
    void listFriendUsersOffline() {
        User user = builder.givenDefaultUser();
        User userFriendOnline = builder.givenAUser();
        User userFriendOffline = builder.givenAUser();

        Project project = builder.givenAProject();

        builder.addUserToProject(project, user.getUsername(), READ);
        builder.addUserToProject(project, userFriendOnline.getUsername(), READ);
        builder.addUserToProject(project, userFriendOffline.getUsername(), READ);

        givenALastConnection(userFriendOffline, project.getId(), DateUtils.addDays(new Date(), -15));
        givenALastConnection(userFriendOnline, project.getId(), DateUtils.addSeconds(new Date(), -15));

        assertThat(userService.getAllFriendsUsersOnline(user)).contains(userFriendOnline)
            .doesNotContain(userFriendOffline);
    }

    @Test
    void listFriendUsersOfflineOnAProject() {
        User user = builder.givenDefaultUser();
        User userFriendOnline = builder.givenAUser();
        User userFriendOnlineButOnAnotherProject = builder.givenAUser();

        Project project = builder.givenAProject();

        builder.addUserToProject(project, user.getUsername(), READ);
        builder.addUserToProject(project, userFriendOnline.getUsername(), READ);
        builder.addUserToProject(project, userFriendOnlineButOnAnotherProject.getUsername(), READ);

        givenALastConnection(
            userFriendOnlineButOnAnotherProject,
            builder.givenAProject().getId(),
            DateUtils.addSeconds(new Date(), -15)
        );
        givenALastConnection(userFriendOnline, project.getId(), DateUtils.addSeconds(new Date(), -15));

        assertThat(userService.getAllFriendsUsersOnline(user, project)).contains(userFriendOnline)
            .doesNotContain(userFriendOnlineButOnAnotherProject);
    }

    @Test
    void listOnlineUserForProjectWitTheirActivities() {
        User userOnline = builder.givenDefaultUser();

        Project project = builder.givenAProject();

        builder.addUserToProject(project, userOnline.getUsername());

        PersistentProjectConnection lastConnection = givenAPersistentConnectionInProject(
            userOnline,
            project,
            DateUtils.addSeconds(new Date(), -15)
        );

        PersistentImageConsultation consultation = givenAPersistentImageConsultation(
            userOnline,
            builder.givenAnImageInstance(project),
            new Date()
        );

        List<JsonObject> allOnlineUserWithTheirPositions = userService.getUsersWithLastActivities(project);
        assertThat(allOnlineUserWithTheirPositions).hasSize(1);
        assertThat(allOnlineUserWithTheirPositions.get(0).get("id")).isEqualTo(userOnline.getId());
        assertThat(allOnlineUserWithTheirPositions.get(0).get("lastImageId")).isEqualTo(consultation.getImage());
        assertThat(allOnlineUserWithTheirPositions.get(0).get("lastImageName")).isNotNull();
        assertThat(allOnlineUserWithTheirPositions.get(0).get("lastConnection")).isNotNull();
        assertThat(allOnlineUserWithTheirPositions.get(0).get("frequency")).isEqualTo(1);
    }


    @Test
    void listOnlineUserForProjectWitTheirPosition() {
        User userOnline = builder.givenDefaultUser();
        User userOnlineButOnDifferentProject = builder.givenAUser();
        User userOffline = builder.givenAUser();

        Project project = builder.givenAProject();
        Project anotherProject = builder.givenAProject();

        givenALastConnection(userOffline, project.getId(), DateUtils.addDays(new Date(), -15));
        givenALastConnection(userOnline, project.getId(), DateUtils.addSeconds(new Date(), -15));
        givenALastConnection(
            userOnlineButOnDifferentProject,
            anotherProject.getId(),
            DateUtils.addSeconds(new Date(), -10)
        );

        givenAPersistentUserPosition(
            DateUtils.addSeconds(new Date(), -15), userOnline,
            builder.givenANotPersistedSliceInstance(
                builder.givenAnImageInstance(project),
                builder.givenAnAbstractSlice()
            ), UserPositionServiceTests.USER_VIEW
        );

        List<JsonObject> allOnlineUserWithTheirPositions = userService.getAllOnlineUserWithTheirPositions(project);
        assertThat(allOnlineUserWithTheirPositions.stream()
            .filter(x -> x.getId().equals(userOnline.getId()))
            .findFirst()).isPresent();
        assertThat(allOnlineUserWithTheirPositions.stream()
            .filter(x -> x.getId().equals(userOnline.getId()))
            .findFirst()
            .get()
            .get("position")).isNotNull();
        assertThat(allOnlineUserWithTheirPositions.stream()
            .filter(x -> x.getId().equals(userOnlineButOnDifferentProject.getId()))
            .findFirst()).isEmpty();
        assertThat(allOnlineUserWithTheirPositions.stream()
            .filter(x -> x.getId().equals(userOffline.getId()))
            .findFirst()).isEmpty();
    }

    PersistentUserPosition givenAPersistentUserPosition(
        Date creation,
        User user,
        SliceInstance sliceInstance,
        AreaDTO areaDTO
    ) {
        return userPositionService.add(
            creation,
            user,
            sliceInstance,
            sliceInstance.getImage(),
            areaDTO,
            1,
            5.0,
            false
        );
    }

    @Test
    void listUserResumeActivities() {
        User userOnline = builder.givenDefaultUser();
        Project project = builder.givenAProject();
        builder.addUserToProject(project, userOnline.getUsername());

        PersistentProjectConnection firstConnection = givenAPersistentConnectionInProject(
            userOnline,
            project,
            DateUtils.addDays(new Date(), -15)
        );
        PersistentProjectConnection lastConnection = givenAPersistentConnectionInProject(
            userOnline,
            project,
            DateUtils.addSeconds(new Date(), -15)
        );

        givenAPersistentImageConsultation(userOnline, builder.givenAnImageInstance(project), new Date());

        JsonObject data = userService.getResumeActivities(project, userOnline);

        assertThat(data.getJSONAttrDate("firstConnection")).isEqualTo(firstConnection.getCreated());
        assertThat(data.getJSONAttrDate("lastConnection")).isEqualTo(lastConnection.getCreated());
        assertThat(data.getJSONAttrInteger("totalAnnotations")).isEqualTo(0);
        assertThat(data.getJSONAttrInteger("totalConnections")).isEqualTo(2);
        assertThat(data.getJSONAttrInteger("totalConsultations")).isEqualTo(1);
        assertThat(data.getJSONAttrInteger("totalAnnotationSelections")).isEqualTo(0);
    }

    // TODO: IAM Account

    @Test
    void addUserToProject() {
        User user = builder.givenAUser();
        Project project = builder.givenAProject();

        assertThat(permissionService.hasACLPermission(project, user.getUsername(), ADMINISTRATION)).isFalse();
        assertThat(permissionService.hasACLPermission(project, user.getUsername(), READ)).isFalse();

        projectMemberService.addUserToProject(user, project, false);

        assertThat(permissionService.hasACLPermission(project, user.getUsername(), ADMINISTRATION)).isFalse();
        assertThat(permissionService.hasACLPermission(project, user.getUsername(), READ)).isTrue();

        projectMemberService.addUserToProject(user, project, true);

        assertThat(permissionService.hasACLPermission(project, user.getUsername(), ADMINISTRATION)).isTrue();
        assertThat(permissionService.hasACLPermission(project, user.getUsername(), READ)).isTrue();
    }

    @Test
    void removeUserFromProject() {
        User user = builder.givenAUser();
        Project project = builder.givenAProject();

        projectMemberService.addUserToProject(user, project, true);

        assertThat(permissionService.hasACLPermission(project, user.getUsername(), ADMINISTRATION)).isTrue();
        assertThat(permissionService.hasACLPermission(project, user.getUsername(), READ)).isTrue();
        assertThat(permissionService.hasACLPermission(project.getOntology(), user.getUsername(), READ)).isTrue();

        projectMemberService.deleteUserFromProject(user, project, true);

        assertThat(permissionService.hasACLPermission(project, user.getUsername(), ADMINISTRATION)).isFalse();
        assertThat(permissionService.hasACLPermission(project, user.getUsername(), READ)).isTrue();
        assertThat(permissionService.hasACLPermission(project.getOntology(), user.getUsername(), READ)).isTrue();

        projectMemberService.deleteUserFromProject(user, project, false);

        assertThat(permissionService.hasACLPermission(project, user.getUsername(), ADMINISTRATION)).isFalse();
        assertThat(permissionService.hasACLPermission(project, user.getUsername(), READ)).isFalse();
        assertThat(permissionService.hasACLPermission(project.getOntology(), user.getUsername(), READ)).isFalse();
    }

    @Test
    void removeOntologyRightWhenRemovingUserFromProject() {
        User user = builder.givenAUser();
        Project project = builder.givenAProject();

        projectMemberService.addUserToProject(user, project, false);

        assertThat(permissionService.hasACLPermission(project, user.getUsername(), READ)).isTrue();
        assertThat(permissionService.hasACLPermission(project.getOntology(), user.getUsername(), READ)).isTrue();

        projectMemberService.deleteUserFromProject(user, project, false);

        assertThat(permissionService.hasACLPermission(project, user.getUsername(), READ)).isFalse();
        assertThat(permissionService.hasACLPermission(project.getOntology(), user.getUsername(), READ)).isFalse();
    }

    @Test
    void removeOntologyRightWhenRemovingUserFromProjectKeepRightIfUserHasAnotherProjectWithOntology() {
        User user = builder.givenAUser();
        Project project = builder.givenAProject();

        projectMemberService.addUserToProject(user, project, false);

        assertThat(permissionService.hasACLPermission(project, user.getUsername(), READ)).isTrue();
        assertThat(permissionService.hasACLPermission(project.getOntology(), user.getUsername(), READ)).isTrue();

        Project projectWithSameOntology = builder.givenAProject();
        projectWithSameOntology.setOntology(project.getOntology());
        projectMemberService.addUserToProject(user, projectWithSameOntology, false);

        projectMemberService.deleteUserFromProject(user, project, false);

        assertThat(permissionService.hasACLPermission(project, user.getUsername(), READ)).isFalse();
        assertThat(permissionService.hasACLPermission(project.getOntology(), user.getUsername(), READ)).isTrue();
    }

    @Test
    void addAndDeleteUserToStorage() {
        User user = builder.givenAUser();
        Storage storage = builder.givenAStorage();

        assertThat(permissionService.hasACLPermission(storage, user.getUsername(), ADMINISTRATION)).isFalse();
        assertThat(permissionService.hasACLPermission(storage, user.getUsername(), READ)).isFalse();

        userService.addUserToStorage(user, storage);

        assertThat(permissionService.hasACLPermission(storage, user.getUsername(), ADMINISTRATION)).isFalse();
        assertThat(permissionService.hasACLPermission(storage, user.getUsername(), READ)).isTrue();

        userService.deleteUserFromStorage(user, storage);

        assertThat(permissionService.hasACLPermission(storage, user.getUsername(), ADMINISTRATION)).isFalse();
        assertThat(permissionService.hasACLPermission(storage, user.getUsername(), READ)).isFalse();
    }
}
